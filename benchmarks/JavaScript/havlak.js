// Adapted based on SOM and Java benchmark.
//  Copyright 2011 Google Inc.
//
//      Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//      You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//      See the License for the specific language governing permissions and
//          limitations under the License.
'use strict';

var benchmark = require('./benchmark.js');
var som       = require('./som.js');

function Havlak() {
  benchmark.Benchmark.call(this);

  this.innerBenchmarkLoop = function (innerIterations) {
    return this.verifyResult((new LoopTesterApp()).main(
      innerIterations, 50, 10 /* was 100 */, 10, 5), innerIterations);
  };

  this.verifyResult = function (result, innerIterations) {
    if (innerIterations === 15000) { return result[0] === 46602 && result[1] === 5213; }
    if (innerIterations ===  1500) { return result[0] ===  6102 && result[1] === 5213; }
    if (innerIterations ===   150) { return result[0] ===  2052 && result[1] === 5213; }
    if (innerIterations ===    15) { return result[0] ===  1647 && result[1] === 5213; }
    if (innerIterations ===     1) { return result[0] ===  1605 && result[1] === 5213; }

    process.stdout.write("No verification result for " + innerIterations + " found");
    process.stdout.write("Result is: " + result[0] + ", " + result[1]);
    return false;
  };
}

function BasicBlock(name) {
  this.name     = name;
  this.inEdges  = new som.Vector(2);
  this.outEdges = new som.Vector(2);
}

BasicBlock.prototype.getInEdges = function () {
  return this.inEdges;
};

BasicBlock.prototype.getOutEdges = function () {
  return this.outEdges;
};

BasicBlock.prototype.getNumPred = function () {
  return this.inEdges.size();
};

BasicBlock.prototype.addOutEdge = function (to) {
  return this.outEdges.append(to);
};

BasicBlock.prototype.addInEdge = function (from) {
  return this.inEdges.append(from);
};

BasicBlock.prototype.customHash = function () {
  return this.name;
};

function BasicBlockEdge(cfg, fromName, toName) {
  this.from = cfg.createNode(fromName);
  this.to   = cfg.createNode(toName);

  this.from.addOutEdge(this.to);
  this.to.addInEdge(this.from);

  cfg.addEdge(this);
}

function ControlFlowGraph() {
  this.startNode     = null;
  this.basicBlockMap = new som.Vector();
  this.edgeList      = new som.Vector();
}

ControlFlowGraph.prototype.createNode = function (name) {
  var node;
  if (this.basicBlockMap.at(name)) {
    node = this.basicBlockMap.at(name);
  } else {
    node = new BasicBlock(name);
    this.basicBlockMap.atPut(name, node);
  }

  if (this.getNumNodes() === 1) {
    this.startNode = node;
  }
  return node;
};

ControlFlowGraph.prototype.addEdge = function (edge) {
  this.edgeList.append(edge);
};

ControlFlowGraph.prototype.getNumNodes = function () {
  return this.basicBlockMap.size();
};

ControlFlowGraph.prototype.getStartBasicBlock = function () {
  return this.startNode;
};

ControlFlowGraph.prototype.getBasicBlocks = function () {
  return this.basicBlockMap;
};

function LoopStructureGraph() {
  this.loopCounter = 0;
  this.loops = new som.Vector();
  this.root  = new SimpleLoop(null, true);
  this.root.setNestingLevel(0);
  this.root.setCounter(this.loopCounter);
  this.loopCounter += 1;
  this.loops.append(this.root);
}

LoopStructureGraph.prototype.createNewLoop = function (bb, isReducible) {
  var loop = new SimpleLoop(bb, isReducible);
  loop.setCounter(this.loopCounter);
  this.loopCounter += 1;
  this.loops.append(loop);
  return loop;
};

