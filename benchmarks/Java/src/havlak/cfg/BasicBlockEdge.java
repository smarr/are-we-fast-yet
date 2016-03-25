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
 * A simple class simulating the concept of Edges
 * between Basic Blocks
 *
 * @author rhundt
 */
package cfg;

/**
 * class BasicBlockEdga
 *
 * These data structures are stubbed out to make the code below easier
 * to review.
 *
 * BasicBlockEdge only maintains two pointers to BasicBlocks.
 */
public class BasicBlockEdge {
  public BasicBlockEdge(CFG cfg, int fromName, int toName) {
    from = cfg.createNode(fromName);
    to   = cfg.createNode(toName);

    from.addOutEdge(to);
    to.addInEdge(from);

    cfg.addEdge(this);
  }

  public  BasicBlock getSrc() { return from; }
  public  BasicBlock getDst() { return to; }

  private BasicBlock from, to;
};
