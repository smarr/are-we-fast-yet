/* Copyright (c) 2001-2016 Stefan Marr
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.*/
// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "Havlak.h"
#include "som/Vector.h"
#include "som/Set.h"
#include "som/Dictionary.h"
#include <utility>
#include <iostream>
#include <cassert>
using namespace som;

class BasicBlock {
    BasicBlock(const BasicBlock& rhs);
public:

    Vector<BasicBlock*> inEdges;
    Vector<BasicBlock*> outEdges;
    int name;

    BasicBlock(int name) {
        this->name = name;
    }

    Vector<BasicBlock*>& getInEdges() {
        return inEdges;
    }

    const Vector<BasicBlock*>& getOutEdges() const {
        return outEdges;
    }

    int getNumPred() const {
        return inEdges.size();
    }

    void addOutEdge(BasicBlock* to) {
        outEdges.append(to);
    }

    void addInEdge(BasicBlock* from) {
        inEdges.append(from);
    }

    class Hash
    {
    public:
        int operator()(BasicBlock* b) {
            return b->name;
        }
    };
};

class ControlFlowGraph;

class BasicBlockEdge {

    BasicBlock* from;
    BasicBlock* to;

    BasicBlockEdge();
    BasicBlockEdge(const BasicBlockEdge&);
public:
    BasicBlockEdge(ControlFlowGraph* cfg, int fromName, int toName);
};

class ControlFlowGraph {

    Vector<BasicBlock*>  basicBlockMap;
    BasicBlock* startNode;
    Vector<BasicBlockEdge*> edgeList;

public:
    ControlFlowGraph() {
        startNode = 0;
    }
    ~ControlFlowGraph()
    {
        for( int i = 0; i < basicBlockMap.size(); i++ )
        {
            delete basicBlockMap.at(i);
        }
        for( int i = 0; i < edgeList.size(); i++ )
        {
            delete edgeList.at(i);
        }
    }

    BasicBlock* createNode(int name) {
        BasicBlock* node;
        if ( name < basicBlockMap.size() && basicBlockMap.at(name)) {
            node = basicBlockMap.at(name);
        } else {
            node = new BasicBlock(name);
            basicBlockMap.atPut(name, node);
        }

        if (getNumNodes() == 1) {
            startNode = node;
        }
        return node;
    }

    void addEdge(BasicBlockEdge* edge) {
        edgeList.append(edge);
    }

    int getNumNodes() const {
        return basicBlockMap.size();
    }

    BasicBlock* getStartBasicBlock() const {
        return startNode;
    }

    Vector<BasicBlock*>& getBasicBlocks() {
        return basicBlockMap;
    }
};

BasicBlockEdge::BasicBlockEdge(ControlFlowGraph *cfg, int fromName, int toName)
{
    from = cfg->createNode(fromName);
    to   = cfg->createNode(toName);

    from->addOutEdge(to);
    to->addInEdge(from);

    cfg->addEdge(this);
}

class SimpleLoop {

    IdentitySet<BasicBlock*> basicBlocks;
    IdentitySet<SimpleLoop*> children;
    SimpleLoop* parent;

    bool isRoot_;
    int     nestingLevel;

    void addChildLoop(SimpleLoop* loop) {
        children.add(loop);
    }

public:
    SimpleLoop(BasicBlock* bb) {
        parent = 0;
        isRoot_ = false;
        nestingLevel = 0;
        if (bb != 0) {
            basicBlocks.add(bb);
        }
    }

    void addNode(BasicBlock* bb) {
        basicBlocks.add(bb);
    }

    IdentitySet<SimpleLoop*>& getChildren() {
        return children;
    }

    SimpleLoop* getParent() const {
        return parent;
    }

    int getNestingLevel() const {
        return nestingLevel;
    }

    bool isRoot() const {   // Note: fct and var are same!
        return isRoot_;
    }

