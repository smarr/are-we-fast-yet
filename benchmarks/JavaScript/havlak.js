// @ts-check
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

const { Benchmark } = require('./benchmark');
const {
  Vector, Set, IdentitySet, IdentityDictionary
} = require('./som');

class BasicBlock {
  constructor(name) {
    this.name = name;
    this.inEdges = new Vector(2);
    this.outEdges = new Vector(2);
  }

  getInEdges() {
    return this.inEdges;
  }

  getOutEdges() {
    return this.outEdges;
  }

  getNumPred() {
    return this.inEdges.size();
  }

  addOutEdge(to) {
    return this.outEdges.append(to);
  }

  addInEdge(from) {
    return this.inEdges.append(from);
  }

  customHash() {
    return this.name;
  }
}

function BasicBlockEdge(cfg, fromName, toName) {
  this.from = cfg.createNode(fromName);
  this.to = cfg.createNode(toName);

  this.from.addOutEdge(this.to);
  this.to.addInEdge(this.from);

  cfg.addEdge(this);
}

class ControlFlowGraph {
  constructor() {
    this.startNode = null;
    this.basicBlockMap = new Vector();
    this.edgeList = new Vector();
  }

  createNode(name) {
    let node;
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
  }

  addEdge(edge) {
    this.edgeList.append(edge);
  }

  getNumNodes() {
    return this.basicBlockMap.size();
  }

  getStartBasicBlock() {
    return this.startNode;
  }

  getBasicBlocks() {
    return this.basicBlockMap;
  }
}

class SimpleLoop {
  constructor(bb, isReducible) {
    this.isReducible = isReducible;
    this.parent = null;
    this.isRoot_ = false;
    this.nestingLevel = 0;
    this.depthLevel = 0;
    this.basicBlocks = new IdentitySet();
    this.children = new IdentitySet();

    if (bb) {
      this.basicBlocks.add(bb);
    }
    this.header = bb;
  }

  addNode(bb) {
    this.basicBlocks.add(bb);
  }

  addChildLoop(loop) {
    this.children.add(loop);
  }

  // Getters/Setters
  getChildren() {
    return this.children;
  }

  getParent() {
    return this.parent;
  }

  getNestingLevel() {
    return this.nestingLevel;
  }

  isRoot() {
    return this.isRoot_;
  }

  setParent(parent) {
    this.parent = parent;
    this.parent.addChildLoop(this);
  }

  setIsRoot() {
    this.isRoot_ = true;
  }

  setCounter(value) {
    this.counter = value;
  }

  setNestingLevel(level) {
    this.nestingLevel = level;
    if (level === 0) {
      this.setIsRoot();
    }
  }

  setDepthLevel(level) {
    this.depthLevel = level;
  }
}

class LoopStructureGraph {
  constructor() {
    this.loopCounter = 0;
    this.loops = new Vector();
    this.root = new SimpleLoop(null, true);
    this.root.setNestingLevel(0);
    this.root.setCounter(this.loopCounter);
    this.loopCounter += 1;
    this.loops.append(this.root);
  }

  createNewLoop(bb, isReducible) {
    const loop = new SimpleLoop(bb, isReducible);
    loop.setCounter(this.loopCounter);
    this.loopCounter += 1;
    this.loops.append(loop);
    return loop;
  }

  calculateNestingLevel() {
    // link up all 1st level loops to artificial root node.
    this.loops.forEach((liter) => {
      if (!liter.isRoot()) {
        if (!liter.getParent()) {
          liter.setParent(this.root);
        }
      }
    });

    // recursively traverse the tree and assign levels.
    this.calculateNestingLevelRec(this.root, 0);
  }

  calculateNestingLevelRec(loop, depth) {
    loop.setDepthLevel(depth);
    loop.getChildren().forEach((liter) => {
      this.calculateNestingLevelRec(liter, depth + 1);

      loop.setNestingLevel(
        Math.max(
          loop.getNestingLevel(),
          1 + liter.getNestingLevel()
        )
      );
    });
  }

  getNumLoops() {
    return this.loops.size();
  }
}

