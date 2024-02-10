-- Adapted based on SOM benchmark.
-- Ported on Lua by Francois Perrad <francois.perrad@gadz.org>
--
-- Copyright 2011 Google Inc.
--
--     Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
--     You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
--     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--     See the License for the specific language governing permissions and
--         limitations under the License.

local Vector = require'som'.Vector
local Set = require'som'.Set
local IdentitySet = require'som'.IdentitySet
local IdentityDictionary = require'som'.IdentityDictionary

local BasicBlock = {_CLASS = 'BasicBlock'} do

function BasicBlock.new (name)
    local obj = {
        name = name,
        in_edges  = Vector.new(2),
        out_edges = Vector.new(2),
    }
    return setmetatable(obj, {__index = BasicBlock})
end

function BasicBlock:num_pred ()
    return self.in_edges:size()
end

function BasicBlock:add_out_edge (to)
    self.out_edges:append(to)
end

function BasicBlock:add_in_edge (from)
    self.in_edges:append(from)
end

function BasicBlock:custom_hash ()
    return self.name
end

end -- class BasicBlock

local BasicBlockEdge = {_CLASS = 'BasicBlockEdge'} do

function BasicBlockEdge.new (cfg, from_name, to_name)
    local from = cfg:create_node(from_name)
    local to   = cfg:create_node(to_name)

    from:add_out_edge(to)
    to:add_in_edge(from)
    local obj = {
        from = from,
        to   = to,
    }
    setmetatable(obj, {__index = BasicBlockEdge})

    cfg:add_edge(obj)
    return obj
end

end -- class BasicBlockEdge

local ControlFlowGraph = {_CLASS = 'ControlFlowGraph'} do

function ControlFlowGraph.new ()
    local obj = {
        start_node = nil,
        basic_block_map = Vector.new(),
        edge_list = Vector.new(),
    }
    return setmetatable(obj, {__index = ControlFlowGraph})
end

function ControlFlowGraph:create_node (name)
    local node
    if self.basic_block_map:at(name) then
        node = self.basic_block_map:at(name)
    else
        node = BasicBlock.new(name)
        self.basic_block_map:at_put(name, node)
    end
    if self:num_nodes() == 1 then
        self.start_node = node
    end
    return node
end

function ControlFlowGraph:add_edge (edge)
    self.edge_list:append(edge)
end

function ControlFlowGraph:num_nodes ()
    return self.basic_block_map:size()
end

function ControlFlowGraph:get_start_basic_block ()
    return self.start_node
end

function ControlFlowGraph:get_basic_blocks ()
    return self.basic_block_map
end

end -- class ControlFlowGraph

local SimpleLoop = {_CLASS = 'SimpleLoop'} do

function SimpleLoop.new (bb, is_reducible)
    local obj = {
        header        = bb,
        is_reducible  = is_reducible,
        parent        = nil,
        is_root       = false,
        nesting_level = 0,
        depth_level   = 0,
        counter       = 0,
        basic_blocks  = IdentitySet.new(),
        children      = IdentitySet.new(),
    }
    if bb then
        obj.basic_blocks:add(bb)
    end
    return setmetatable(obj, {__index = SimpleLoop})
end

function SimpleLoop:add_node (bb)
    self.basic_blocks:add(bb)
end

function SimpleLoop:add_child_loop (loop)
    self.children:add(loop)
end

function SimpleLoop:set_parent (parent)
    self.parent = parent
    self.parent:add_child_loop(self)
end

function SimpleLoop:set_is_root ()
    self.is_root = true
end

function SimpleLoop:set_nesting_level (level)
    self.nesting_level = level
    if level == 0 then
        self:set_is_root()
    end
end

end -- class SimpleLoop

local LoopStructureGraph = {_CLASS = 'LoopStructureGraph'} do

function LoopStructureGraph.new ()
    local loops = Vector.new()
    local root = SimpleLoop.new(nil, true)
    local obj = {
        loop_counter = 0,
        loops = loops,
        root = root,
    }
    root:set_nesting_level(0)
    root.counter = obj.loop_counter
    obj.loop_counter = obj.loop_counter + 1
    loops:append(root)
    return setmetatable(obj, {__index = LoopStructureGraph})
end

function LoopStructureGraph:create_new_loop (bb, is_reducible)
    local loop = SimpleLoop.new(bb, is_reducible)
    loop.counter = self.loop_counter
    self.loop_counter = self.loop_counter + 1
    self.loops:append(loop)
    return loop
end

function LoopStructureGraph:calculate_nesting_level ()
    -- link up all 1st level loops to artificial root node.
    self.loops:each(function (it)
        if not it.is_root then
            if not it.parent then
                it:set_parent(self.root)
            end
        end
    end)
    -- recursively traverse the tree and assign levels.
    self:calculate_nesting_level_rec(self.root, 0)
