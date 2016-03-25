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
public class BasicBlock {

  static int numBasicBlocks = 0;

  public static void resetNumBasicBlocks() {
    System.out.println("REMOVE!!! resetNumBasicBlocks()");
    numBasicBlocks = 0;
  }
  public static int getNumBasicBlocks() {
    return numBasicBlocks;
  }

  public BasicBlock(final int name) {
    this.name = name;
    inEdges   = new ArrayList<BasicBlock>();
    outEdges  = new ArrayList<BasicBlock>();
    ++numBasicBlocks;
  }

  public void dump() {
    System.out.format("BB#%03d: ", getName());
    if (inEdges.size() > 0) {
      System.out.format("in : ");
      for (BasicBlock bb : inEdges) {
        System.out.format("BB#%03d ", bb.getName());
      }
    }
    if (outEdges.size() > 0) {
      System.out.format("out: ");
      for (BasicBlock bb : outEdges) {
        System.out.format("BB#%03d ", bb.getName());
      }
    }
    System.out.println();
  }

  public int getName() {
    return name;
  }

  public List<BasicBlock> getInEdges() {
    return inEdges;
  }
  public List<BasicBlock> getOutEdges() {
    return outEdges;
  }

  public int getNumPred() {
    return inEdges.size();
  }
  public int getNumSucc() {
    return outEdges.size();
  }

  public void addOutEdge(final BasicBlock to) {
    outEdges.add(to);
  }
  public void addInEdge(final BasicBlock from) {
    inEdges.add(from);
  }

  private final List<BasicBlock> inEdges, outEdges;
  private final int name;
};