class UnionFindNode {
  // Initialize this node.
  initNode(bb, dfsNumber) {
    this.parent = this;
    this.bb = bb;
    this.dfsNumber = dfsNumber;
    this.loop = null;
  }

  // Union/Find Algorithm - The find routine.
  //
  // Implemented with Path Compression (inner loops are only
  // visited and collapsed once, however, deep nests would still
  // result in significant traversals).
  //
  findSet() {
    const nodeList = new Vector();
    let node = this;

    while (node !== node.parent) {
      if (node.parent !== node.parent.parent) {
        nodeList.append(node);
      }
      node = node.parent;
    }

    // Path Compression, all nodes' parents point to the 1st level parent.
    nodeList.forEach((iter) => iter.union(this.parent));
    return node;
  }

  // Union/Find Algorithm - The union routine.
  //
  // Trivial. Assigning parent pointer is enough,
  // we rely on path compression.
  //
  union(basicBlock) {
    this.parent = basicBlock;
  }

  // Getters/Setters
  //
  getBb() {
    return this.bb;
  }

  getLoop() {
    return this.loop;
  }

  getDfsNumber() {
    return this.dfsNumber;
  }

  setLoop(loop) {
    this.loop = loop;
  }
}

const UNVISITED = 2147483647; // Marker for uninitialized nodes.
const MAXNONBACKPREDS = (32 * 1024); // Safeguard against pathological algorithm behavior.

class HavlakLoopFinder {
  constructor(cfg, lsg) {
    this.nonBackPreds = new Vector();
    this.backPreds = new Vector();
    this.number = new IdentityDictionary();
    this.maxSize = 0;

    this.header = null;
    this.type = null;
    this.last = null;
    this.nodes = null;

    this.cfg = cfg;
    this.lsg = lsg;
  }

  // As described in the paper, determine whether a node 'w' is a
  // "true" ancestor for node 'v'.
  //
  // Dominance can be tested quickly using a pre-order trick
  // for depth-first spanning trees. This is why DFS is the first
  // thing we run below.
  isAncestor(w, v) {
    return w <= v && v <= this.last[w];
  }

  // DFS - Depth-First-Search
  //
  // DESCRIPTION:
  // Simple depth first traversal along out edges with node numbering.
  doDFS(currentNode, current) {
    this.nodes[current].initNode(currentNode, current);
    this.number.atPut(currentNode, current);

    let lastId = current;
    const outerBlocks = currentNode.getOutEdges();

    for (let i = 0; i < outerBlocks.size(); i += 1) {
      const target = outerBlocks.at(i);
      if (this.number.at(target) === UNVISITED) {
        lastId = this.doDFS(target, lastId + 1);
      }
    }

    this.last[current] = lastId;
    return lastId;
  }

  initAllNodes() {
    // Step a:
    //   - initialize all nodes as unvisited.
    //   - depth-first traversal and numbering.
    //   - unreached BB's are marked as dead.
    this.cfg.getBasicBlocks().forEach(
      (bb) => { this.number.atPut(bb, UNVISITED); }
    );

    this.doDFS(this.cfg.getStartBasicBlock(), 0);
  }

  identifyEdges(size) {
    // Step b:
    //   - iterate over all nodes.
    //
    //   A backedge comes from a descendant in the DFS tree, and non-backedges
    //   from non-descendants (following Tarjan).
    //
    //   - check incoming edges 'v' and add them to either
    //     - the list of backedges (backPreds) or
    //     - the list of non-backedges (nonBackPreds)
    for (let w = 0; w < size; w += 1) {
      this.header[w] = 0;
      this.type[w] = 'BB_NONHEADER';

      const nodeW = this.nodes[w].getBb();
      if (!nodeW) {
        this.type[w] = 'BB_DEAD';
      } else {
        this.processEdges(nodeW, w);
      }
    }
  }

  processEdges(nodeW, w) {
    if (nodeW.getNumPred() > 0) {
      nodeW.getInEdges().forEach((nodeV) => {
        const v = this.number.at(nodeV);
        if (v !== UNVISITED) {
          if (this.isAncestor(w, v)) {
            this.backPreds.at(w).append(v);
          } else {
            this.nonBackPreds.at(w).add(v);
          }
        }
      });
    }
  }

