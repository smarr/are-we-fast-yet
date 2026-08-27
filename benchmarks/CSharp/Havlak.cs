namespace Benchmarks;

public class Havlak : Benchmark
{
  public override bool InnerBenchmarkLoop(int innerIterations)
  {
    return VerifyResult((new LoopTesterApp()).Main(innerIterations, 50, 10 /* was 100 */, 10, 5), innerIterations);
  }

  public bool VerifyResult(object result, int innerIterations)
  {
    int[] r = (int[])result;

    if (innerIterations == 15000) { return r[0] == 46602 && r[1] == 5213; }
    if (innerIterations ==  1500) { return r[0] ==  6102 && r[1] == 5213; }
    if (innerIterations ==   150) { return r[0] ==  2052 && r[1] == 5213; }
    if (innerIterations ==    15) { return r[0] ==  1647 && r[1] == 5213; }
    if (innerIterations ==     1) { return r[0] ==  1605 && r[1] == 5213; }

    Console.WriteLine("No verification result for " + innerIterations + " found");
    Console.WriteLine("Result is: " + r[0] + ", " + r[1]);
    return false;
  }

  public override object Execute()
  {
    throw new NotImplementedException();
  }

  public override bool VerifyResult(object result)
  {
    throw new NotImplementedException();
  }

  sealed class BasicBlock : ICustomHash
  {
    public Vector<BasicBlock> InEdges { get; }
    public Vector<BasicBlock> OutEdges { get; }

    private readonly int name;

    public BasicBlock(int name)
    {
      this.name = name;
      InEdges = new Vector<BasicBlock>();
      OutEdges = new Vector<BasicBlock>();
    }

    public int GetNumPred() { return InEdges.Size(); }

    public void AddOutEdge(BasicBlock to)
    {
      OutEdges.Append(to);
    }

    public void AddInEdge(BasicBlock from)
    {
      InEdges.Append(from);
    }

    public int CustomHash()
    {
      return name;
    }
  }

  sealed class BasicBlockEdge
  {
    private readonly BasicBlock from;
    private readonly BasicBlock to;

    public BasicBlockEdge(ControlFlowGraph cfg, int fromName, int toName)
    {
      from = cfg.CreateNode(fromName);
      to = cfg.CreateNode(toName);

      from.AddOutEdge(to);
      to.AddInEdge(from);

      cfg.AddEdge(this);
    }
  }

  sealed class ControlFlowGraph
  {
    public Vector<BasicBlock> BasicBlocks { get; }

    private BasicBlock? startNode;
    private readonly Vector<BasicBlockEdge> edgeList;

    public ControlFlowGraph()
    {
      BasicBlocks = new Vector<BasicBlock>();
      edgeList = new Vector<BasicBlockEdge>();
    }

    public BasicBlock CreateNode(int name)
    {
      BasicBlock node;

      if (BasicBlocks.At(name) != null) {
        node = BasicBlocks.At(name)!;
      } else {
        node = new BasicBlock(name);
        BasicBlocks.AtPut(name, node);
      }

      if (GetNumNodes() == 1)
      {
        startNode = node;
      }

      return node;
    }

    public void AddEdge(BasicBlockEdge edge)
    {
      edgeList.Append(edge);
    }

    public int GetNumNodes() { return BasicBlocks.Size(); }

    public BasicBlock? GetStartBasicBlock() { return startNode; }
  }

  sealed class LoopStructureGraph
  {
    private readonly SimpleLoop root;
    private readonly Vector<SimpleLoop> loops;

    private int loopCounter;

    public LoopStructureGraph()
    {
      loopCounter = 0;
      loops = new Vector<SimpleLoop>();
      root = new SimpleLoop(null, true);
      root.NestingLevel = 0;
      root.Counter = loopCounter;
      loopCounter += 1;

      loops.Append(root);
    }

    public SimpleLoop CreateLoop(BasicBlock bb, bool isReducible)
    {
      SimpleLoop loop = new SimpleLoop(bb, isReducible);
      loop.Counter = loopCounter;
      loopCounter += 1;
      loops.Append(loop);
      return loop;
    }