    void setParent(SimpleLoop* parent) {
        this->parent = parent;
        this->parent->addChildLoop(this);
    }

    void setIsRoot() {
        isRoot_ = true;
    }

    void setNestingLevel(int level) {
        nestingLevel = level;
        if (level == 0) {
            setIsRoot();
        }
    }

};

class UnionFindNode {

    UnionFindNode* parent;
    BasicBlock*    bb;
    SimpleLoop*    loop;
    int           dfsNumber;

public:
    UnionFindNode():parent(0),bb(0),loop(0),dfsNumber(0) { }

    void initNode( BasicBlock* bb, int dfsNumber) {
        this->parent     = this;
        this->bb         = bb;
        this->dfsNumber  = dfsNumber;
        this->loop       = 0;
    }

    UnionFindNode* findSet() {
        Vector<UnionFindNode*> nodeList;

        UnionFindNode* node = this;
        while (node != node->parent) {
            if (node->parent != node->parent->parent) {
                nodeList.append(node);
            }
            node = node->parent;
        }

        class Iterator : public ForEachInterface<UnionFindNode*>
        {
            UnionFindNode* parent;
        public:
            Iterator(UnionFindNode* p):parent(p){}
            void apply(UnionFindNode* const & iter)
            {
                iter->unite(parent);
            }
        } it(parent);
        nodeList.forEach(it);
        return node;
    }

    void unite(UnionFindNode* basicBlock) { // orig union
        parent = basicBlock;
    }

    // Getters/Setters
    //
    BasicBlock* getBb() const {
        return bb;
    }

    SimpleLoop* getLoop() const {
        return loop;
    }

    int getDfsNumber() const {
        return dfsNumber;
    }

    void setLoop(SimpleLoop* loop) {
        this->loop = loop;
    }
};

class LoopStructureGraph {

    SimpleLoop* root;
    Vector<SimpleLoop*> loops;
    int loopCounter;

public:
    LoopStructureGraph() {
        loopCounter = 0;
        root = new SimpleLoop(0);
        root->setNestingLevel(0);
        loopCounter += 1;
        loops.append(root);
    }
    ~LoopStructureGraph()
    {
        for( int i = 0; i < loops.size(); i++ )
        {
            delete loops.at(i);
        }
    }

    SimpleLoop* createNewLoop(BasicBlock* bb, bool isReducible) {
        SimpleLoop* loop = new SimpleLoop(bb);
        loopCounter += 1;
        loops.append(loop);
        return loop;
    }

    void calculateNestingLevel() {
        // link up all 1st level loops to artificial root node.
        class Iter : public ForEachInterface<SimpleLoop*>
        {
            SimpleLoop* root;
        public:
            Iter(SimpleLoop* r):root(r){}
            void apply(SimpleLoop* const & liter)
            {
                if (!liter->isRoot()) {
                    if (liter->getParent() == 0) {
                        liter->setParent(root);
                    }
                }
            }
        } it(root);
        loops.forEach(it);

        // recursively traverse the tree and assign levels.
        calculateNestingLevelRec(root, 0);
    }

#define MAX(a,b) (((a)>(b))?(a):(b))
    static void calculateNestingLevelRec(SimpleLoop* loop, int depth) {

        class Iter : public ForEachInterface<SimpleLoop*>
        {
            SimpleLoop* loop; int depth;
        public:
            Iter(SimpleLoop* l, int d):loop(l),depth(d){}
            void apply(SimpleLoop* const & liter)
            {
                calculateNestingLevelRec(liter, depth + 1);

                loop->setNestingLevel(MAX(loop->getNestingLevel(),
                                          1 + liter->getNestingLevel()));
            }
        } it(loop,depth);
        loop->getChildren().forEach(it);
    }

    int getNumLoops() const {
        return loops.size();
    }
};

class HavlakLoopFinder {

