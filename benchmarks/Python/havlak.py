# Adapted based on SOM benchmark.
# Copyright 2011 Google Inc.
#
#     Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#         limitations under the License.
from enum import Enum

from benchmark import Benchmark
from som.identity_dictionary import IdentityDictionary
from som.identity_set import IdentitySet
from som.set import Set
from som.vector import Vector


class Havlak(Benchmark):
    def inner_benchmark_loop(self, inner_iterations):
        return self._verify_result(
            _LoopTesterApp().main(inner_iterations, 50, 10, 10, 5), inner_iterations
        )

    @staticmethod
    def _verify_result(result, inner_iterations):
        if inner_iterations == 15_000:
            return result[0] == 46_602 and result[1] == 5213
        if inner_iterations == 1_500:
            return result[0] == 6_102 and result[1] == 5213
        if inner_iterations == 150:
            return result[0] == 2_052 and result[1] == 5213
        if inner_iterations == 15:
            return result[0] == 1_647 and result[1] == 5213
        if inner_iterations == 1:
            return result[0] == 1_605 and result[1] == 5213

        print("No verification result for " + str(inner_iterations) + " found")
        print("Result is: " + str(result[0]) + ", " + str(result[1]))
        return False

    def benchmark(self):
        raise Exception("should not be reached")

    def verify_result(self, result):
        raise Exception("should not be reached")


class _BasicBlock:
    def __init__(self, name):
        self._name = name
        self._in_edges = Vector(2)
        self._out_edges = Vector(2)

    def get_in_edges(self):
        return self._in_edges

    def get_out_edges(self):
        return self._out_edges

    def get_num_pred(self):
        return self._in_edges.size()

    def add_out_edge(self, to):
        self._out_edges.append(to)

    def add_in_edge(self, from_):
        self._in_edges.append(from_)

    def custom_hash(self):
        return self._name


class _BasicBlockEdge:
    def __init__(self, cfg, from_name, to_name):
        self._from = cfg.create_node(from_name)
        self._to = cfg.create_node(to_name)

        self._from.add_out_edge(self._to)
        self._to.add_in_edge(self._from)

        cfg.add_edge(self)


class _ControlFlowGraph:
    def __init__(self):
        self._start_node = None
        self._basic_block_map = Vector()
        self._edge_list = Vector()

    def create_node(self, name):
        if self._basic_block_map.at(name):
            node = self._basic_block_map.at(name)
        else:
            node = _BasicBlock(name)
            self._basic_block_map.at_put(name, node)

        if self.num_nodes() == 1:
            self._start_node = node
        return node

    def add_edge(self, edge):
        self._edge_list.append(edge)

    def num_nodes(self):
        return self._basic_block_map.size()

    def get_start_basic_block(self):
        return self._start_node

    def get_basic_blocks(self):
        return self._basic_block_map


class _LoopStructureGraph:
    def __init__(self):
        self._loop_counter = 0
        self._loops = Vector()
        self._root = _SimpleLoop(None, True)
        self._root.set_nesting_level(0)
        self._root.counter = self._loop_counter
        self._loop_counter += 1
        self._loops.append(self._root)

    def create_new_loop(self, bb, is_reducible):
        loop = _SimpleLoop(bb, is_reducible)
        loop.counter = self._loop_counter
        self._loop_counter += 1
        self._loops.append(loop)
        return loop

    def calculate_nesting_level(self):
        def each(liter):
            if not liter.is_root():
                if liter.get_parent() is None:
                    liter.set_parent(self._root)

        self._loops.for_each(each)
        self._calculate_nesting_level_rec(self._root, 0)

    def _calculate_nesting_level_rec(self, loop, depth):
        loop.depth_level = depth

        def each(liter):
            self._calculate_nesting_level_rec(liter, depth + 1)
            loop.set_nesting_level(
                max(loop.get_nesting_level(), 1 + liter.get_nesting_level())
            )

        loop.get_children().for_each(each)

    def num_loops(self):
        return self._loops.size()


class _SimpleLoop:
    def __init__(self, bb, is_reducible):
        self._is_reducible = is_reducible
        self._parent = None
        self._is_root = False
        self._nesting_level = 0
        self._depth_level = 0
        self._counter = 0
        self._basic_blocks = IdentitySet()
        self._children = IdentitySet()

        if bb is not None:
            self._basic_blocks.add(bb)

        self._header = bb

    def add_node(self, bb):
        self._basic_blocks.add(bb)

    def add_child_loop(self, loop):
        self._children.add(loop)

    def get_children(self):
        return self._children

    def get_parent(self):
        return self._parent

    def get_nesting_level(self):
        return self._nesting_level

    def is_root(self):
        return self._is_root

    def set_parent(self, parent):
        self._parent = parent
        self._parent.add_child_loop(self)

    def set_is_root(self):
        self._is_root = True

    def set_counter(self, value):
        self._counter = value

    def set_nesting_level(self, level):
        self._nesting_level = level
        if level == 0:
            self.set_is_root()

    def set_depth_level(self, level):
        self._depth_level = level


