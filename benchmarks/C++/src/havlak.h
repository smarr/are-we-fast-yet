#pragma once

#include <any>
#include <cstdint>
#include <limits>

#include "som/error.h"
#include "som/identity_set.h"

using std::cout;

class BasicBlock {
 private:
  Vector<BasicBlock*> _inEdges{2};
  Vector<BasicBlock*> _outEdges{2};
  uint32_t _name;

 public:
  explicit BasicBlock(int32_t name) : _name(name) {}

  Vector<BasicBlock*>& getInEdges() { return _inEdges; }
  Vector<BasicBlock*>& getOutEdges() { return _outEdges; }
  int32_t getNumPred() { return static_cast<int32_t>(_inEdges.size()); }
  void addOutEdge(BasicBlock* to) { _outEdges.append(to); }
  void addInEdge(BasicBlock* from) { _inEdges.append(from); }
  [[nodiscard]] uint32_t customHash() const { return _name; }

  bool equal(BasicBlock* other) const { return _name == other->_name; }
};

class BasicBlockEdge;

class ControlFlowGraph {
 private:
  Vector<BasicBlock*> _basicBlockMap{};
  BasicBlock* _startNode{nullptr};
  Vector<BasicBlockEdge*> _edgeList{};

 public:
  ControlFlowGraph() = default;

  ~ControlFlowGraph() {
    _basicBlockMap.destroyValues();
    _edgeList.destroyValues();
  }

  BasicBlock* createNode(int32_t name) {
    BasicBlock* node = nullptr;
    if (_basicBlockMap.at(name) != nullptr) {
      node = *_basicBlockMap.at(name);
    } else {
      node = new BasicBlock(name);
      _basicBlockMap.atPut(name, node);
    }

    if (getNumNodes() == 1) {
      _startNode = node;
    }
    return node;
  }

  void addEdge(BasicBlockEdge* edge) { _edgeList.append(edge); }
  int32_t getNumNodes() { return static_cast<int32_t>(_basicBlockMap.size()); }
  BasicBlock* getStartBasicBlock() { return _startNode; }
  Vector<BasicBlock*>& getBasicBlocks() { return _basicBlockMap; }
};

class BasicBlockEdge {
 private:
  BasicBlock* _from;
  BasicBlock* _to;

 public:
  BasicBlockEdge(ControlFlowGraph& cfg, int32_t fromName, int32_t toName)
      : _from(cfg.createNode(fromName)), _to(cfg.createNode(toName)) {
    _from->addOutEdge(_to);
    _to->addInEdge(_from);

    cfg.addEdge(this);
  }
};

class SimpleLoop {
 private:
  IdentitySet<BasicBlock*> _basicBlocks{};
  IdentitySet<SimpleLoop*> _children{};
  SimpleLoop* _parent{nullptr};
  BasicBlock* _header;
  bool _isReducible;
  bool _isRoot{false};
  int32_t _nestingLevel{0};
  int32_t _counter{0};
  int32_t _depthLevel{0};

  void addChildLoop(SimpleLoop* loop) { _children.add(loop); }

 public:
  SimpleLoop(BasicBlock* bb, bool isReducible)
      : _header(bb), _isReducible(isReducible) {
    if (bb != nullptr) {
      _basicBlocks.add(bb);
    }
  }

  bool equal(SimpleLoop* other) const { return this == other; }

  void addNode(BasicBlock* bb) { _basicBlocks.add(bb); }

  [[nodiscard]] IdentitySet<SimpleLoop*>& getChildren() { return _children; }
  [[nodiscard]] SimpleLoop* getParent() const { return _parent; }
  [[nodiscard]] int32_t getNestingLevel() const { return _nestingLevel; }
  [[nodiscard]] bool isRoot() const { return _isRoot; }

  void setParent(SimpleLoop* parent) {
    _parent = parent;
    _parent->addChildLoop(this);
  }

  void setIsRoot() { _isRoot = true; }
  void setCounter(int32_t value) { _counter = value; }

  void setNestingLevel(int32_t level) {
    _nestingLevel = level;
    if (level == 0) {
      setIsRoot();
    }
  }

  void setDepthLevel(int32_t level) { _depthLevel = level; }
};

class LoopStructureGraph {
 private:
  SimpleLoop* _root;
  Vector<SimpleLoop*> _loops{};
  int32_t _loopCounter{0};

 public:
  LoopStructureGraph() : _root(new SimpleLoop(nullptr, true)) {
    _root->setNestingLevel(0);
    _root->setCounter(_loopCounter);
    _loopCounter += 1;
    _loops.append(_root);
  }

  ~LoopStructureGraph() { _loops.destroyValues(); }

  [[nodiscard]] SimpleLoop* createNewLoop(BasicBlock* bb, bool isReducible) {
    auto* loop = new SimpleLoop(bb, isReducible);
    loop->setCounter(_loopCounter);
    _loopCounter += 1;
    _loops.append(loop);
    return loop;
  }

