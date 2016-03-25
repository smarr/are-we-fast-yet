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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple class simulating the concept of Basic Blocks
 *
 * BasicBlock only maintains a vector of in-edges and
 * a vector of out-edges.
 *
 * @author rhundt
 */
final class BasicBlock {

  private final List<BasicBlock> inEdges, outEdges;
  private final int name;

  BasicBlock(final int name) {
    this.name = name;
    inEdges   = new ArrayList<BasicBlock>();
    outEdges  = new ArrayList<BasicBlock>();
  }

  int getName() {
    return name;
  }

  List<BasicBlock> getInEdges() {
    return inEdges;
  }

  List<BasicBlock> getOutEdges() {
    return outEdges;
  }

  int getNumPred() {
    return inEdges.size();
  }

  int getNumSucc() {
    return outEdges.size();
  }

  void addOutEdge(final BasicBlock to) {
    outEdges.add(to);
  }

  void addInEdge(final BasicBlock from) {
    inEdges.add(from);
  }
}
