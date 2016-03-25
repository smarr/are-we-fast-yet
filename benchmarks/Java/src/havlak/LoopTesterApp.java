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

public class LoopTesterApp {
  public LoopTesterApp() {
    cfg = new ControlFlowGraph();
    lsg = new LoopStructureGraph();
    root = cfg.createNode(0);
  }

  // Create 4 basic blocks, corresponding to and if/then/else clause
  // with a CFG that looks like a diamond
  public int buildDiamond(final int start) {
    int bb0 = start;
    new BasicBlockEdge(cfg, bb0, bb0 + 1);
    new BasicBlockEdge(cfg, bb0, bb0 + 2);
    new BasicBlockEdge(cfg, bb0 + 1, bb0 + 3);
    new BasicBlockEdge(cfg, bb0 + 2, bb0 + 3);

    return bb0 + 3;
  }

  // Connect two existing nodes
  public void buildConnect(final int start, final int end) {
    new BasicBlockEdge(cfg, start, end);
  }

  // Form a straight connected sequence of n basic blocks
  public int buildStraight(final int start, final int n) {
    for (int i = 0; i < n; i++) {
      buildConnect(start + i, start + i + 1);
    }
    return start + n;
  }

  // Construct a simple loop with two diamonds in it
  public int buildBaseLoop(final int from) {
    int header = buildStraight(from, 1);
    int diamond1 = buildDiamond(header);
    int d11 = buildStraight(diamond1, 1);
    int diamond2 = buildDiamond(d11);
    int footer = buildStraight(diamond2, 1);
    buildConnect(diamond2, d11);
    buildConnect(diamond1, header);

    buildConnect(footer, from);
    footer = buildStraight(footer, 1);
    return footer;
  }

  public static void main(final String[] args) {
    System.out.println("Welcome to LoopTesterApp, Java edition");

    // Constructing App
    LoopTesterApp app = new LoopTesterApp();

    // Constructing Simple CFG
    app.cfg.createNode(0);
    app.buildBaseLoop(0);
    app.cfg.createNode(1);
    new BasicBlockEdge(app.cfg, 0, 2);

    // 15000 dummy loops
    for (int dummyloop = 0; dummyloop < 15000; dummyloop++) {
      HavlakLoopFinder finder = new HavlakLoopFinder(app.cfg, app.lsg);
      finder.findLoops();
    }

    // Constructing CFG...
    int n = 2;

    for (int parlooptrees = 0; parlooptrees < 10; parlooptrees++) {
      app.cfg.createNode(n + 1);
      app.buildConnect(2, n + 1);
      n = n + 1;

      for (int i = 0; i < 10; i++) { // 10 used to be 100
        int top = n;
        n = app.buildStraight(n, 1);
        for (int j = 0; j < 25; j++) {
          n = app.buildBaseLoop(n);
        }
        int bottom = app.buildStraight(n, 1);
        app.buildConnect(n, top);
        n = bottom;
      }
      app.buildConnect(n, 1);
    }

    // Performing Loop Recognition, 1 Iteration
    HavlakLoopFinder finder = new HavlakLoopFinder(app.cfg, app.lsg);
    finder.findLoops();

    // Another 50 iterations...
    for (int i = 0; i < 50; i++) {
      System.out.format(".");
      HavlakLoopFinder finder2 = new HavlakLoopFinder(app.cfg, new LoopStructureGraph());
      finder2.findLoops();
    }

    app.lsg.calculateNestingLevel();
  }

  public  ControlFlowGraph        cfg;
  private final LoopStructureGraph        lsg;
  private final BasicBlock root;
}
