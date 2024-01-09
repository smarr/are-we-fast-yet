
#include "HavlakLoopFinder.h"

namespace havlak {

    bool HavlakLoopFinder::isAncestor(int w, int v) {
        return w <= v && v <= _last[w];
    }

    int HavlakLoopFinder::doDFS(shared_ptr<BasicBlock> currentNode, int current) {
        _nodes[current]->initNode(currentNode, current);
        _number->atPut(currentNode, current);
        int lastId = current;
        shared_ptr<Vector<shared_ptr<BasicBlock>>> outerBlocks = currentNode->getOutEdges();
        for (int i = 0; i < outerBlocks->size(); i++) {
            shared_ptr<BasicBlock> target = outerBlocks->at(i);
            if (_number->at(target) == UNVISITED) {
                lastId = doDFS(target, lastId + 1);
            }
        }

        _last[current] = lastId;
        return lastId;
    }

    void HavlakLoopFinder::initAllNodes() {
        _cfg->getBasicBlocks()->forEach([&](shared_ptr<BasicBlock> bb) -> void {
            _number->atPut(bb, UNVISITED);
        });
        doDFS(_cfg->getStartBasicBlock(), 0);
    }

    void HavlakLoopFinder::identifyEdges(int size) {
        for (int w = 0; w < size; w++) {
            _header[w] = 0;
            _type[w] = BB_NONHEADER;

            shared_ptr<BasicBlock> nodeW = _nodes[w]->getBb();
            if (nodeW == nullptr) {
                _type[w] = BB_DEAD;
            } else {
                processEdges(nodeW, w);
            }
        }
    }

    void HavlakLoopFinder::processEdges(shared_ptr<BasicBlock> nodeW, int w) {
        if (nodeW->getNumPred() > 0) {
            nodeW->getInEdges()->forEach([&](shared_ptr<BasicBlock> nodeV) -> void {
                int v = _number->at(nodeV);
                if (v != UNVISITED) {
                    if (isAncestor(w, v)) {
                        _backPreds->at(w)->append(v);
                    } else {
                        _nonBackPreds->at(w).insert(v);
                    }
                }
            });
        }
    }

    void HavlakLoopFinder::stepEProcessNonBackPreds(int w, shared_ptr<Vector<shared_ptr<UnionFindNode>>> nodePool, shared_ptr<Vector<shared_ptr<UnionFindNode>>> workList, shared_ptr<UnionFindNode> x) {
        for (int iter : _nonBackPreds->at(x->getDfsNumber())) {
            shared_ptr<UnionFindNode> y = _nodes[iter];
            shared_ptr<UnionFindNode> ydash = y->findSet();

            if (!isAncestor(w, ydash->getDfsNumber())) {
                _type[w] = BB_IRREDUCIBLE;
                _nonBackPreds->at(w).insert(ydash->getDfsNumber());
            } else {
                if (ydash->getDfsNumber() != w) {
                    if (!nodePool->hasSome([&](shared_ptr<UnionFindNode> e) -> bool {
                        return e == ydash;
                    })) {
                        workList->append(ydash);
                        nodePool->append(ydash);
                    }
                }
            }
        }
    }


    void HavlakLoopFinder::setLoopAttributes(int w, shared_ptr<Vector<shared_ptr<UnionFindNode>>> nodePool, shared_ptr<SimpleLoop> loop) {
        _nodes[w]->setLoop(loop);

        nodePool->forEach([&](shared_ptr<UnionFindNode> node) -> void {
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

    void HavlakLoopFinder::stepD(int w, shared_ptr<Vector<shared_ptr<UnionFindNode>>> nodePool) {

        _backPreds->at(w)->forEach([&](int v) -> void {
            if (v != w) {
                nodePool->append(_nodes[v]->findSet());
            } else {
                _type[w] = BB_SELF;
            }   
        });
    }



    HavlakLoopFinder::HavlakLoopFinder(shared_ptr<ControlFlowGraph> cfg, shared_ptr<LoopStructureGraph> lsg) {
        _cfg = cfg;
        _lsg = lsg;
    }

    void HavlakLoopFinder::findLoops() {
        if (_cfg->getStartBasicBlock() == nullptr) {
            return;
        }

        int size = _cfg->getNumNodes();

        _nonBackPreds->removeAll();
        _backPreds->removeAll();
        _number->removeAll();
        if (size > _maxSize) {
            _header = new int[size];
            _type = new BasicBlockClass[size];
            _last = new int[size];
            _nodes = new shared_ptr<UnionFindNode>[size];
            _maxSize = size;
        }

        for (int i = 0; i < size; ++i) {
            _nodes[i] = make_shared<UnionFindNode>();
            _backPreds->append(make_shared<Vector<int>>());
        }

        initAllNodes();
        identifyEdges(size);

        // Start node is root of all other loops.
        _header[0] = 0;
        for (int w = size - 1; w >= 0; w--) {
            // this is 'P' in Havlak's paper
            shared_ptr<Vector<shared_ptr<UnionFindNode>>> nodePool = make_shared<Vector<shared_ptr<UnionFindNode>>>();

            shared_ptr<BasicBlock> nodeW = _nodes[w]->getBb();
            if (nodeW != nullptr) {
                stepD(w, nodePool);
            
                // Copy nodePool to workList.
                //
                shared_ptr<Vector<shared_ptr<UnionFindNode>>> workList = make_shared<Vector<shared_ptr<UnionFindNode>>>();
                nodePool->forEach([&](shared_ptr<UnionFindNode> niter) -> void {
                    workList->append(niter);
                });

                if (nodePool->size() != 0) {
                    _type[w] = BB_REDUCIBLE;
                }

                // work the list...
                //
                while (!workList->isEmpty()) {
                    shared_ptr<UnionFindNode> x = workList->removeFirst();

                    int nonBackSize = _nonBackPreds->at(x->getDfsNumber()).size();
                    if (nonBackSize > MAXNONBACKPREDS) {
                        return;
                    }
                    stepEProcessNonBackPreds(w, nodePool, workList, x);
                }

                if ((nodePool->size() > 0) || (_type[w] == BB_SELF)) {
                    shared_ptr<SimpleLoop> loop = _lsg->createNewLoop(nodeW, _type[w] != BB_IRREDUCIBLE);
                    setLoopAttributes(w, nodePool, loop);
                }
            }
        }  // Step c
    }  // findLoops
}