LoopStructureGraph.prototype.calculateNestingLevel = function () {
  // link up all 1st level loops to artificial root node.
  var that = this;
  this.loops.forEach(function (liter) {
    if (!liter.isRoot()) {
      if (!liter.getParent()) {
        liter.setParent(that.root);
      }
    }
  });

  // recursively traverse the tree and assign levels.
  this.calculateNestingLevelRec(this.root, 0);
};

LoopStructureGraph.prototype.calculateNestingLevelRec = function (loop, depth) {
  var that = this;
  loop.setDepthLevel(depth);
  loop.getChildren().forEach(function (liter) {
    that.calculateNestingLevelRec(liter, depth + 1);

    loop.setNestingLevel(
      Math.max(loop.getNestingLevel(),
        1 + liter.getNestingLevel()));
  });
};

LoopStructureGraph.prototype.getNumLoops = function () {
  return this.loops.size();
};


function SimpleLoop(bb, isReducible) {
  this.isReducible = isReducible;
  this.parent = null;
  this.isRoot_ = false;
  this.nestingLevel = 0;
  this.depthLevel   = 0;
  this.basicBlocks  = new som.IdentitySet();
  this.children     = new som.IdentitySet();

  if (bb) {
    this.basicBlocks.add(bb);
  }
  this.header = bb;
}

SimpleLoop.prototype.addNode = function (bb) {
  this.basicBlocks.add(bb);
};

SimpleLoop.prototype.addChildLoop = function (loop) {
  this.children.add(loop);
};

// Getters/Setters
SimpleLoop.prototype.getChildren = function () {
  return this.children;
};

SimpleLoop.prototype.getParent = function () {
  return this.parent;
};

SimpleLoop.prototype.getNestingLevel = function (){
  return this.nestingLevel;
};

SimpleLoop.prototype.isRoot = function () {
  return this.isRoot_;
};

SimpleLoop.prototype.setParent = function (parent) {
  this.parent = parent;
  this.parent.addChildLoop(this);
};

SimpleLoop.prototype.setIsRoot = function () {
  this.isRoot_ = true;
};

SimpleLoop.prototype.setCounter = function (value) {
  this.counter = value;
};

SimpleLoop.prototype.setNestingLevel = function (level) {
  this.nestingLevel = level;
  if (level === 0) {
    this.setIsRoot();
  }
};

SimpleLoop.prototype.setDepthLevel = function (level) {
  this.depthLevel = level;
};

function UnionFindNode() { /* no op */ }

// Initialize this node.
UnionFindNode.prototype.initNode = function (bb, dfsNumber) {
  this.parent     = this;
  this.bb         = bb;
  this.dfsNumber  = dfsNumber;
  this.loop       = null;
};

// Union/Find Algorithm - The find routine.
//
// Implemented with Path Compression (inner loops are only
// visited and collapsed once, however, deep nests would still
// result in significant traversals).
//
UnionFindNode.prototype.findSet = function () {
  var nodeList = new som.Vector(),
    node = this,
    that = this;
  while (node !== node.parent) {
    if (node.parent !== node.parent.parent) {
      nodeList.append(node);
    }
    node = node.parent;
  }

  // Path Compression, all nodes' parents point to the 1st level parent.
  nodeList.forEach(function (iter) { iter.union(that.parent); });
  return node;
};

// Union/Find Algorithm - The union routine.
//
// Trivial. Assigning parent pointer is enough,
// we rely on path compression.
//
UnionFindNode.prototype.union = function (basicBlock) {
  this.parent = basicBlock;
};

// Getters/Setters
//
UnionFindNode.prototype.getBb = function () {
  return this.bb;
};

UnionFindNode.prototype.getLoop = function () {
  return this.loop;
};

UnionFindNode.prototype.getDfsNumber = function () {
  return this.dfsNumber;
};

UnionFindNode.prototype.setLoop = function (loop) {
  this.loop = loop;
};

function LoopTesterApp() {
  this.cfg = new ControlFlowGraph();
  this.lsg = new LoopStructureGraph();
  this.cfg.createNode(0);
}