class _UnionFindNode:
    def __init__(self):
        self._parent = None
        self._bb = None
        self._dfs_number = 0
        self._loop = None

    def init_node(self, bb, dfs_number):
        self._parent = self
        self._bb = bb
        self._dfs_number = dfs_number
        self._loop = None

    def find_set(self):
        node_list = Vector()

        node = self
        while node is not node.get_parent():
            if node.get_parent() is not node.get_parent().get_parent():
                node_list.append(node)
            node = node.get_parent()

        node_list.for_each(lambda i: i.union(self.get_parent()))
        return node

    def union(self, basic_block):
        self._parent = basic_block

    def get_bb(self):
        return self._bb

    def get_parent(self):
        return self._parent

    def get_loop(self):
        return self._loop

    def get_dfs_number(self):
        return self._dfs_number

    def set_loop(self, loop):
        self._loop = loop


class _LoopTesterApp:
    def __init__(self):
        self._cfg = _ControlFlowGraph()
        self._lsg = _LoopStructureGraph()
        self._cfg.create_node(0)

    def _build_diamond(self, start):
        bb0 = start
        _BasicBlockEdge(self._cfg, bb0, bb0 + 1)
        _BasicBlockEdge(self._cfg, bb0, bb0 + 2)
        _BasicBlockEdge(self._cfg, bb0 + 1, bb0 + 3)
        _BasicBlockEdge(self._cfg, bb0 + 2, bb0 + 3)
        return bb0 + 3

    def _build_connect(self, start, end_):
        _BasicBlockEdge(self._cfg, start, end_)

    def _build_straight(self, start, n):
        for i in range(n):
            self._build_connect(start + i, start + i + 1)
        return start + n

    def _build_base_loop(self, from_):
        header = self._build_straight(from_, 1)
        diamond1 = self._build_diamond(header)
        d11 = self._build_straight(diamond1, 1)
        diamond2 = self._build_diamond(d11)
        footer = self._build_straight(diamond2, 1)
        self._build_connect(diamond2, d11)
        self._build_connect(diamond1, header)

        self._build_connect(footer, from_)
        footer = self._build_straight(footer, 1)
        return footer

    def main(
        self, num_dummy_loops, find_loop_iterations, par_loops, ppar_loops, pppar_loops
    ):
        self._construct_simple_cfg()
        self._add_dummy_loops(num_dummy_loops)
        self._construct_cfg(par_loops, ppar_loops, pppar_loops)

        self._find_loops(self._lsg)
        for _ in range(find_loop_iterations):
            self._find_loops(_LoopStructureGraph())

        self._lsg.calculate_nesting_level()
        return [self._lsg.num_loops(), self._cfg.num_nodes()]

    def _construct_cfg(self, par_loops, ppar_loops, pppar_loops):
        n = 2

        for _ in range(par_loops):
            self._cfg.create_node(n + 1)
            self._build_connect(2, n + 1)
            n += 1

            for _ in range(ppar_loops):
                top = n
                n = self._build_straight(n, 1)
                for _ in range(pppar_loops):
                    n = self._build_base_loop(n)
                bottom = self._build_straight(n, 1)
                self._build_connect(n, top)
                n = bottom

            self._build_connect(n, 1)

    def _add_dummy_loops(self, num_dummy_loops):
        for _ in range(num_dummy_loops):
            self._find_loops(self._lsg)

    def _find_loops(self, loop_structure):
        finder = _HavlakLoopFinder(self._cfg, loop_structure)
        finder.find_loops()

    def _construct_simple_cfg(self):
        self._cfg.create_node(0)
        self._build_base_loop(0)
        self._cfg.create_node(1)
        _BasicBlockEdge(self._cfg, 0, 2)


_UNVISITED = 2_147_483_647
_MAXNONBACKPREDS = 32 * 1024


class _BasicBlockClass(Enum):
    BB_TOP = 0  # uninitialized
    BB_NONHEADER = 1  # a regular BB
    BB_REDUCIBLE = 2  # reducible loop
    BB_SELF = 3  # single BB loop
    BB_IRREDUCIBLE = 4  # irreducible loop
    BB_DEAD = 5  # a dead BB
    BB_LAST = 6  # Sentinel