    public void CalculateNestingLevel()
    {
      // link up all 1st level loops to artificial root node.
      loops.ForEach(liter => {
        if (!liter.IsRoot) {
          if (liter.Parent == null) {
            liter.Parent = root;
          }
        }
      });

      // recursively traverse the tree and assign levels.
      CalculateNestingLevelRec(root, 0);
    }

    private void CalculateNestingLevelRec(SimpleLoop loop, int depth)
    {
      loop.DepthLevel = depth;
      loop.Children.ForEach(liter => {
        CalculateNestingLevelRec(liter, depth + 1);

        loop.NestingLevel = Math.Max(loop.NestingLevel, 1+ liter.NestingLevel);
      });
    }

    public int GetNumLoops() { return loops.Size(); }
  }

  sealed class SimpleLoop
  {
    private readonly IdentitySet<BasicBlock> basicBlocks;
    private SimpleLoop? parent;
    private int nestingLevel;

    private readonly BasicBlock? header;

    private readonly bool isReducible;

    public IdentitySet<SimpleLoop> Children { get; }
    public int Counter { get; set; }
    public int DepthLevel { get; set; }
    public bool IsRoot { get; private set; }

    public SimpleLoop(BasicBlock? bb, bool isReducible)
    {
      this.isReducible = isReducible;
      parent = null;
      IsRoot = false;
      nestingLevel = 0;
      DepthLevel = 0;
      basicBlocks = new IdentitySet<BasicBlock>();
      Children = new IdentitySet<SimpleLoop>();

      if (bb != null) {
        basicBlocks.Add(bb);
      }

      header = bb;
    }

    public void AddNode(BasicBlock bb)
    {
      basicBlocks.Add(bb);
    }

    private void AddChildLoop(SimpleLoop loop)
    {
      Children.Add(loop);
    }

    public SimpleLoop? Parent
    {
      get => parent;
      set
      {
        parent = value;
        value!.AddChildLoop(this);
      }
    }

    public int NestingLevel
    {
      get => nestingLevel;
      set
      {
        nestingLevel = value;
        if (value == 0) {
          IsRoot = true;
        }
      }
    }
  }

  sealed class UnionFindNode
  {
    private UnionFindNode? parent;

    public BasicBlock? Bb { get; private set; }
    public SimpleLoop? Loop { get; set; }
    public int DfsNumber { get; private set; }

    // Initialize this node.
    public void InitNode(BasicBlock bb, int dfsNumber)
    {
      parent = this;
      Bb = bb;
      DfsNumber = dfsNumber;
      Loop = null;
    }

    // Union/Find Algorithm - The find routine.
    //
    // Implemented with Path Compression (inner loops are only
    // visited and collapsed once, however, deep nests would still
    // result in significant traversals).
    public UnionFindNode FindSet()
    {
      Vector<UnionFindNode> nodeList = new Vector<UnionFindNode>();

      UnionFindNode node = this;
      while (node != node.parent) {
        if (node.parent != node.parent!.parent) {
          nodeList.Append(node);
        }
        node = node.parent!;
      }

      // Path Compression, all nodes' parents point to the 1st level parent.
      nodeList.ForEach(iter => iter.Union(parent!));
      return node;
    }

    // Union/Find Algorithm - The union routine.
    //
    // Trivial. Assigning parent pointer is enough,
    // we rely on path compression.
    public void Union(UnionFindNode basicBlock)
    {
      parent = basicBlock;
    }
  }

  sealed class HavlakLoopFinder
  {
    private readonly ControlFlowGraph cfg;      // Control Flow Graph
    private readonly LoopStructureGraph lsg;    // Loop Structure Graph

    // Marker for uninitialized nodes.
    private const int UNVISITED = int.MaxValue;

    // Safeguard against pathological algorithm behavior.
    private const int MAX_NON_BACK_PREDS = 32 * 1024;

    private readonly Vector<Set<object>> nonBackPreds = new Vector<Set<object>>();
    private readonly Vector<Vector<object>> backPreds = new Vector<Vector<object>>();
    private readonly IdentityDictionary<BasicBlock, object> number = new IdentityDictionary<BasicBlock, object>();
    private int maxSize = 0;
    private int[] header = Array.Empty<int>();
    private BasicBlockClass[] type = Array.Empty<BasicBlockClass>();
    private int[] last = Array.Empty<int>();
    private UnionFindNode[] nodes = Array.Empty<UnionFindNode>();

