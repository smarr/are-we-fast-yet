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

  public static int[] main(final int numDummyLoops, final int findLoopIteration,
      final int parLoops, final int pparLoops, final int ppparLoops) {
    // Constructing App
    LoopTesterApp app = new LoopTesterApp();
    app.constructSimpleCFG();

    app.addDummyLoops(numDummyLoops);

    // Constructing CFG...
    app.constructCFG(parLoops, pparLoops, ppparLoops);

    // Performing Loop Recognition, 1 Iteration, then findLoopIteration
    app.findLoops(app.lsg);
    for (int i = 0; i < findLoopIteration; i++) {
      app.findLoops(new LoopStructureGraph());
    }

    app.lsg.calculateNestingLevel();

    int numBasicBlocks = BasicBlock.getNumBasicBlocks();
    BasicBlock.resetNumBasicBlocks();
    return new int[] { app.lsg.getNumLoops(), numBasicBlocks };
  }

  void constructCFG(final int parLoops, final int pparLoops,
      final int ppparLoops) {
    int n = 2;

    for (int parlooptrees = 0; parlooptrees < parLoops; parlooptrees++) {
      cfg.createNode(n + 1);
      buildConnect(2, n + 1);
      n = n + 1;

      for (int i = 0; i < pparLoops; i++) {
        int top = n;
        n = buildStraight(n, 1);
        for (int j = 0; j < ppparLoops; j++) {
          n = buildBaseLoop(n);
        }
        int bottom = buildStraight(n, 1);
        buildConnect(n, top);
        n = bottom;
      }
      buildConnect(n, 1);
    }
  }

  void addDummyLoops(final int numDummyLoops) {
    for (int dummyloop = 0; dummyloop < numDummyLoops; dummyloop++) {
      HavlakLoopFinder finder = new HavlakLoopFinder(cfg, lsg);
      finder.findLoops();
    }
  }

  void findLoops(final LoopStructureGraph loopStructure) {
    HavlakLoopFinder finder = new HavlakLoopFinder(cfg, loopStructure);
    finder.findLoops();
  }

  void constructSimpleCFG() {
    cfg.createNode(0);
    buildBaseLoop(0);
    cfg.createNode(1);
    new BasicBlockEdge(cfg, 0, 2);
  }

  public  final ControlFlowGraph   cfg;
  private final LoopStructureGraph lsg;
  private final BasicBlock root;
}