  // Find loops and build loop forest using Havlak's algorithm, which
  // is derived from Tarjan. Variable names and step numbering has
  // been chosen to be identical to the nomenclature in Havlak's
  // paper (which, in turn, is similar to the one used by Tarjan).
  findLoops() {
    if (!this.cfg.getStartBasicBlock()) {
      return;
    }

    const size = this.cfg.getNumNodes();

    this.nonBackPreds.removeAll();
    this.backPreds.removeAll();
    this.number.removeAll();
    if (size > this.maxSize) {
      this.header = new Array(size);
      this.type = new Array(size);
      this.last = new Array(size);
      this.nodes = new Array(size);
      this.maxSize = size;
    }

    for (let i = 0; i < size; i += 1) {
      this.nonBackPreds.append(new Set());
      this.backPreds.append(new Vector());
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
    for (let w = size - 1; w >= 0; w -= 1) {
      // this is 'P' in Havlak's paper
      const nodePool = new Vector();

      const nodeW = this.nodes[w].getBb();
      if (nodeW) {
        this.stepD(w, nodePool);

        // Copy nodePool to workList.
        const workList = new Vector();
        nodePool.forEach((niter) => workList.append(niter));

        if (nodePool.size() !== 0) {
          this.type[w] = 'BB_REDUCIBLE';
        }

        // work the list...
        while (!workList.isEmpty()) {
          const x = workList.removeFirst();

          // Step e:
          //
          // Step e represents the main difference from Tarjan's method.
          // Chasing upwards from the sources of a node w's backedges. If
          // there is a node y' that is not a descendant of w, w is marked
          // the header of an irreducible loop, there is another entry
          // into this loop that avoids w.

          // The algorithm has degenerated. Break and
          // return in this case.
          const nonBackSize = this.nonBackPreds.at(x.getDfsNumber()).size();
          if (nonBackSize > MAXNONBACKPREDS) {
            return;
          }
          this.stepEProcessNonBackPreds(w, nodePool, workList, x);
        }

        // Collapse/Unionize nodes in a SCC to a single node
        // For every SCC found, create a loop descriptor and link it in.
        //
        if ((nodePool.size() > 0) || (this.type[w] === 'BB_SELF')) {
          const loop = this.lsg.createNewLoop(nodeW, this.type[w] !== 'BB_IRREDUCIBLE');
          this.setLoopAttributes(w, nodePool, loop);
        }
      }
    } // Step c
  } // findLoops

  stepEProcessNonBackPreds(w, nodePool, workList, x) {
    this.nonBackPreds.at(x.getDfsNumber()).forEach((iter) => {
      const y = this.nodes[iter];
      const ydash = y.findSet();

      if (!this.isAncestor(w, ydash.getDfsNumber())) {
        this.type[w] = 'BB_IRREDUCIBLE';
        this.nonBackPreds.at(w).add(ydash.getDfsNumber());
      } else if (ydash.getDfsNumber() !== w) {
        if (!nodePool.hasSome((e) => e === ydash)) {
          workList.append(ydash);
          nodePool.append(ydash);
        }
      }
    });
  }

  setLoopAttributes(w, nodePool, loop) {
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

    nodePool.forEach((node) => {
      // Add nodes to loop descriptor.
      this.header[node.getDfsNumber()] = w;
      node.union(this.nodes[w]);

      // Nested loops are not added, but linked together.
      if (node.getLoop()) {
        node.getLoop().setParent(loop);
      } else {
        loop.addNode(node.getBb());
      }
    });
  }

  stepD(w, nodePool) {
    this.backPreds.at(w).forEach((v) => {
      if (v !== w) {
        nodePool.append(this.nodes[v].findSet());
      } else {
        this.type[w] = 'BB_SELF';
      }
    });
  }
}

class LoopTesterApp {
  constructor() {
    this.cfg = new ControlFlowGraph();
    this.lsg = new LoopStructureGraph();
    this.cfg.createNode(0);
  }