    public HavlakLoopFinder(ControlFlowGraph cfg, LoopStructureGraph lsg)
    {
      this.cfg = cfg;
      this.lsg = lsg;
    }

    // Basic Blocks and Loops are being classified as regular, irreducible,
    // and so on. This enum contains a symbolic name for all these
    // classifications.
    private enum BasicBlockClass
    {
      BbTop,          // uninitialized
      BbNonHeader,    // a regular BB
      BbReducible,    // reducible loop
      BbSelf,         // single BB loop
      BbIrreducible,  // irreducible loop
      BbDead,         // a dead BB
      BbLast          // Sentinel
    }

    // As described in the paper, determine whether a node 'w' is a
    // "true" ancestor for node 'v'.
    //
    // Dominance can be tested quickly using a pre-order trick
    // for depth-first spanning trees. This is why DFS is the first
    // thing we run below.
    private bool IsAncestor(int w, int v)
    {
      return w <= v && v <= last[w];
    }

    // DFS - Depth-First-Search
    //
    // Simple depth first traversal along out edges with node numbering.
    private int DoDFS(BasicBlock currentNode, int current)
    {
      nodes[current].InitNode(currentNode, current);
      number.AtPut(currentNode, current);

      int lastId = current;
      Vector<BasicBlock> outerBlocks = currentNode.OutEdges;

      for (int i = 0; i < outerBlocks.Size(); i++) {
        BasicBlock target = outerBlocks.At(i)!;
        if ((int)(number.At(target) ?? 0) == UNVISITED) {
          lastId = DoDFS(target, lastId + 1);
        }
      }

      last[current] = lastId;
      return lastId;
    }

    private void InitAllNodes()
    {
      // Step a:
      //   - initialize all nodes as unvisited.
      //   - depth-first traversal and numbering.
      //   - unreached BB's are marked as dead.
      cfg.BasicBlocks.ForEach(bb => number.AtPut(bb, UNVISITED));

      DoDFS(cfg.GetStartBasicBlock()!, 0);
    }

    private void IdentifyEdges(int size)
    {
      // Step b:
      //   - iterate over all nodes.
      //
      //   A backedge comes from a descendant in the DFS tree, and non-backedges
      //   from non-descendants (following Tarjan).
      //
      //   - check incoming edges 'v' and add them to either
      //     - the list of backedges (backPreds) or
      //     - the list of non-backedges (nonBackPreds)
      for (int w = 0; w < size; w++) {
        header[w] = 0;
        type[w] = BasicBlockClass.BbNonHeader;

        BasicBlock? nodeW = nodes[w].Bb;
        if (nodeW == null) {
          type[w] = BasicBlockClass.BbDead;
        } else {
          ProcessEdges(nodeW, w);
        }
      }
    }

    private void ProcessEdges(BasicBlock nodeW, int w)
    {
      if (nodeW.GetNumPred() > 0) {
        nodeW.InEdges.ForEach(nodeV => {
          int v = (int)(number.At(nodeV) ?? 0);
          if (v != UNVISITED) {
            if (IsAncestor(w, v)) {
              backPreds.At(w)!.Append(v);
            } else {
              nonBackPreds.At(w)!.Add(v);
            }
          }
        });
      }
    }

