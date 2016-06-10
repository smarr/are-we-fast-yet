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

//======================================================
// Scaffold Code
//======================================================

/**
 * The Havlak loop finding algorithm.
 *
 * @author rhundt
 */
package havlak;

import som.IdentitySet;

/**
 * class SimpleLoop
 *
 * Basic representation of loops, a loop has an entry point,
 * one or more exit edges, a set of basic blocks, and potentially
 * an outer loop - a "parent" loop.
 *
 * Furthermore, it can have any set of properties, e.g.,
 * it can be an irreducible loop, have control flow, be
 * a candidate for transformations, and what not.
 */
final class SimpleLoop {

  private final IdentitySet<BasicBlock> basicBlocks;
  private final IdentitySet<SimpleLoop> children;
  private SimpleLoop            parent;

  @SuppressWarnings("unused") private final BasicBlock header;
  @SuppressWarnings("unused") private final boolean isReducible;

  private boolean isRoot;
  private int     nestingLevel;

  @SuppressWarnings("unused") private int counter;
  @SuppressWarnings("unused") private int depthLevel;


  SimpleLoop(final BasicBlock bb, final boolean isReducible) {
    this.isReducible = isReducible;
    parent = null;
    isRoot = false;
    nestingLevel = 0;
    depthLevel   = 0;
    basicBlocks  = new IdentitySet<>();
    children     = new IdentitySet<>();

    if (bb != null) {
      basicBlocks.add(bb);
    }
    header = bb;
  }

  public void addNode(final BasicBlock bb) {
    basicBlocks.add(bb);
  }

  private void addChildLoop(final SimpleLoop loop) {
    children.add(loop);
  }

  // Getters/Setters
  public IdentitySet<SimpleLoop> getChildren() {
    return children;
  }

  public SimpleLoop getParent() {
    return parent;
  }

  public int getNestingLevel() {
    return nestingLevel;
  }

  public boolean isRoot() {   // Note: fct and var are same!
    return isRoot;
  }

  public void setParent(final SimpleLoop parent) {
    this.parent = parent;
    this.parent.addChildLoop(this);
  }

  public void setIsRoot() {
    isRoot = true;
  }

  public void setCounter(final int value) {
    counter = value;
  }

  public void setNestingLevel(final int level) {
    nestingLevel = level;
    if (level == 0) {
      setIsRoot();
    }
  }

  public void setDepthLevel(final int level) {
    depthLevel = level;
  }
}