  void calculateNestingLevel() {
    // link up all 1st level loops to artificial root node.
    _loops.forEach([this](SimpleLoop* const& liter) -> void {
      if (!liter->isRoot()) {
        if (liter->getParent() == nullptr) {
          liter->setParent(_root);
        }
      }
    });

    // recursively traverse the tree and assign levels.
    calculateNestingLevelRec(_root, 0);
  }

  void calculateNestingLevelRec(SimpleLoop* loop, int32_t depth) {
    loop->setDepthLevel(depth);
    loop->getChildren().forEach(
        [this, loop, depth](SimpleLoop* const& liter) -> void {
          calculateNestingLevelRec(liter, depth + 1);

          loop->setNestingLevel(
              std::max(loop->getNestingLevel(), 1 + liter->getNestingLevel()));
        });
  }

  int32_t getNumLoops() { return static_cast<int32_t>(_loops.size()); }
};

class UnionFindNode {
 private:
  UnionFindNode* _parent;
  BasicBlock* _bb;
  SimpleLoop* _loop;
  int32_t _dfsNumber;

 public:
  UnionFindNode() = default;

  void initNode(BasicBlock* bb, int32_t dfsNumber) {
    _parent = this;
    _bb = bb;
    _dfsNumber = dfsNumber;
    _loop = nullptr;
  }

  UnionFindNode* findSet() {
    Vector<UnionFindNode*> nodeList{};

    UnionFindNode* node = this;
    while (node != node->_parent) {
      if (node->_parent != node->_parent->_parent) {
        nodeList.append(node);
      }
      node = node->_parent;
    }

    // Path Compression, all nodes' parents point to the 1st level parent.
    nodeList.forEach([this](UnionFindNode* const& iter) -> void {
      iter->unionSet(_parent);
    });
    return node;
  }

  void unionSet(UnionFindNode* basicBlock) { _parent = basicBlock; }
  [[nodiscard]] BasicBlock* getBb() const { return _bb; }
  [[nodiscard]] SimpleLoop* getLoop() const { return _loop; }
  [[nodiscard]] int32_t getDfsNumber() const { return _dfsNumber; }
  void setLoop(SimpleLoop* loop) { _loop = loop; }
};

class HavlakLoopFinder {
 private:
  enum BasicBlockClass {
    BB_TOP,          // uninitialized
    BB_NONHEADER,    // a regular BB
    BB_REDUCIBLE,    // reducible loop
    BB_SELF,         // single BB loop
    BB_IRREDUCIBLE,  // irreducible loop
    BB_DEAD,         // a dead BB
    BB_LAST          // Sentinel
  };

  ControlFlowGraph& _cfg;    // Control Flow Graph
  LoopStructureGraph& _lsg;  // Loop Structure Graph

  static constexpr int32_t UNVISITED = std::numeric_limits<int32_t>::max();
  static constexpr int32_t MAXNONBACKPREDS = static_cast<int32_t>(32 * 1024);

  Vector<Set<int32_t>*> _nonBackPreds{};
  Vector<Vector<int32_t>*> _backPreds{};
  IdentityDictionary<BasicBlock, int32_t> _number{};

  int32_t _maxSize{0};
  int32_t* _header{nullptr};
  BasicBlockClass* _type{nullptr};
  int32_t* _last{nullptr};
  UnionFindNode** _nodes{nullptr};

 public:
  HavlakLoopFinder(ControlFlowGraph& cfg, LoopStructureGraph& lsg)
      : _cfg(cfg), _lsg(lsg) {}

  ~HavlakLoopFinder() {
    _nonBackPreds.destroyValues();
    _backPreds.destroyValues();
    delete[] _header;
    delete[] _type;
    delete[] _last;

    for (int32_t i = 0; i < _maxSize; i += 1) {
      delete _nodes[i];
    }
    delete[] _nodes;
  }