    /**
     * enum BasicBlockClass
     *
     * Basic Blocks and Loops are being classified as regular, irreducible,
     * and so on. This enum contains a symbolic name for all these classifications
     */
    enum BasicBlockClass {
        BB_TOP,          // uninitialized
        BB_NONHEADER,    // a regular BB
        BB_REDUCIBLE,    // reducible loop
        BB_SELF,         // single BB loop
        BB_IRREDUCIBLE,  // irreducible loop
        BB_DEAD,         // a dead BB
        BB_LAST          // Sentinel
    };

    ControlFlowGraph*   cfg;      // Control Flow Graph
    LoopStructureGraph* lsg;      // Loop Structure Graph

    enum { UNVISITED = 0xffffffff, MAXNONBACKPREDS = (32 * 1024) };

    Vector<Set<int> >  nonBackPreds;
    Vector<Vector<int> > backPreds;
    typedef IdentityDictionary<BasicBlock*, int, BasicBlock::Hash> Number;
    Number number;
    int maxSize;
    Vector<int>                    header;
    Vector<BasicBlockClass>        type;
    Vector<int>                    last;
    Vector<UnionFindNode*>          nodes;

public:
    HavlakLoopFinder(ControlFlowGraph* cfg, LoopStructureGraph* lsg):maxSize(0) {
        this->cfg = cfg;
        this->lsg = lsg;
    }
    ~HavlakLoopFinder()
    {
        for( int i = 0; i < nodes.size(); i++ )
        {
            delete nodes.at(i);
            nodes.atPut(i,0);
        }
    }

    //
    // findLoops
    //
    // Find loops and build loop forest using Havlak's algorithm, which
    // is derived from Tarjan. Variable names and step numbering has
    // been chosen to be identical to the nomenclature in Havlak's
    // paper (which, in turn, is similar to the one used by Tarjan).
    //
    void findLoops() {
        if (cfg->getStartBasicBlock() == 0) {
            return;
        }

        int size = cfg->getNumNodes();

        nonBackPreds.removeAll();
        backPreds.removeAll();
        number.removeAll();
        if (size > maxSize) {
            header.expand(size);
            type.expand(size);
            last.expand(size);
            nodes.expand(size);
            backPreds.expand(size);
            nonBackPreds.expand(size);
            maxSize = size;
        }

        for (int i = 0; i < size; ++i) {
            nodes.atPut(i, new UnionFindNode());
        }

        initAllNodes();
        identifyEdges(size);

        // Start node is root of all other loops.
        header.atPut(0, 0);

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
        for (int w = size - 1; w >= 0; w--) {
            // this is 'P' in Havlak's paper
            Vector<UnionFindNode*> nodePool;

            BasicBlock* nodeW = nodes.at(w)->getBb();
            if (nodeW != 0) {
                stepD(w, nodePool);

                // Copy nodePool to workList.
                //
                Vector<UnionFindNode*> workList;

                class Iter : public ForEachInterface<UnionFindNode*>
                {
                    Vector<UnionFindNode*>& workList;
                public:
                    Iter(Vector<UnionFindNode*>& w):workList(w){}
                    void apply(UnionFindNode* const& niter)
                    {
                        workList.append(niter);
                    }
                } it(workList);
                nodePool.forEach(it);

                if (nodePool.size() != 0) {
                    type.atPut(w, BB_REDUCIBLE);
                }

                // work the list...
                //
                while (!workList.isEmpty()) {
                    UnionFindNode* x = workList.removeFirst();

                    // Step e:
                    //
                    // Step e represents the main difference from Tarjan's method.
                    // Chasing upwards from the sources of a node w's backedges. If
                    // there is a node y' that is not a descendant of w, w is marked
                    // the header of an irreducible loop, there is another entry
                    // into this loop that avoids w.
                    //

                    // The algorithm has degenerated. Break and
                    // return in this case.
                    //
                    int nonBackSize = nonBackPreds.at(x->getDfsNumber()).size();
                    if (nonBackSize > MAXNONBACKPREDS) {
                        return;
                    }
                    stepEProcessNonBackPreds(w, nodePool, workList, x);
                }

                // Collapse/Unionize nodes in a SCC to a single node
                // For every SCC found, create a loop descriptor and link it in.
                //
                if ((nodePool.size() > 0) || type.at(w) == BB_SELF) {
                    SimpleLoop* loop = lsg->createNewLoop(nodeW, type.at(w) != BB_IRREDUCIBLE);
                    setLoopAttributes(w, nodePool, loop);
                }
            }
        }  // Step c
    }  // findLoops
private:

    //
    // IsAncestor
    //
    // As described in the paper, determine whether a node 'w' is a
    // "true" ancestor for node 'v'.
    //
    // Dominance can be tested quickly using a pre-order trick
    // for depth-first spanning trees. This is why DFS is the first
    // thing we run below.
    //
    bool isAncestor(int w, int v) {
        return w <= v && v <= last.at(w);
    }

    //
    // DFS - Depth-First-Search
    //
    // DESCRIPTION:
    // Simple depth first traversal along out edges with node numbering.
    //
    int doDFS(BasicBlock* currentNode, int current) {
        nodes.at(current)->initNode(currentNode, current);
        number.atPut(currentNode, current);

        int lastId = current;
        const Vector<BasicBlock*>& outerBlocks = currentNode->getOutEdges();

        for (int i = 0; i < outerBlocks.size(); i++) {
            BasicBlock* target = outerBlocks.at(i);
            if ( *number.at(target) == UNVISITED) {
                lastId = doDFS(target, lastId + 1);
            }
        }

        last.atPut(current, lastId);
        return lastId;
    }

    void initAllNodes() {
        // Step a:
        //   - initialize all nodes as unvisited.
        //   - depth-first traversal and numbering.
        //   - unreached BB's are marked as dead.
        //
        class Iter : public ForEachInterface<BasicBlock*>
        {
            Number& number;
        public:
            Iter(Number& n):number(n){}
            void apply(BasicBlock* const& bb)
            {
                number.atPut(bb, UNVISITED);
            }
        } it(number);
        cfg->getBasicBlocks().forEach(it);

        doDFS(cfg->getStartBasicBlock(), 0);
    }

    void identifyEdges(int size) {
        // Step b:
        //   - iterate over all nodes.
        //
        //   A backedge comes from a descendant in the DFS tree, and non-backedges
        //   from non-descendants (following Tarjan).
        //
        //   - check incoming edges 'v' and add them to either
        //     - the list of backedges (backPreds) or
        //     - the list of non-backedges (nonBackPreds)
        //
        for (int w = 0; w < size; w++) {
            header.atPut(w,0);
            type.atPut(w,BB_NONHEADER);

            BasicBlock* nodeW = nodes.at(w)->getBb();
            if (nodeW == 0) {
                type.atPut(w,BB_DEAD);
            } else {
                processEdges(nodeW, w);
            }
        }
    }

    void processEdges(BasicBlock* nodeW, int w) {
        if (nodeW->getNumPred() > 0) {
            class Iter : public ForEachInterface<BasicBlock*>
            {
                HavlakLoopFinder* that;
                int w;
            public:
                Iter(HavlakLoopFinder* t, int ww):that(t),w(ww){}
                void apply(BasicBlock* const & nodeV)
                {
                    int v = *that->number.at(nodeV);
                    if (v != UNVISITED) {
                        if (that->isAncestor(w, v)) {
                            Vector<int>& tmp = that->backPreds.at(w);
                            tmp.append(v);
                        } else {
                            that->nonBackPreds.at(w).add(v);
                        }
                    }
                }
            } it(this,w);
            nodeW->getInEdges().forEach(it);
        }
    }