// Create 4 basic blocks, corresponding to and if/then/else clause
// with a CFG that looks like a diamond
LoopTesterApp.prototype.buildDiamond = function (start) {
  var bb0 = start;
  new BasicBlockEdge(this.cfg, bb0, bb0 + 1);
  new BasicBlockEdge(this.cfg, bb0, bb0 + 2);
  new BasicBlockEdge(this.cfg, bb0 + 1, bb0 + 3);
  new BasicBlockEdge(this.cfg, bb0 + 2, bb0 + 3);

  return bb0 + 3;
};

// Connect two existing nodes
LoopTesterApp.prototype.buildConnect = function (start, end) {
  new BasicBlockEdge(this.cfg, start, end);
};

// Form a straight connected sequence of n basic blocks
LoopTesterApp.prototype.buildStraight = function (start, n) {
  for (var i = 0; i < n; i++) {
    this.buildConnect(start + i, start + i + 1);
  }
  return start + n;
};

// Construct a simple loop with two diamonds in it
LoopTesterApp.prototype.buildBaseLoop = function (from) {
  var header = this.buildStraight(from, 1),
    diamond1 = this.buildDiamond(header),
    d11      = this.buildStraight(diamond1, 1),
    diamond2 = this.buildDiamond(d11),
    footer   = this.buildStraight(diamond2, 1);
  this.buildConnect(diamond2, d11);
  this.buildConnect(diamond1, header);

  this.buildConnect(footer, from);
  footer = this.buildStraight(footer, 1);
  return footer;
};

LoopTesterApp.prototype.main = function (numDummyLoops, findLoopIterations,
                                         parLoops, pparLoops, ppparLoops) {
  this.constructSimpleCFG();
  this.addDummyLoops(numDummyLoops);
  this.constructCFG(parLoops, pparLoops, ppparLoops);

  // Performing Loop Recognition, 1 Iteration, then findLoopIteration
  this.findLoops(this.lsg);
  for (var i = 0; i < findLoopIterations; i++) {
    this.findLoops(new LoopStructureGraph());
  }

  this.lsg.calculateNestingLevel();
  return [this.lsg.getNumLoops(), this.cfg.getNumNodes()];
};

LoopTesterApp.prototype.constructCFG = function (parLoops, pparLoops, ppparLoops) {
  var n = 2;

  for (var parlooptrees = 0; parlooptrees < parLoops; parlooptrees++) {
    this.cfg.createNode(n + 1);
    this.buildConnect(2, n + 1);
    n += 1;

    for (var i = 0; i < pparLoops; i++) {
      var top = n;
      n = this.buildStraight(n, 1);
      for (var j = 0; j < ppparLoops; j++) {
        n = this.buildBaseLoop(n);
      }
      var bottom = this.buildStraight(n, 1);
      this.buildConnect(n, top);
      n = bottom;
    }
    this.buildConnect(n, 1);
  }
};

LoopTesterApp.prototype.addDummyLoops = function (numDummyLoops) {
  for (var dummyloop = 0; dummyloop < numDummyLoops; dummyloop++) {
    this.findLoops(this.lsg);
  }
};

LoopTesterApp.prototype.findLoops = function (loopStructure) {
  var finder = new HavlakLoopFinder(this.cfg, loopStructure);
  finder.findLoops();
};

LoopTesterApp.prototype.constructSimpleCFG = function () {
  this.cfg.createNode(0);
  this.buildBaseLoop(0);
  this.cfg.createNode(1);
  new BasicBlockEdge(this.cfg, 0, 2);
};

var UNVISITED = 2147483647,       // Marker for uninitialized nodes.
  MAXNONBACKPREDS = (32 * 1024);  // Safeguard against pathological algorithm behavior.

function HavlakLoopFinder(cfg, lsg) {
  this.nonBackPreds = new som.Vector();
  this.backPreds  = new som.Vector();
  this.number = new som.IdentityDictionary();
  this.maxSize = 0;

  this.cfg = cfg;
  this.lsg = lsg;
}

