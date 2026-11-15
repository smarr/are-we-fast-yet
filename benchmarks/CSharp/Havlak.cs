namespace Benchmarks;

public class Havlak : Benchmark
{
  public override bool InnerBenchmarkLoop(int innerIterations)
  {
    return VerifyResult((new LoopTesterApp()).main(innerIterations, 50, 10 /* was 100 */, 10, 5), innerIterations);
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

    BasicBlockEdge(ControlFlowGraph cfg, int fromName, int toName)
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

    ControlFlowGraph()
    {
      BasicBlocks = new Vector<BasicBlock>();
      edgeList = new Vector<BasicBlockEdge>();
    }

    public BasicBlock CreateNode(int name)
    {
      BasicBlock node;

      if (BasicBlocks.At(name) != null) {
        node = BasicBlocks.At(name);
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

    LoopStructureGraph()
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
    private readonly IdentitySet<SimpleLoop> children;
    private SimpleLoop? parent;

    private readonly BasicBlock? header;

    private readonly bool isReducible;

    private bool isRoot;
    private int nestingLevel;

    private int counter;
    private int depthLevel;

    SimpleLoop(BasicBlock bb, bool isReducible)
    {
      this.isReducible = isReducible;
      parent = null;
      isRoot = false;
      nestingLevel = 0;
      depthLevel = 0;
      basicBlocks = new IdentitySet<BasicBlock>();
      children = new IdentitySet<SimpleLoop>();

      if (bb != null) {
        basicBlocks.Add(bb);
      }

      header = bb;
    }
  }
}