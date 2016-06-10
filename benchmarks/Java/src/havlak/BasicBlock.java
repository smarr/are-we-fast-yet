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

import som.Dictionary.CustomHash;
import som.Vector;

/**
 * A simple class simulating the concept of Basic Blocks
 *
 * BasicBlock only maintains a vector of in-edges and
 * a vector of out-edges.
 *
 * @author rhundt
 */
final class BasicBlock implements CustomHash {

  private final Vector<BasicBlock> inEdges;
  private final Vector<BasicBlock> outEdges;
  private final int name;

  BasicBlock(final int name) {
    this.name = name;
    inEdges   = new Vector<BasicBlock>(2);
    outEdges  = new Vector<BasicBlock>(2);
  }

  public Vector<BasicBlock> getInEdges() {
    return inEdges;
  }

  public Vector<BasicBlock> getOutEdges() {
    return outEdges;
  }

  public int getNumPred() {
    return inEdges.size();
  }

  public void addOutEdge(final BasicBlock to) {
    outEdges.append(to);
  }

  public void addInEdge(final BasicBlock from) {
    inEdges.append(from);
  }

  @Override
  public int customHash() {
    return name;
  }
}