end

function LoopStructureGraph:calculate_nesting_level_rec (loop, depth)
    loop.depth_level = depth
    loop.children:each(function (it)
        self:calculate_nesting_level_rec(it, depth + 1)
        loop:set_nesting_level(math.max(loop.nesting_level, 1 + it.nesting_level))
    end)
end

function LoopStructureGraph:num_loops ()
    return self.loops:size()
end

end -- class LoopStructureGraph

local UnionFindNode = {_CLASS = 'UnionFindNode'} do

function UnionFindNode.new ()
    local obj = {
        dfs_number = 0,
        parent = nil,
        bb     = nil,
        loop   = nil,
    }
    return setmetatable(obj, {__index = UnionFindNode})
end

function UnionFindNode:init_node (bb, dfs_number)
    self.parent     = self
    self.bb         = bb
    self.dfs_number = dfs_number
    self.loop       = nil
end

function UnionFindNode:find_set ()
    local node_list = Vector.new()

    local node = self
    while node ~= node.parent do
        if node.parent ~= node.parent.parent then
            node_list:append(node)
        end
        node = node.parent
    end

    -- Path Compression, all nodes' parents point to the 1st level parent.
    node_list:each(function (it)
        it:union(self.parent)
    end)
    return node
end

function UnionFindNode:union (basic_block)
    self.parent = basic_block
end

end -- class UnionFindNode

local HavlakLoopFinder = {_CLASS = 'HavlakLoopFinder'} do

local UNVISITED = 2147483647            -- Marker for uninitialized nodes.
local MAXNONBACKPREDS = 32 * 1024       -- Safeguard against pathological algorithm behavior.

function HavlakLoopFinder.new (cfg, lsg)
    local obj = {
        cfg = cfg,
        lsg = lsg,
        non_back_preds = Vector.new(),
        back_preds     = Vector.new(),
        number         = IdentityDictionary.new(),
        max_size = 0,
        header = nil,
        type   = nil,
        last   = nil,
        nodes  = nil,
    }
    return setmetatable(obj, {__index = HavlakLoopFinder})
end

-- As described in the paper, determine whether a node 'w' is a
-- "true" ancestor for node 'v'.
--
-- Dominance can be tested quickly using a pre-order trick
-- for depth-first spanning trees. This is why DFS is the first
-- thing we run below.
function HavlakLoopFinder:is_ancestor (w, v)
    return (w <= v) and (v <= self.last[w])
end

-- DFS - Depth-First-Search
--
-- DESCRIPTION:
-- Simple depth first traversal along out edges with node numbering.
function HavlakLoopFinder:do_dfs (current_node, current)
    self.nodes[current]:init_node(current_node, current)
    self.number:at_put(current_node, current)

    local last_id = current
    local outer_blocks = current_node.out_edges

    outer_blocks:each(function (target)
        if self.number:at(target) == UNVISITED then
            last_id = self:do_dfs(target, last_id + 1)
        end
    end)

    self.last[current] = last_id
    return last_id
end

function HavlakLoopFinder:init_all_nodes ()
    -- Step a:
    --   - initialize all nodes as unvisited.
    --   - depth-first traversal and numbering.
    --   - unreached BB's are marked as dead.
    self.cfg:get_basic_blocks():each(function (bb)
        self.number:at_put(bb, UNVISITED)
    end)
    self:do_dfs(self.cfg:get_start_basic_block(), 1)
end

function HavlakLoopFinder:identify_edges (size)
    -- Step b:
    --   - iterate over all nodes.
    --
    --   A backedge comes from a descendant in the DFS tree, and non-backedges
    --   from non-descendants (following Tarjan).
    --
    --   - check incoming edges 'v' and add them to either
    --     - the list of backedges (backPreds) or
    --     - the list of non-backedges (nonBackPreds)
    for w = 1, size do
        self.header[w] = 1
        self.type[w] = 'BB_NONHEADER'

        local node_w = self.nodes[w].bb
        if not node_w then
            self.type[w] = 'BB_DEAD'
        else
            self:process_edges(node_w, w)
        end
    end
end

function HavlakLoopFinder:process_edges (node_w, w)
    local number = self.number
    if node_w:num_pred() > 0 then
        node_w.in_edges:each(function (node_v)
            local v = number:at(node_v)
            if v ~= UNVISITED then
                if self:is_ancestor(w, v) then
                    self.back_preds:at(w):append(v)
                else
                    self.non_back_preds:at(w):add(v)
                end
            end
        end)
    end
end