// As described in the paper, determine whether a node 'w' is a
// "true" ancestor for node 'v'.
//
// Dominance can be tested quickly using a pre-order trick
// for depth-first spanning trees. This is why DFS is the first
// thing we run below.
HavlakLoopFinder.prototype.isAncestor = function (w, v) {
  return w <= v && v <= this.last[w];
};

// DFS - Depth-First-Search
//
// DESCRIPTION:
// Simple depth first traversal along out edges with node numbering.
HavlakLoopFinder.prototype.doDFS = function (currentNode, current) {
  this.nodes[current].initNode(currentNode, current);
  this.number.atPut(currentNode, current);

  var lastId = current,
    outerBlocks = currentNode.getOutEdges();

  for (var i = 0; i < outerBlocks.size(); i++) {
    var target = outerBlocks.at(i);
    if (this.number.at(target) == UNVISITED) {
      lastId = this.doDFS(target, lastId + 1);
    }
  }

  this.last[current] = lastId;
  return lastId;
};

HavlakLoopFinder.prototype.initAllNodes = function () {
  // Step a:
  //   - initialize all nodes as unvisited.
  //   - depth-first traversal and numbering.
  //   - unreached BB's are marked as dead.
  //
  var that = this;
  this.cfg.getBasicBlocks().forEach(
    function (bb) { that.number.atPut(bb, UNVISITED); });

  this.doDFS(this.cfg.getStartBasicBlock(), 0);
};

HavlakLoopFinder.prototype.identifyEdges = function (size) {
  // Step b:
  //   - iterate over all nodes.
  //
  //   A backedge comes from a descendant in the DFS tree, and non-backedges
  //   from non-descendants (following Tarjan).
  //
  //   - check incoming edges 'v' and add them to either
  //     - the list of backedges (backPreds) or
  //     - the list of non-backedges (nonBackPreds)
  for (var w = 0; w < size; w++) {
    this.header[w] = 0;
    this.type[w] = "BB_NONHEADER";

    var nodeW = this.nodes[w].getBb();
    if (!nodeW) {
      this.type[w] = "BB_DEAD";
    } else {
      this.processEdges(nodeW, w);
    }
  }
};

HavlakLoopFinder.prototype.processEdges = function (nodeW, w) {
  var that = this;

  if (nodeW.getNumPred() > 0) {
    nodeW.getInEdges().forEach(function (nodeV) {
      var v = that.number.at(nodeV);
      if (v != UNVISITED) {
        if (that.isAncestor(w, v)) {
          that.backPreds.at(w).append(v);
        } else {
          that.nonBackPreds.at(w).add(v);
        }
      }
    });
  }
};

