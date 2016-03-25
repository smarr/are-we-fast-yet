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

/**
 * A simple class simulating the concept of
 * a control flow graph.
 *
 * @author rhundt
 */
package havlak.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * class CFG
 *
 * CFG maintains a list of nodes, plus a start node.
 * That's it.
 */
public class CFG {
  public CFG() {
    startNode = null;
    basicBlockMap = new HashMap<Integer, BasicBlock>();
    edgeList = new ArrayList<BasicBlockEdge>();
  }

  public BasicBlock createNode(final int name) {
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

  public void dump() {
    for (BasicBlock bb : basicBlockMap.values()) {
      bb.dump();
    }
  }

  public void addEdge(final BasicBlockEdge edge) {
    edgeList.add(edge);
  }

  public int getNumNodes() {
    return basicBlockMap.size();
  }

  public BasicBlock getStartBasicBlock() {
    return startNode;
  }

  public BasicBlock getDst(final BasicBlockEdge edge) {
    return edge.getDst();
  }

  public BasicBlock getSrc(final BasicBlockEdge edge) {
    return edge.getSrc();
  }

  public Map<Integer, BasicBlock> getBasicBlocks() {
    return basicBlockMap;
  }

  private final Map<Integer, BasicBlock>  basicBlockMap;
  private BasicBlock                startNode;
  private final List<BasicBlockEdge>      edgeList;
};