  void findLoops() {
    if (_cfg.getStartBasicBlock() == nullptr) {
      return;
    }

    const int32_t size = _cfg.getNumNodes();

    _nonBackPreds.removeAll();
    _backPreds.removeAll();
    _number.removeAll();
    if (size > _maxSize) {
      _header = new int32_t[size];
      _type = new BasicBlockClass[size];
      _last = new int32_t[size];
      _nodes = new UnionFindNode*[size];
      _maxSize = size;
    }

    for (int32_t i = 0; i < size; i += 1) {
      _nonBackPreds.append(new Set<int32_t>());
      _backPreds.append(new Vector<int32_t>());
      _nodes[i] = new UnionFindNode();
    }

    initAllNodes();
    identifyEdges(size);

    // Start node is root of all other loops.
    _header[0] = 0;
    for (int32_t w = size - 1; w >= 0; w -= 1) {
      // this is 'P' in Havlak's paper
      Vector<UnionFindNode*> nodePool{};

      BasicBlock* nodeW = _nodes[w]->getBb();
      if (nodeW != nullptr) {
        stepD(w, nodePool);

        // Copy nodePool to workList.
        Vector<UnionFindNode*> workList{};
        nodePool.forEach([&workList](UnionFindNode* const& niter) -> void {
          workList.append(niter);
        });

        if (nodePool.size() != 0) {
          _type[w] = BB_REDUCIBLE;
        }

        // work the list...
        //
        while (!workList.isEmpty()) {
          UnionFindNode* x = workList.removeFirst();

          const auto nonBackSize = static_cast<int32_t>(
              (*_nonBackPreds.at(x->getDfsNumber()))->size());
          if (nonBackSize > MAXNONBACKPREDS) {
            return;
          }
          stepEProcessNonBackPreds(w, nodePool, workList, x);
        }

        if ((nodePool.size() > 0) || (_type[w] == BB_SELF)) {
          SimpleLoop* loop =
              _lsg.createNewLoop(nodeW, _type[w] != BB_IRREDUCIBLE);
          setLoopAttributes(w, nodePool, loop);
        }
      }
    }  // Step c
  }    // findLoops

 private:
  bool isAncestor(int32_t w, int32_t v) { return w <= v && v <= _last[w]; }

  int32_t doDFS(BasicBlock* currentNode, int32_t current) {
    _nodes[current]->initNode(currentNode, current);
    _number.atPut(currentNode, current);

    int32_t lastId = current;
    const Vector<BasicBlock*>& outerBlocks = currentNode->getOutEdges();

    for (int32_t i = 0; i < static_cast<int32_t>(outerBlocks.size()); i += 1) {
      BasicBlock* target = *outerBlocks.at(i);
      if (*_number.at(target) == UNVISITED) {
        lastId = doDFS(target, lastId + 1);
      }
    }

    _last[current] = lastId;
    return lastId;
  }

  void initAllNodes() {
    _cfg.getBasicBlocks().forEach([this](BasicBlock* const& bb) -> void {
      _number.atPut(bb, UNVISITED);
    });
    doDFS(_cfg.getStartBasicBlock(), 0);
  }

  void identifyEdges(int32_t size) {
    for (int32_t w = 0; w < size; w += 1) {
      _header[w] = 0;
      _type[w] = BB_NONHEADER;

      BasicBlock* nodeW = _nodes[w]->getBb();
      if (nodeW == nullptr) {
        _type[w] = BB_DEAD;
      } else {
        processEdges(nodeW, w);
      }
    }
  }

  void processEdges(BasicBlock* nodeW, int32_t w) {
    if (nodeW->getNumPred() > 0) {
      nodeW->getInEdges().forEach([this, w](BasicBlock* const& nodeV) -> void {
        const int32_t v = *_number.at(nodeV);
        if (v != UNVISITED) {
          if (isAncestor(w, v)) {
            (*_backPreds.at(w))->append(v);
          } else {
            (*_nonBackPreds.at(w))->add(v);
          }
        }
      });
    }
  }

  void stepEProcessNonBackPreds(int32_t w,
                                Vector<UnionFindNode*>& nodePool,
                                Vector<UnionFindNode*>& workList,
                                UnionFindNode* x) {
    (*_nonBackPreds.at(x->getDfsNumber()))
        ->forEach([this, w, &nodePool, &workList](int32_t const& iter) -> void {
          UnionFindNode* y = _nodes[iter];
          UnionFindNode* ydash = y->findSet();

          if (!isAncestor(w, ydash->getDfsNumber())) {
            _type[w] = BB_IRREDUCIBLE;
            (*_nonBackPreds.at(w))->add(ydash->getDfsNumber());
          } else {
            if (ydash->getDfsNumber() != w) {
              if (!nodePool.hasSome([ydash](UnionFindNode* const& e) -> bool {
                    return e == ydash;
                  })) {
                workList.append(ydash);
                nodePool.append(ydash);
              }
            }
          }
        });
  }

  void setLoopAttributes(int32_t w,
                         Vector<UnionFindNode*>& nodePool,
                         SimpleLoop* loop) {
    _nodes[w]->setLoop(loop);

    nodePool.forEach([this, w, loop](UnionFindNode* const& node) -> void {
      _header[node->getDfsNumber()] = w;
      node->unionSet(_nodes[w]);

      // Nested loops are not added, but linked together.
      if (node->getLoop() != nullptr) {
        node->getLoop()->setParent(loop);
      } else {
        loop->addNode(node->getBb());
      }
    });
  }