// Find loops and build loop forest using Havlak's algorithm, which
// is derived from Tarjan. Variable names and step numbering has
// been chosen to be identical to the nomenclature in Havlak's
// paper (which, in turn, is similar to the one used by Tarjan).
HavlakLoopFinder.prototype.findLoops = function () {
  if (!this.cfg.getStartBasicBlock()) {
    return;
  }

  var size = this.cfg.getNumNodes();

  this.nonBackPreds.removeAll();
  this.backPreds.removeAll();
  this.number.removeAll();
  if (size > this.maxSize) {
    this.header  = new Array(size);
    this.type    = new Array(size);
    this.last    = new Array(size);
    this.nodes   = new Array(size);
    this.maxSize = size;
  }

  for (var i = 0; i < size; ++i) {
    this.nonBackPreds.append(new som.Set());
    this.backPreds.append(new som.Vector());
    this.nodes[i] = new UnionFindNode();
  }

  this.initAllNodes();
  this.identifyEdges(size);

  // Start node is root of all other loops.
  this.header[0] = 0;

  // Step c:
  //
  // The outer loop, unchanged from Tarjan. It does nothing except
  // for those nodes which are the destinations of backedges.
  // For a header node w, we chase backward from the sources of the
  // backedges adding nodes to the set P, representing the body of
  // the loop headed by w.
  //
  // By running through the nodes in reverse of the DFST preorder,
  // we ensure that inner loop headers will be processed before the
  // headers for surrounding loops.
  //
  for (var w = size - 1; w >= 0; w--) {
    // this is 'P' in Havlak's paper
    var nodePool = new som.Vector();

    var nodeW = this.nodes[w].getBb();
    if (nodeW) {
      this.stepD(w, nodePool);

      // Copy nodePool to workList.
      var workList = new som.Vector();
      nodePool.forEach(function (niter) { workList.append(niter); });

      if (nodePool.size() !== 0) {
        this.type[w] = "BB_REDUCIBLE";
      }

      // work the list...
      while (!workList.isEmpty()) {
        var x = workList.removeFirst();

        // Step e:
        //
        // Step e represents the main difference from Tarjan's method.
        // Chasing upwards from the sources of a node w's backedges. If
        // there is a node y' that is not a descendant of w, w is marked
        // the header of an irreducible loop, there is another entry
        // into this loop that avoids w.

        // The algorithm has degenerated. Break and
        // return in this case.
        var nonBackSize = this.nonBackPreds.at(x.getDfsNumber()).size();
        if (nonBackSize > MAXNONBACKPREDS) {
          return;
        }
        this.stepEProcessNonBackPreds(w, nodePool, workList, x);
      }

      // Collapse/Unionize nodes in a SCC to a single node
      // For every SCC found, create a loop descriptor and link it in.
      //
      if ((nodePool.size() > 0) || (this.type[w] === "BB_SELF")) {
        var loop = this.lsg.createNewLoop(nodeW, this.type[w] !== "BB_IRREDUCIBLE");
        this.setLoopAttributes(w, nodePool, loop);
      }
    }
  }  // Step c
};  // findLoops

HavlakLoopFinder.prototype.stepEProcessNonBackPreds = function (w, nodePool,
                                                                workList, x) {
  var that = this;
  this.nonBackPreds.at(x.getDfsNumber()).forEach(function (iter) {
    var y = that.nodes[iter],
      ydash = y.findSet();

    if (!that.isAncestor(w, ydash.getDfsNumber())) {
      that.type[w] = "BB_IRREDUCIBLE";
      that.nonBackPreds.at(w).add(ydash.getDfsNumber());
    } else {
      if (ydash.getDfsNumber() != w) {
        if (!nodePool.hasSome(function (e) { return e == ydash; })) {
          workList.append(ydash);
          nodePool.append(ydash);
        }
      }
    }
  });
};

HavlakLoopFinder.prototype.setLoopAttributes = function (w, nodePool, loop) {
  // At this point, one can set attributes to the loop, such as:
  //
  // the bottom node:
  //    iter  = backPreds[w].begin();
  //    loop bottom is: nodes[iter].node);
  //
  // the number of backedges:
  //    backPreds[w].size()
  //
  // whether this loop is reducible:
  //    type[w] != BasicBlockClass.BB_IRREDUCIBLE
  //
  this.nodes[w].setLoop(loop);
  var that = this;

  nodePool.forEach(function (node) {
    // Add nodes to loop descriptor.
    that.header[node.getDfsNumber()] = w;
    node.union(that.nodes[w]);

    // Nested loops are not added, but linked together.
    if (node.getLoop()) {
      node.getLoop().setParent(loop);
    } else {
      loop.addNode(node.getBb());
    }
  });
};

HavlakLoopFinder.prototype.stepD = function (w, nodePool) {
  var that = this;
  this.backPreds.at(w).forEach(function (v) {
    if (v != w) {
      nodePool.append(that.nodes[v].findSet());
    } else {
      that.type[w] = "BB_SELF";
    }
  });
};

exports.newInstance = function () {
  return new Havlak();
};