-- Find loops and build loop forest using Havlak's algorithm, which
-- is derived from Tarjan. Variable names and step numbering has
-- been chosen to be identical to the nomenclature in Havlak's
-- paper (which, in turn, is similar to the one used by Tarjan).
function HavlakLoopFinder:find_loops ()
    if not self.cfg:get_start_basic_block() then
        return
    end

    local size = self.cfg:num_nodes()
    self.non_back_preds:remove_all()
    self.back_preds:remove_all()
    self.number:remove_all()

    if size > self.max_size then
        self.header   = {}
        self.type     = {}
        self.last     = {}
        self.nodes    = {}
        self.max_size = size
    end

    for i = 1, size do
        self.non_back_preds:append(Set.new())
        self.back_preds:append(Vector.new())
        self.nodes[i] = UnionFindNode.new()
    end

    self:init_all_nodes()
    self:identify_edges(size)

    -- Start node is root of all other loops.
    self.header[0] = 0

    -- Step c:
    --
    -- The outer loop, unchanged from Tarjan. It does nothing except
    -- for those nodes which are the destinations of backedges.
    -- For a header node w, we chase backward from the sources of the
    -- backedges adding nodes to the set P, representing the body of
    -- the loop headed by w.
    --
    -- By running through the nodes in reverse of the DFST preorder,
    -- we ensure that inner loop headers will be processed before the
    -- headers for surrounding loops.
    for w = size, 1, -1 do
        -- this is 'P' in Havlak's paper
        local node_pool = Vector.new()
        local node_w = self.nodes[w].bb
        if node_w then
            self:step_d(w, node_pool)

            -- Copy nodePool to workList.
            local work_list = Vector.new()
            node_pool:each(function (it)
                work_list:append(it)
            end)

            if node_pool:size() ~= 0 then
                self.type[w] = 'BB_REDUCIBLE'
            end

            -- work the list...
            while not work_list:is_empty() do
                local x = work_list:remove_first()

                -- Step e:
                --
                -- Step e represents the main difference from Tarjan's method.
                -- Chasing upwards from the sources of a node w's backedges. If
                -- there is a node y' that is not a descendant of w, w is marked
                -- the header of an irreducible loop, there is another entry
                -- into this loop that avoids w.

                -- The algorithm has degenerated. Break and
                -- return in this case.
                local non_back_size = self.non_back_preds:at(x.dfs_number):size()
                if non_back_size > MAXNONBACKPREDS then
                    return
                end
                self:step_e_process_non_back_preds(w, node_pool, work_list, x)
            end
        end

        -- Collapse/Unionize nodes in a SCC to a single node
        -- For every SCC found, create a loop descriptor and link it in.
        if (node_pool:size() > 0) or (self.type[w] == 'BB_SELF') then
            local loop = self.lsg:create_new_loop(node_w, self.type[w] ~= 'BB_IRREDUCIBLE')
            self:set_loop_attributes(w, node_pool, loop)
        end
    end
end

function HavlakLoopFinder:step_e_process_non_back_preds (w, node_pool, work_list, x)
    self.non_back_preds:at(x.dfs_number):each(function (it)
        local y = self.nodes[it]
        local ydash = y:find_set()

        if not self:is_ancestor(w, ydash.dfs_number) then
            self.type[w] = 'BB_IRREDUCIBLE'
            self.non_back_preds:at(w):add(ydash.dfs_number)
        else
            if ydash.dfs_number ~= w then
                if not node_pool:has_some(function (e) return e == ydash end) then
                    work_list:append(ydash)
                    node_pool:append(ydash)
                end
            end
        end
    end)
end

function HavlakLoopFinder:set_loop_attributes (w, node_pool, loop)
    -- At this point, one can set attributes to the loop, such as:
    --
    -- the bottom node:
    --    iter  = backPreds[w].begin();
    --    loop bottom is: nodes[iter].node);
    --
    -- the number of backedges:
    --    backPreds[w].size()
    --
    -- whether this loop is reducible:
    --    type[w] != BasicBlockClass.BB_IRREDUCIBLE
    self.nodes[w].loop = loop
    node_pool:each(function (node)
        -- Add nodes to loop descriptor.
        self.header[node.dfs_number] = w
        node:union(self.nodes[w])

        -- Nested loops are not added, but linked together.
        if node.loop then
            node.loop:set_parent(loop)
        else
            loop:add_node(node.bb)
        end
    end)
end

function HavlakLoopFinder:step_d (w, node_pool)
    self.back_preds:at(w):each(function (v)
        if v ~= w then
            node_pool:append(self.nodes[v]:find_set())
        else
            self.type[w] = 'BB_SELF'
        end
    end)
end