class _HavlakLoopFinder:
    def __init__(self, cfg, lsg):
        self._cfg = cfg
        self._lsg = lsg
        self._non_back_preds = Vector()
        self._back_preds = Vector()
        self._number = IdentityDictionary()
        self._max_size = 0
        self._header = None
        self._type = None
        self._last = None
        self._nodes = None

    def _is_ancestor(self, w, v):
        return w <= v and v <= self._last[w]

    def _do_dfs(self, current_node, current):
        self._nodes[current].init_node(current_node, current)
        self._number.at_put(current_node, current)

        last_id = current
        outer_blocks = current_node.get_out_edges()

        def each(target):
            nonlocal last_id
            if self._number.at(target) == _UNVISITED:
                last_id = self._do_dfs(target, last_id + 1)

        outer_blocks.for_each(each)

        self._last[current] = last_id
        return last_id

    def _init_all_nodes(self):
        self._cfg.get_basic_blocks().for_each(
            lambda bb: self._number.at_put(bb, _UNVISITED)
        )

        self._do_dfs(self._cfg.get_start_basic_block(), 0)

    def _identify_edges(self, size):
        for w in range(size):
            self._header[w] = 0
            self._type[w] = _BasicBlockClass.BB_NONHEADER

            node_w = self._nodes[w].get_bb()
            if node_w is None:
                self._type[w] = _BasicBlockClass.BB_DEAD
            else:
                self._process_edges(node_w, w)

    def _process_edges(self, node_w, w):
        if node_w.get_num_pred() > 0:

            def each(node_v):
                v = self._number.at(node_v)
                if v != _UNVISITED:
                    if self._is_ancestor(w, v):
                        self._back_preds.at(w).append(v)
                    else:
                        self._non_back_preds.at(w).add(v)

            node_w.get_in_edges().for_each(each)

    def find_loops(self):
        if self._cfg.get_start_basic_block() is None:
            return

        size = self._cfg.num_nodes()
        self._non_back_preds.remove_all()
        self._back_preds.remove_all()
        self._number.remove_all()

        if size > self._max_size:
            self._header = [0] * size
            self._type = [None] * size
            self._last = [0] * size
            self._nodes = [None] * size
            self._max_size = size

        for i in range(size):
            self._non_back_preds.append(Set())
            self._back_preds.append(Vector())
            self._nodes[i] = _UnionFindNode()

        self._init_all_nodes()
        self._identify_edges(size)

        self._header[0] = 0

        for w in range(size - 1, -1, -1):
            node_pool = Vector()
            node_w = self._nodes[w].get_bb()
            if node_w is not None:
                self._step_d(w, node_pool)

                work_list = Vector()
                node_pool.for_each(work_list.append)

                if node_pool.size() != 0:
                    self._type[w] = _BasicBlockClass.BB_REDUCIBLE

                while not work_list.is_empty():
                    x = work_list.remove_first()

                    non_back_size = self._non_back_preds.at(x.get_dfs_number()).size()
                    if non_back_size > _MAXNONBACKPREDS:
                        return

                    self._step_e_process_non_back_preds(w, node_pool, work_list, x)

            if node_pool.size() > 0 or self._type[w] == _BasicBlockClass.BB_SELF:
                loop = self._lsg.create_new_loop(
                    node_w, self._type[w] != _BasicBlockClass.BB_IRREDUCIBLE
                )
                self._set_loop_attributes(w, node_pool, loop)

    def _step_e_process_non_back_preds(self, w, node_pool, work_list, x):
        def each(i):
            y = self._nodes[i]
            ydash = y.find_set()

            if not self._is_ancestor(w, ydash.get_dfs_number()):
                self._type[w] = _BasicBlockClass.BB_IRREDUCIBLE
                self._non_back_preds.at(w).add(ydash.get_dfs_number())
            else:
                if ydash.get_dfs_number() != w:
                    work_list.append(ydash)
                    node_pool.append(ydash)

        self._non_back_preds.at(x.get_dfs_number()).for_each(each)

    def _set_loop_attributes(self, w, node_pool, loop):
        self._nodes[w].set_loop(loop)

        def each(node):
            self._header[node.get_dfs_number()] = w
            node.union(self._nodes[w])

            if node.get_loop() is not None:
                node.get_loop().set_parent(loop)
            else:
                loop.add_node(node.get_bb())

        node_pool.for_each(each)

    def _step_d(self, w, node_pool):
        def each(v):
            if v != w:
                node_pool.append(self._nodes[v].find_set())
            else:
                self._type[w] = _BasicBlockClass.BB_SELF

        self._back_preds.at(w).for_each(each)