  // Create 4 basic blocks, corresponding to and if/then/else clause
  // with a CFG that looks like a diamond
  buildDiamond(start) {
    const bb0 = start;
    new BasicBlockEdge(this.cfg, bb0, bb0 + 1);
    new BasicBlockEdge(this.cfg, bb0, bb0 + 2);
    new BasicBlockEdge(this.cfg, bb0 + 1, bb0 + 3);
    new BasicBlockEdge(this.cfg, bb0 + 2, bb0 + 3);

    return bb0 + 3;
  }

  // Connect two existing nodes
  buildConnect(start, end) {
    new BasicBlockEdge(this.cfg, start, end);
  }

  // Form a straight connected sequence of n basic blocks
  buildStraight(start, n) {
    for (let i = 0; i < n; i += 1) {
      this.buildConnect(start + i, start + i + 1);
    }
    return start + n;
  }

  // Construct a simple loop with two diamonds in it
  buildBaseLoop(from) {
    const header = this.buildStraight(from, 1);
    const diamond1 = this.buildDiamond(header);
    const d11 = this.buildStraight(diamond1, 1);
    const diamond2 = this.buildDiamond(d11);

    let footer = this.buildStraight(diamond2, 1);
    this.buildConnect(diamond2, d11);
    this.buildConnect(diamond1, header);

    this.buildConnect(footer, from);
    footer = this.buildStraight(footer, 1);
    return footer;
  }

  main(numDummyLoops, findLoopIterations, parLoops, pparLoops, ppparLoops) {
    this.constructSimpleCFG();
    this.addDummyLoops(numDummyLoops);
    this.constructCFG(parLoops, pparLoops, ppparLoops);

    // Performing Loop Recognition, 1 Iteration, then findLoopIteration
    this.findLoops(this.lsg);
    for (let i = 0; i < findLoopIterations; i += 1) {
      this.findLoops(new LoopStructureGraph());
    }

    this.lsg.calculateNestingLevel();
    return [this.lsg.getNumLoops(), this.cfg.getNumNodes()];
  }

  constructCFG(parLoops, pparLoops, ppparLoops) {
    let n = 2;

    for (let parlooptrees = 0; parlooptrees < parLoops; parlooptrees += 1) {
      this.cfg.createNode(n + 1);
      this.buildConnect(2, n + 1);
      n += 1;

      for (let i = 0; i < pparLoops; i += 1) {
        const top = n;
        n = this.buildStraight(n, 1);
        for (let j = 0; j < ppparLoops; j += 1) {
          n = this.buildBaseLoop(n);
        }
        const bottom = this.buildStraight(n, 1);
        this.buildConnect(n, top);
        n = bottom;
      }
      this.buildConnect(n, 1);
    }
  }

  addDummyLoops(numDummyLoops) {
    for (let dummyloop = 0; dummyloop < numDummyLoops; dummyloop += 1) {
      this.findLoops(this.lsg);
    }
  }

  findLoops(loopStructure) {
    const finder = new HavlakLoopFinder(this.cfg, loopStructure);
    finder.findLoops();
  }

  constructSimpleCFG() {
    this.cfg.createNode(0);
    this.buildBaseLoop(0);
    this.cfg.createNode(1);
    new BasicBlockEdge(this.cfg, 0, 2);
  }
}

class Havlak extends Benchmark {
  innerBenchmarkLoop(innerIterations) {
    return this.verifyResult(
      (new LoopTesterApp()).main(
        innerIterations,
        50,
        10 /* was 100 */,
        10,
        5
      ),
      innerIterations
    );
  }

  verifyResult(result, innerIterations) {
    if (innerIterations === 15000) { return result[0] === 46602 && result[1] === 5213; }
    if (innerIterations === 1500) { return result[0] === 6102 && result[1] === 5213; }
    if (innerIterations === 150) { return result[0] === 2052 && result[1] === 5213; }
    if (innerIterations === 15) { return result[0] === 1647 && result[1] === 5213; }
    if (innerIterations === 1) { return result[0] === 1605 && result[1] === 5213; }

    process.stdout.write(`No verification result for ${innerIterations} found`);
    process.stdout.write(`Result is: ${result[0]}, ${result[1]}`);
    return false;
  }
}

exports.newInstance = () => new Havlak();