end -- class HavlakLoopFinder

local LoopTesterApp = {_CLASS = 'LoopTesterApp'} do

function LoopTesterApp.new ()
    local cfg = ControlFlowGraph.new()
    local lsg = LoopStructureGraph.new()
    local obj = {
        cfg = cfg,
        lsg = lsg,
    }
    cfg:create_node(1)
    return setmetatable(obj, {__index = LoopTesterApp})
end

-- Create 4 basic blocks, corresponding to and if/then/else clause
-- with a CFG that looks like a diamond
function LoopTesterApp:build_diamond (start)
    local bb0 = start
    BasicBlockEdge.new(self.cfg, bb0, bb0 + 1)
    BasicBlockEdge.new(self.cfg, bb0, bb0 + 2)
    BasicBlockEdge.new(self.cfg, bb0 + 1, bb0 + 3)
    BasicBlockEdge.new(self.cfg, bb0 + 2, bb0 + 3)
    return bb0 + 3
end

-- Connect two existing nodes
function LoopTesterApp:build_connect (start, end_)
    BasicBlockEdge.new(self.cfg, start, end_)
end

-- Form a straight connected sequence of n basic blocks
function LoopTesterApp:build_straight (start, n)
    for i = 1, n do
        self:build_connect(start + i - 1, start + i)
    end
    return start + n
end

-- Construct a simple loop with two diamonds in it
function LoopTesterApp:build_base_loop (from)
    local header   = self:build_straight(from, 1)
    local diamond1 = self:build_diamond(header)
    local d11      = self:build_straight(diamond1, 1)
    local diamond2 = self:build_diamond(d11)
    local footer   = self:build_straight(diamond2, 1)
    self:build_connect(diamond2, d11)
    self:build_connect(diamond1, header)
    self:build_connect(footer, from)
    return self:build_straight(footer, 1)
end

function LoopTesterApp:main (num_dummy_loops, find_loop_iterations, par_loops,
                             ppar_loops, pppar_loops)
    self:construct_simple_cfg()
    self:add_dummy_loops(num_dummy_loops)
    self:construct_cfg(par_loops, ppar_loops, pppar_loops)

    -- Performing Loop Recognition, 1 Iteration, then findLoopIteration
    self:find_loops(self.lsg)

    for _ = 0, find_loop_iterations do
        self:find_loops(LoopStructureGraph.new())
    end

    self.lsg:calculate_nesting_level()
    return {self.lsg:num_loops(), self.cfg:num_nodes()}
end

function LoopTesterApp:construct_cfg (par_loops, ppar_loops, pppar_loops)
    local n = 3

    for _ = 1, par_loops do
        self.cfg:create_node(n + 1)
        self:build_connect(3, n + 1)
        n = n + 1

        for _ = 1, ppar_loops do
            local top = n
            n = self:build_straight(n, 1)
            for _ = 1, pppar_loops do
                n = self:build_base_loop(n)
            end
            local bottom = self:build_straight(n, 1)
            self:build_connect(n, top)
            n = bottom
        end
        self:build_connect(n, 1)
    end
end

function LoopTesterApp:add_dummy_loops (num_dummy_loops)
    for _ = 1, num_dummy_loops do
        self:find_loops(self.lsg)
    end
end

function LoopTesterApp:find_loops (loop_structure)
    local finder = HavlakLoopFinder.new(self.cfg, loop_structure)
    finder:find_loops()
end

function LoopTesterApp:construct_simple_cfg ()
    self.cfg:create_node(1)
    self:build_base_loop(1)
    self.cfg:create_node(2)
    BasicBlockEdge.new(self.cfg, 1, 3)
end

end -- class LoopTesterApp


local havlak = {} do
setmetatable(havlak, {__index = require'benchmark'})

function havlak:inner_benchmark_loop (inner_iterations)
    local result = LoopTesterApp.new():main(inner_iterations, 50, 10, 10, 5)
    return self:verify_result(result, inner_iterations)
end

function havlak:verify_result (result, inner_iterations)
    if     inner_iterations == 15000 then
        return result[1] == 46602 and result[2] == 5213
    elseif inner_iterations ==  1500 then
        return result[1] ==  6102 and result[2] == 5213
    elseif inner_iterations ==   150 then
        return result[1] ==  2052 and result[2] == 5213
    elseif inner_iterations ==    15 then
        return result[1] ==  1647 and result[2] == 5213
    elseif inner_iterations ==     1 then
        return result[1] ==  1605 and result[2] == 5213
    else
        print(('No verification result for %d found'):format(inner_iterations))
        print(('Result is: %d, %d'):format(result[0], result[1]))
        return false
    end
end

end -- object havlak

return havlak