    void stepEProcessNonBackPreds(int w, Vector<UnionFindNode*>& nodePool,
                                  Vector<UnionFindNode*>& workList, UnionFindNode* x) {
        class Iter : public ForEachInterface<int>
        {
            HavlakLoopFinder* that;
            int w;
            Vector<UnionFindNode*>& nodePool;
            Vector<UnionFindNode*>& workList;
        public:
            Iter(HavlakLoopFinder* t, int ww,
                 Vector<UnionFindNode*>& n,
                 Vector<UnionFindNode*>& w):that(t),w(ww),nodePool(n),workList(w){}
            void apply(const int& iter)
            {
                UnionFindNode* y = that->nodes.at(iter);
                UnionFindNode* ydash = y->findSet();

                if (!that->isAncestor(w, ydash->getDfsNumber())) {
                    that->type.atPut(w, BB_IRREDUCIBLE);
                    that->nonBackPreds.at(w).add(ydash->getDfsNumber());
                } else {
                    if (ydash->getDfsNumber() != w) {
                        class Iter : public TestInterface<UnionFindNode*>
                        {
                            UnionFindNode* ydash;
                        public:
                            Iter(UnionFindNode* y):ydash(y){}
                            bool test(UnionFindNode* const & e) const
                            {
                                return e == ydash;
                            }
                        } it(ydash);
                        if (!nodePool.hasSome(it)) {
                            workList.append(ydash);
                            nodePool.append(ydash);
                        }
                    }
                }
            }
        } it(this,w,nodePool,workList);
        nonBackPreds.at(x->getDfsNumber()).forEach(it);
    }

    void setLoopAttributes(int w, Vector<UnionFindNode*>& nodePool, SimpleLoop* loop) {
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
        nodes.at(w)->setLoop(loop);

        class Iter : public ForEachInterface<UnionFindNode*>
        {
            HavlakLoopFinder* that;
            int w;
            SimpleLoop* loop;
        public:
            Iter(HavlakLoopFinder* that,int w,SimpleLoop* loop):that(that),w(w),loop(loop){}
            void apply(UnionFindNode* const & node)
            {
                // Add nodes to loop descriptor.
                that->header.atPut(node->getDfsNumber(), w);
                node->unite(that->nodes.at(w));

                // Nested loops are not added, but linked together.
                if (node->getLoop() != 0) {
                    node->getLoop()->setParent(loop);
                } else {
                    loop->addNode(node->getBb());
                }
            }
        } it(this,w,loop);
        nodePool.forEach(it);
    }

    void stepD(int w, Vector<UnionFindNode*>& nodePool) {
        class Iter : public ForEachInterface<int>
        {
            HavlakLoopFinder* that;
            int w;
            Vector<UnionFindNode*>& nodePool;
        public:
            Iter(HavlakLoopFinder* that, int w, Vector<UnionFindNode*>& nodePool):that(that),w(w),nodePool(nodePool){}
            void apply(const int& v)
            {
                if (v != w) {
                    nodePool.append(that->nodes.at(v)->findSet());
                } else {
                    that->type.atPut(w,BB_SELF);
                }
            }
        } it(this,w,nodePool);
        backPreds.at(w).forEach(it);
    }
};

class LoopTesterApp {

    ControlFlowGraph*   cfg;
    LoopStructureGraph* lsg;

public:
    LoopTesterApp() {
        cfg = new ControlFlowGraph();
        lsg = new LoopStructureGraph();
        cfg->createNode(0);
    }

    ~LoopTesterApp()
    {
        delete lsg;
        delete cfg;
    }