  void stepD(int32_t w, Vector<UnionFindNode*>& nodePool) {
    (*_backPreds.at(w))
        ->forEach([this, w, &nodePool](int32_t const& v) -> void {
          if (v != w) {
            nodePool.append(_nodes[v]->findSet());
          } else {
            _type[w] = BB_SELF;
          }
        });
  }
};

class LoopTesterApp {
 private:
  ControlFlowGraph _cfg{};
  LoopStructureGraph _lsg{};

  int32_t buildDiamond(int32_t start) {
    const int32_t bb0 = start;
    new BasicBlockEdge(_cfg, bb0, bb0 + 1);
    new BasicBlockEdge(_cfg, bb0, bb0 + 2);
    new BasicBlockEdge(_cfg, bb0 + 1, bb0 + 3);
    new BasicBlockEdge(_cfg, bb0 + 2, bb0 + 3);

    return bb0 + 3;
  }

  void buildConnect(int32_t start, int32_t end) {
    new BasicBlockEdge(_cfg, start, end);
  }

  int32_t buildStraight(int32_t start, int32_t n) {
    for (int32_t i = 0; i < n; i += 1) {
      buildConnect(start + i, start + i + 1);
    }
    return start + n;
  }

  int32_t buildBaseLoop(int32_t from) {
    const int32_t header = buildStraight(from, 1);
    const int32_t diamond1 = buildDiamond(header);
    const int32_t d11 = buildStraight(diamond1, 1);
    const int32_t diamond2 = buildDiamond(d11);
    int32_t footer = buildStraight(diamond2, 1);
    buildConnect(diamond2, d11);
    buildConnect(diamond1, header);
    buildConnect(footer, from);
    footer = buildStraight(footer, 1);
    return footer;
  }

  void constructSimpleCFG() {
    _cfg.createNode(0);
    buildBaseLoop(0);
    _cfg.createNode(1);

    new BasicBlockEdge(_cfg, 0, 2);
  }

 public:
  LoopTesterApp() { _cfg.createNode(0); }

  std::array<int32_t, 2> main(int32_t numDummyLoops,
                              int32_t findLoopIterations,
                              int32_t parLoops,
                              int32_t pparLoops,
                              int32_t ppparLoops) {
    constructSimpleCFG();
    addDummyLoops(numDummyLoops);
    constructCFG(parLoops, pparLoops, ppparLoops);
    findLoops(_lsg);
    for (int32_t i = 0; i < findLoopIterations; i += 1) {
      LoopStructureGraph l{};
      findLoops(l);
    }

    _lsg.calculateNestingLevel();
    return {_lsg.getNumLoops(), _cfg.getNumNodes()};
  }

  void constructCFG(int32_t parLoops, int32_t pparLoops, int32_t ppparLoops) {
    int32_t n = 2;

    for (int32_t parlooptrees = 0; parlooptrees < parLoops; parlooptrees += 1) {
      _cfg.createNode(n + 1);
      buildConnect(2, n + 1);
      n += 1;

      for (int32_t i = 0; i < pparLoops; i += 1) {
        const int32_t top = n;
        n = buildStraight(n, 1);
        for (int32_t j = 0; j < ppparLoops; j += 1) {
          n = buildBaseLoop(n);
        }
        const int32_t bottom = buildStraight(n, 1);
        buildConnect(n, top);
        n = bottom;
      }
      buildConnect(n, 1);
    }
  }

  void addDummyLoops(int32_t numDummyLoops) {
    for (int32_t dummyloop = 0; dummyloop < numDummyLoops; dummyloop += 1) {
      findLoops(_lsg);
    }
  }

  void findLoops(LoopStructureGraph& loopStructure) {
    HavlakLoopFinder finder(_cfg, loopStructure);
    finder.findLoops();
  }
};

class Havlak : public Benchmark {
 public:
  bool inner_benchmark_loop(int32_t inner_iterations) override {
    const bool result = verifyResult(
        LoopTesterApp().main(inner_iterations, 50, 10 /* was 100 */, 10, 5),
        inner_iterations);
    return result;
  }

  bool verifyResult(std::array<int32_t, 2> r, int32_t innerIterations) {
    if (innerIterations == 15000) {
      return r[0] == 46602 && r[1] == 5213;
    }
    if (innerIterations == 1500) {
      return r[0] == 6102 && r[1] == 5213;
    }
    if (innerIterations == 150) {
      return r[0] == 2052 && r[1] == 5213;
    }
    if (innerIterations == 15) {
      return r[0] == 1647 && r[1] == 5213;
    }
    if (innerIterations == 1) {
      return r[0] == 1605 && r[1] == 5213;
    }

    cout << "No verification result for " << innerIterations << " found\n";
    cout << "Result is: " << r[0] << ", " << r[1] << "\n";

    return false;
  }

  std::any benchmark() override { throw Error("Should never be reached"); }

  bool verify_result(std::any) override {
    throw Error("Should never be reached");
  }
};