    // findLoops
    //
    // Find loops and build loop forest using Havlak's algorithm, which
    // is derived from Tarjan. Variable names and step numbering has
    // been chosen to be identical to the nomenclature in Havlak's
    // paper (which, in turn, is similar to the one used by Tarjan).
    public void FindLoops()
    {
      if (cfg.GetStartBasicBlock() == null) {
        return;
      }

      int size = cfg.GetNumNodes();

      nonBackPreds.RemoveAll();
      backPreds.RemoveAll();
      number.RemoveAll();
      if (size > maxSize) {
        header = new int[size];
        type = new BasicBlockClass[size];
        last = new int[size];
        nodes = new UnionFindNode[size];
        maxSize = size;
      }

      for (int i = 0; i < size; ++i) {
        nonBackPreds.Append(new Set<object>());
        backPreds.Append(new Vector<object>());
        nodes[i] = new UnionFindNode();
      }

      InitAllNodes();
      IdentifyEdges(size);

      // Start node is root of all other loops.
      header[0] = 0;

      // Step c:
      //
      // The outer loop, unchanged from Tarjan. It does nothing except
      // for those nodes which are the destinations of backedges.
      // For a header node w, we chase backward from the sources of the
      // backedges adding nodes to the set P, representing the body of
      // the loop headed by w.
      //
      // By running through the nodes in reverse of the DFST preorder,
      // we ensure that inner loop headers will be processed before the
      // headers for surrounding loops.
      for (int w = size - 1; w >= 0; w--) {
        // this is 'P' in Havlak's paper
        Vector<UnionFindNode> nodePool = new Vector<UnionFindNode>();

        BasicBlock? nodeW = nodes[w].Bb;
        if (nodeW != null) {
          StepD(w, nodePool);

          // Copy nodePool to workList.
          Vector<UnionFindNode> workList = new Vector<UnionFindNode>();
          nodePool.ForEach(niter => workList.Append(niter));

          if (nodePool.Size() != 0) {
            type[w] = BasicBlockClass.BbReducible;
          }

          // work the list...
          while (!workList.IsEmpty()) {
            UnionFindNode x = workList.RemoveFirst()!;

            // Step e:
            //
            // Step e represents the main difference from Tarjan's method.
            // Chasing upwards from the sources of a node w's backedges. If
            // there is a node y' that is not a descendant of w, w is marked
            // the header of an irreducible loop, there is another entry
            // into this loop that avoids w.

            // The algorithm has degenerated. Break and
            // return in this case.
            int nonBackSize = nonBackPreds.At(x.DfsNumber)!.Size();
            if (nonBackSize > MAX_NON_BACK_PREDS) {
              return;
            }
            StepEProcessNonBackPreds(w, nodePool, workList, x);
          }

          // Collapse/Unionize nodes in a SCC to a single node
          // For every SCC found, create a loop descriptor and link it in.
          if ((nodePool.Size() > 0) || (type[w] == BasicBlockClass.BbSelf)) {
            SimpleLoop loop = lsg.CreateLoop(nodeW, type[w] != BasicBlockClass.BbIrreducible);
            SetLoopAttributes(w, nodePool, loop);
          }
        }
      }  // Step c
    }  // findLoops

    private void StepEProcessNonBackPreds(int w, Vector<UnionFindNode> nodePool,
        Vector<UnionFindNode> workList, UnionFindNode x)
    {
      nonBackPreds.At(x.DfsNumber)!.ForEach(iter => {
        UnionFindNode y = nodes[(int)iter];
        UnionFindNode ydash = y.FindSet();

        if (!IsAncestor(w, ydash.DfsNumber)) {
          type[w] = BasicBlockClass.BbIrreducible;
          nonBackPreds.At(w)!.Add(ydash.DfsNumber);
        } else {
          if (ydash.DfsNumber != w) {
            if (!nodePool.HasSome(e => e == ydash)) {
              workList.Append(ydash);
              nodePool.Append(ydash);
            }
          }
        }
      });
    }

    private void SetLoopAttributes(int w, Vector<UnionFindNode> nodePool, SimpleLoop loop)
    {
      // At this point, one can set attributes to the loop, such as:
      //
      // the bottom node:
      //    iter  = backPreds[w].begin();
      //    loop bottom is: nodes[iter].node);
      //
      // the number of backedges:
      //    backPreds[w].size()
      //
      // whether this loop is reducible:
      //    type[w] != BasicBlockClass.BB_IRREDUCIBLE
      nodes[w].Loop = loop;

      nodePool.ForEach(node => {
        // Add nodes to loop descriptor.
        header[node.DfsNumber] = w;
        node.Union(nodes[w]);

        // Nested loops are not added, but linked together.
        SimpleLoop? nodeLoop = node.Loop;
        if (nodeLoop != null) {
          nodeLoop.Parent = loop;
        } else {
          loop.AddNode(node.Bb!);
        }
      });
    }