    std::pair<int,int> main(int numDummyLoops, int findLoopIterations,
                            int parLoops, int pparLoops, int ppparLoops) {
        constructSimpleCFG();
        addDummyLoops(numDummyLoops);
        constructCFG(parLoops, pparLoops, ppparLoops);

        // Performing Loop Recognition, 1 Iteration, then findLoopIteration
        findLoops(lsg);
        Vector<LoopStructureGraph*> toDelete;
        for (int i = 0; i < findLoopIterations; i++) {
            LoopStructureGraph* l = new LoopStructureGraph();
            toDelete.append(l);
            findLoops(l);
        }

        lsg->calculateNestingLevel();
        for( int i = 0; i < toDelete.size(); i++ )
            delete toDelete.at(i);
        return std::pair<int,int>(lsg->getNumLoops(), cfg->getNumNodes());
    }

private:
    // Create 4 basic blocks, corresponding to and if/then/else clause
    // with a CFG that looks like a diamond
    int buildDiamond(int start) {
        int bb0 = start;
        new BasicBlockEdge(cfg, bb0, bb0 + 1);
        new BasicBlockEdge(cfg, bb0, bb0 + 2);
        new BasicBlockEdge(cfg, bb0 + 1, bb0 + 3);
        new BasicBlockEdge(cfg, bb0 + 2, bb0 + 3);

        return bb0 + 3;
    }

    // Connect two existing nodes
    void buildConnect(int start, int end) {
        new BasicBlockEdge(cfg, start, end);
    }

    // Form a straight connected sequence of n basic blocks
    int buildStraight( int start,  int n) {
        for (int i = 0; i < n; i++) {
            buildConnect(start + i, start + i + 1);
        }
        return start + n;
    }

    // Construct a simple loop with two diamonds in it
    int buildBaseLoop(int from) {
        int header   = buildStraight(from, 1);
        int diamond1 = buildDiamond(header);
        int d11      = buildStraight(diamond1, 1);
        int diamond2 = buildDiamond(d11);
        int footer   = buildStraight(diamond2, 1);
        buildConnect(diamond2, d11);
        buildConnect(diamond1, header);

        buildConnect(footer, from);
        footer = buildStraight(footer, 1);
        return footer;
    }

    void constructCFG(int parLoops, int pparLoops, int ppparLoops) {
        int n = 2;

        for (int parlooptrees = 0; parlooptrees < parLoops; parlooptrees++) {
            cfg->createNode(n + 1);
            buildConnect(2, n + 1);
            n += 1;

            for (int i = 0; i < pparLoops; i++) {
                int top = n;
                n = buildStraight(n, 1);
                for (int j = 0; j < ppparLoops; j++) {
                    n = buildBaseLoop(n);
                }
                int bottom = buildStraight(n, 1);
                buildConnect(n, top);
                n = bottom;
            }
            buildConnect(n, 1);
        }
    }

    void addDummyLoops(int numDummyLoops) {
        for (int dummyloop = 0; dummyloop < numDummyLoops; dummyloop++) {
            findLoops(lsg);
        }
    }

    void findLoops(LoopStructureGraph* loopStructure) {
        HavlakLoopFinder finder(cfg, loopStructure);
        finder.findLoops();
    }

    void constructSimpleCFG() {
        cfg->createNode(0);
        buildBaseLoop(0);
        cfg->createNode(1);
        new BasicBlockEdge(cfg, 0, 2);
    }
};

bool Havlak::innerBenchmarkLoop(int innerIterations)
{
    LoopTesterApp app;

    std::pair<int,int> result = app.main(innerIterations, 50, 10 /* was 100 */, 10, 5);

    return verifyResult(result.first, result.second, innerIterations);
}

bool Havlak::verifyResult(int a, int b, int innerIterations)
{
    if (innerIterations == 15000) { return a == 46602 && b == 5213; }
    if (innerIterations ==  1500) { return a ==  6102 && b == 5213; }
    if (innerIterations ==   150) { return a ==  2052 && b == 5213; }
    if (innerIterations ==    15) { return a ==  1647 && b == 5213; }
    if (innerIterations ==     1) { return a ==  1605 && b == 5213; }

    // Checkstyle: stop
    std::cerr << "No verification result for " << innerIterations << " found" << std::endl;
    std::cerr << "Result is: " << a << ", " << b << std::endl;
    // Checkstyle: resume

    return false;

}


