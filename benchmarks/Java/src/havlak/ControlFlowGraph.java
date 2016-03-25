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
package havlak;

import java.util.HashMap;
import java.util.Map;

import som.Vector;


/**
 * A simple class simulating the concept of
 * a control flow graph.
 *
 * CFG maintains a list of nodes, plus a start node.
 * That's it.
 *
 * @author rhundt
 */
final class ControlFlowGraph {

  private final Map<Integer, BasicBlock>  basicBlockMap;
  private BasicBlock                startNode;
  private final Vector<BasicBlockEdge> edgeList;

  ControlFlowGraph() {
    startNode = null;
    basicBlockMap = new HashMap<Integer, BasicBlock>();
    edgeList = new Vector<>();
  }

  BasicBlock createNode(final int name) {
    BasicBlock node;
    if (!basicBlockMap.containsKey(name)) {
      node = new BasicBlock(name);
      basicBlockMap.put(name, node);
    } else {
      node = basicBlockMap.get(name);
    }

    if (getNumNodes() == 1) {
      startNode = node;
    }

    return node;
  }

  void addEdge(final BasicBlockEdge edge) {
    edgeList.append(edge);
  }

  int getNumNodes() {
    return basicBlockMap.size();
  }

  BasicBlock getStartBasicBlock() {
    return startNode;
  }

  BasicBlock getDst(final BasicBlockEdge edge) {
    return edge.getDst();
  }

  BasicBlock getSrc(final BasicBlockEdge edge) {
    return edge.getSrc();
  }

  Map<Integer, BasicBlock> getBasicBlocks() {
    return basicBlockMap;
  }
}