    private void StepD(int w, Vector<UnionFindNode> nodePool)
    {
      backPreds.At(w)!.ForEach(v => {
        if ((int)v != w) {
          nodePool.Append(nodes[(int)v].FindSet());
        } else {
          type[w] = BasicBlockClass.BbSelf;
        }
      });
    }
  }

  sealed class LoopTesterApp
  {
    private readonly ControlFlowGraph cfg;
    private readonly LoopStructureGraph lsg;

    public LoopTesterApp()
    {
      cfg = new ControlFlowGraph();
      lsg = new LoopStructureGraph();
      cfg.CreateNode(0);
    }

    // Create 4 basic blocks, corresponding to and if/then/else clause
    // with a CFG that looks like a diamond
    private int BuildDiamond(int start)
    {
      int bb0 = start;
      new BasicBlockEdge(cfg, bb0, bb0 + 1);
      new BasicBlockEdge(cfg, bb0, bb0 + 2);
      new BasicBlockEdge(cfg, bb0 + 1, bb0 + 3);
      new BasicBlockEdge(cfg, bb0 + 2, bb0 + 3);

      return bb0 + 3;
    }

    // Connect two existing nodes
    private void BuildConnect(int start, int end)
    {
      new BasicBlockEdge(cfg, start, end);
    }

    // Form a straight connected sequence of n basic blocks
    private int BuildStraight(int start, int n)
    {
      for (int i = 0; i < n; i++) {
        BuildConnect(start + i, start + i + 1);
      }
      return start + n;
    }

    // Construct a simple loop with two diamonds in it
    private int BuildBaseLoop(int from)
    {
      int header = BuildStraight(from, 1);
      int diamond1 = BuildDiamond(header);
      int d11 = BuildStraight(diamond1, 1);
      int diamond2 = BuildDiamond(d11);
      int footer = BuildStraight(diamond2, 1);
      BuildConnect(diamond2, d11);
      BuildConnect(diamond1, header);

      BuildConnect(footer, from);
      footer = BuildStraight(footer, 1);
      return footer;
    }

    public int[] Main(int numDummyLoops, int findLoopIterations,
        int parLoops, int pparLoops, int ppparLoops)
    {
      ConstructSimpleCFG();
      AddDummyLoops(numDummyLoops);
      ConstructCFG(parLoops, pparLoops, ppparLoops);

      // Performing Loop Recognition, 1 Iteration, then findLoopIteration
      FindLoops(lsg);
      for (int i = 0; i < findLoopIterations; i++) {
        FindLoops(new LoopStructureGraph());
      }

      lsg.CalculateNestingLevel();
      return new int[] { lsg.GetNumLoops(), cfg.GetNumNodes() };
    }

    private void ConstructCFG(int parLoops, int pparLoops, int ppparLoops)
    {
      int n = 2;

      for (int parlooptrees = 0; parlooptrees < parLoops; parlooptrees++) {
        cfg.CreateNode(n + 1);
        BuildConnect(2, n + 1);
        n += 1;

        for (int i = 0; i < pparLoops; i++) {
          int top = n;
          n = BuildStraight(n, 1);
          for (int j = 0; j < ppparLoops; j++) {
            n = BuildBaseLoop(n);
          }
          int bottom = BuildStraight(n, 1);
          BuildConnect(n, top);
          n = bottom;
        }
        BuildConnect(n, 1);
      }
    }

    private void AddDummyLoops(int numDummyLoops)
    {
      for (int dummyloop = 0; dummyloop < numDummyLoops; dummyloop++) {
        FindLoops(lsg);
      }
    }

    private void FindLoops(LoopStructureGraph loopStructure)
    {
      HavlakLoopFinder finder = new HavlakLoopFinder(cfg, loopStructure);
      finder.FindLoops();
    }

    private void ConstructSimpleCFG()
    {
      cfg.CreateNode(0);
      BuildBaseLoop(0);
      cfg.CreateNode(1);
      new BasicBlockEdge(cfg, 0, 2);
    }
  }
}