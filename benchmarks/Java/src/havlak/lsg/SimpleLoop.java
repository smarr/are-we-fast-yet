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
package lsg;

import cfg.BasicBlock;

import java.util.*;

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
public class SimpleLoop {
  public SimpleLoop() {
    parent = null;
    isRoot = false;
    isReducible  = true;
    nestingLevel = 0;
    depthLevel   = 0;
    basicBlocks  = new HashSet<BasicBlock>();
    children     = new HashSet<SimpleLoop>();
  }

  public void addNode(BasicBlock bb) {
    basicBlocks.add(bb);
  }

  public void addChildLoop(SimpleLoop loop) {
    children.add(loop);
  }

  public void dump(int indent) {
    for (int i = 0; i < indent; i++)
      System.out.format("  ");

    System.out.format("loop-%d nest: %d depth %d %s",
                      counter, nestingLevel, depthLevel,
                      isReducible ? "" : "(Irreducible) ");
    if (!getChildren().isEmpty()) {
      System.out.format("Children: ");
      for (SimpleLoop loop : getChildren()) {
        System.out.format("loop-%d ", loop.getCounter());
      }
    }
    if (!basicBlocks.isEmpty()) {
      System.out.format("(");
      for (BasicBlock bb : basicBlocks) {
        System.out.format("BB#%d%s", bb.getName(), header == bb ? "* " : " ");
      }
      System.out.format("\b)");
    }
    System.out.format("\n");
  }

  // Getters/Setters
  public Set<SimpleLoop> getChildren() {
    return children;
  }
  public SimpleLoop   getParent() {
    return parent;
  }
  public int          getNestingLevel(){
    return nestingLevel;
  }
  public int          getDepthLevel() {
    return depthLevel;
  }
  public int          getCounter() {
    return counter;
  }
  public boolean      isRoot() {   // Note: fct and var are same!
    return isRoot;
  }
  public void setParent(SimpleLoop parent) {
    this.parent = parent;
    this.parent.addChildLoop(this);
  }
  public void setHeader(BasicBlock bb) {
    basicBlocks.add(bb);
    header = bb;
  }
  public void setIsRoot() {
    isRoot = true;
  }
  public void setCounter(int value) {
    counter = value;
  }
  public void setNestingLevel(int level) {
    nestingLevel = level;
    if (level == 0) {
      setIsRoot();
    }
  }
  public void setDepthLevel(int level) {
    depthLevel = level;
  }
  public void setIsReducible(boolean isReducible) {
    this.isReducible = isReducible;
  }

  private Set<BasicBlock>        basicBlocks;
  private Set<SimpleLoop>        children;
  private SimpleLoop             parent;
  private BasicBlock             header;

  private boolean      isRoot;
  private boolean      isReducible;
  private int          counter;
  private int          nestingLevel;
  private int          depthLevel;
};
