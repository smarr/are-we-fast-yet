using Benchmarks.Som;

namespace Benchmarks;

internal class Havlak : Benchmark
{
    public override bool InnerBenchmarkLoop(int innerIterations) 
        => VerifyResult(new LoopTesterApp().Main(innerIterations, 50, 10, 10, 5), innerIterations);

    public bool VerifyResult(int[] r, int innerIterations)
    {
        if (innerIterations == 15000)
        {
            return r[0] == 46602 && r[1] == 5213;
        }

        if (innerIterations == 1500)
        {
            return r[0] == 6102 && r[1] == 5213;
        }

        if (innerIterations == 150)
        {
            return r[0] == 2052 && r[1] == 5213;
        }

        if (innerIterations == 15)
        {
            return r[0] == 1647 && r[1] == 5213;
        }

        if (innerIterations == 1)
        {
            return r[0] == 1605 && r[1] == 5213;
        }

        Console.WriteLine($"No verification result for {innerIterations} found");
        Console.WriteLine($"Result is: {r[0]}, {r[1]}");

        return false;
    }

    public override object Execute() => throw new InvalidOperationException("Execute should not be called for Havlak benchmark.");

    public override bool VerifyResult(object result) => throw new InvalidOperationException("VerifyResult(object) should not be called for Havlak benchmark.");
}

internal sealed class BasicBlock : ICustomHash
{
    public Vector<BasicBlock> InEdges { get; }
    public Vector<BasicBlock> OutEdges { get; }

    private readonly int _name;

    public BasicBlock(int name)
    {
        _name = name;
        InEdges = new Vector<BasicBlock>(2);
        OutEdges = new Vector<BasicBlock>(2);
    }

    public int GetNumPred() => InEdges.Size();

    public void AddOutEdge(BasicBlock to) => OutEdges.Append(to);

    public void AddInEdge(BasicBlock from) => InEdges.Append(from);

    public int CustomHash() => _name;
}

internal sealed class BasicBlockEdge
{
    private readonly BasicBlock _from;
    private readonly BasicBlock _to;

    public BasicBlockEdge(ControlFlowGraph cfg, int fromName, int toName)
    {
        _from = cfg.CreateNode(fromName);
        _to = cfg.CreateNode(toName);

        _from.AddOutEdge(_to);
        _to.AddInEdge(_from);

        cfg.AddEdge(this);
    }
}

internal sealed class ControlFlowGraph
{
    public Vector<BasicBlock> BasicBlocks { get; } = new();

    private BasicBlock? _startNode;
    private readonly Vector<BasicBlockEdge> _edgeList = new();

    public BasicBlock CreateNode(int name)
    {
        BasicBlock node;

        if (BasicBlocks.At(name) != null)
        {
            node = BasicBlocks.At(name)!;
        }
        else
        {
            node = new BasicBlock(name);
            BasicBlocks.AtPut(name, node);
        }

        if (GetNumNodes() == 1)
        {
            _startNode = node;
        }

        return node;
    }

    public void AddEdge(BasicBlockEdge edge) => _edgeList.Append(edge);

    public int GetNumNodes() => BasicBlocks.Size();

    public BasicBlock? GetStartBasicBlock() => _startNode;
}

internal sealed class HavlakLoopFinder
{
    private readonly ControlFlowGraph _cfg; // Control Flow Graph
    private readonly LoopStructureGraph _lsg; // Loop Structure Graph

    // Marker for uninitialized nodes.
    private const int Unvisited = int.MaxValue;

    // Safeguard against pathological algorithm behavior.
    private const int MaxNonBackPreds = 32 * 1024;

    private readonly Vector<Set<int>> _nonBackPreds = new();
    private readonly Vector<Vector<int>> _backPreds = new();
    private readonly IdentityDictionary<BasicBlock, int> _number = new();
    private int _maxSize;
    private int[]? _header;
    private BasicBlockClass[]? _type;
    private int[]? _last;
    private UnionFindNode[]? _nodes;

    public HavlakLoopFinder(ControlFlowGraph cfg, LoopStructureGraph lsg)
    {
        _cfg = cfg;
        _lsg = lsg;
    }

    // Basic Blocks and Loops are being classified as regular, irreducible,
    // and so on. This enum contains a symbolic name for all these
    // classifications.
    private enum BasicBlockClass
    {
        BbTop, // uninitialized
        BbNonHeader, // a regular BB
        BbReducible, // reducible loop
        BbSelf, // single BB loop
        BbIrreducible, // irreducible loop
        BbDead, // a dead BB
        BbLast // Sentinel
    }

    // As described in the paper, determine whether a node 'w' is a
    // "true" ancestor for node 'v'.
    //
    // Dominance can be tested quickly using a pre-order trick
    // for depth-first spanning trees. This is why DFS is the first
    // thing we run below.
    private bool IsAncestor(int w, int v) => w <= v && v <= _last![w];

    // DFS - Depth-First-Search
    //
    // Simple depth first traversal along out edges with node numbering.
    // ReSharper disable once InconsistentNaming
    private int DoDFS(BasicBlock currentNode, int current)
    {
        _nodes![current].InitNode(currentNode, current);
        _number.AtPut(currentNode, current);

        int lastId = current;
        Vector<BasicBlock> outerBlocks = currentNode.OutEdges;

        for (int i = 0; i < outerBlocks.Size(); i++)
        {
            BasicBlock? target = outerBlocks.At(i);
            if (_number.At(target!) == Unvisited)
            {
                lastId = DoDFS(target!, lastId + 1);
            }
        }

        _last![current] = lastId;
        return lastId;
    }

    private void InitAllNodes()
    {
        // Step a:
        //   - initialize all nodes as unvisited.
        //   - depth-first traversal and numbering.
        //   - unreached BB's are marked as dead.
        _cfg.BasicBlocks.ForEach(bb => _number.AtPut(bb, Unvisited));

        DoDFS(_cfg.GetStartBasicBlock()!, 0);
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
        for (int w = 0; w < size; w++)
        {
            _header![w] = 0;
            _type![w] = BasicBlockClass.BbNonHeader;

            BasicBlock? nodeW = _nodes![w].Bb;
            if (nodeW == null)
            {
                _type[w] = BasicBlockClass.BbDead;
            }
            else
            {
                ProcessEdges(nodeW, w);
            }
        }
    }

    private void ProcessEdges(BasicBlock nodeW, int w)
    {
        if (nodeW.GetNumPred() > 0)
        {
            nodeW.InEdges.ForEach(nodeV =>
            {
                int v = _number.At(nodeV);
                if (v != Unvisited)
                {
                    if (IsAncestor(w, v))
                    {
                        _backPreds.At(w)!.Append(v);
                    }
                    else
                    {
                        _nonBackPreds.At(w)!.Add(v);
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
        if (_cfg.GetStartBasicBlock() == null)
        {
            return;
        }

        int size = _cfg.GetNumNodes();

        _nonBackPreds.RemoveAll();
        _backPreds.RemoveAll();
        _number.RemoveAll();
        if (size > _maxSize)
        {
            _header = new int[size];
            _type = new BasicBlockClass[size];
            _last = new int[size];
            _nodes = new UnionFindNode[size];
            _maxSize = size;
        }

        for (int i = 0; i < size; ++i)
        {
            _nonBackPreds.Append(new Set<int>());
            _backPreds.Append(new Vector<int>());
            _nodes![i] = new UnionFindNode();
        }

        InitAllNodes();
        IdentifyEdges(size);

        // Start node is root of all other loops.
        _header![0] = 0;

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
        for (int w = size - 1; w >= 0; w--)
        {
            // this is 'P' in Havlak's paper
            Vector<UnionFindNode> nodePool = new();

            BasicBlock? nodeW = _nodes![w].Bb;
            if (nodeW != null)
            {
                StepD(w, nodePool);

                // Copy nodePool to workList.
                Vector<UnionFindNode> workList = new();
                nodePool.ForEach(workList.Append);

                if (nodePool.Size() != 0)
                {
                    _type![w] = BasicBlockClass.BbReducible;
                }

                // work the list...
                while (!workList.IsEmpty())
                {
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
                    int nonBackSize = _nonBackPreds.At(x.DfsNumber)!.Size();
                    if (nonBackSize > MaxNonBackPreds)
                    {
                        return;
                    }

                    StepEProcessNonBackPreds(w, nodePool, workList, x);
                }

                // Collapse/Unionize nodes in a SCC to a single node
                // For every SCC found, create a loop descriptor and link it in.
                if (nodePool.Size() > 0 || _type![w] == BasicBlockClass.BbSelf)
                {
                    SimpleLoop loop = _lsg.CreateLoop(nodeW, _type![w] != BasicBlockClass.BbIrreducible);
                    SetLoopAttributes(w, nodePool, loop);
                }
            }
        } // Step c
    } // findLoops

    private void StepEProcessNonBackPreds(int w, Vector<UnionFindNode> nodePool,
        Vector<UnionFindNode> workList, UnionFindNode x)
    {
        _nonBackPreds.At(x.DfsNumber)!.ForEach(iter =>
        {
            UnionFindNode y = _nodes![iter];
            UnionFindNode ydash = y.FindSet();

            if (!IsAncestor(w, ydash.DfsNumber))
            {
                _type![w] = BasicBlockClass.BbIrreducible;
                _nonBackPreds.At(w)!.Add(ydash.DfsNumber);
            }
            else
            {
                if (ydash.DfsNumber != w)
                {
                    if (!nodePool.HasSome(e => e == ydash))
                    {
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
        _nodes![w].Loop = loop;

        nodePool.ForEach(node =>
        {
            // Add nodes to loop descriptor.
            _header![node.DfsNumber] = w;
            node.Union(_nodes[w]);

            // Nested loops are not added, but linked together.
            if (node.Loop != null)
            {
                node.Loop.SetParent(loop);
            }
            else
            {
                loop.AddNode(node.Bb!);
            }
        });
    }

    private void StepD(int w, Vector<UnionFindNode> nodePool)
    {
        _backPreds.At(w)!.ForEach(v =>
        {
            if (v != w)
            {
                nodePool.Append(_nodes![v].FindSet());
            }
            else
            {
                _type![w] = BasicBlockClass.BbSelf;
            }
        });
    }
}

internal sealed class LoopStructureGraph
{
    private readonly SimpleLoop _root;
    private readonly Vector<SimpleLoop> _loops;

    private int _loopCounter;

    public LoopStructureGraph()
    {
        _loopCounter = 0;
        _loops = new Vector<SimpleLoop>();
        _root = new SimpleLoop(null, true) { NestingLevel = 0, Counter = _loopCounter };
        _loopCounter += 1;

        _loops.Append(_root);
    }

    public SimpleLoop CreateLoop(BasicBlock bb, bool isReducible)
    {
        SimpleLoop loop = new(bb, isReducible) { Counter = _loopCounter };
        _loopCounter += 1;
        _loops.Append(loop);
        return loop;
    }

    public void CalculateNestingLevel()
    {
        // link up all 1st level loops to artificial root node.
        _loops.ForEach(liter =>
        {
            if (!liter.IsRoot && liter.Parent == null)
            {
                liter.SetParent(_root);
            }
        });

        // recursively traverse the tree and assign levels.
        CalculateNestingLevelRec(_root, 0);
    }

    private void CalculateNestingLevelRec(SimpleLoop loop, int depth)
    {
        loop.DepthLevel = depth;
        loop.Children.ForEach(liter =>
        {
            CalculateNestingLevelRec(liter, depth + 1);

            loop.NestingLevel = Math.Max(loop.NestingLevel, 1 + liter.NestingLevel);
        });
    }

    public int GetNumLoops() => _loops.Size();
}

internal sealed class LoopTesterApp
{
    private readonly ControlFlowGraph _cfg;
    private readonly LoopStructureGraph _lsg;

    public LoopTesterApp()
    {
        _cfg = new ControlFlowGraph();
        _lsg = new LoopStructureGraph();
        _cfg.CreateNode(0);
    }

    // Create 4 basic blocks, corresponding to and if/then/else clause
    // with a CFG that looks like a diamond
    private int BuildDiamond(int start)
    {
        int bb0 = start;
        new BasicBlockEdge(_cfg, bb0, bb0 + 1);
        new BasicBlockEdge(_cfg, bb0, bb0 + 2);
        new BasicBlockEdge(_cfg, bb0 + 1, bb0 + 3);
        new BasicBlockEdge(_cfg, bb0 + 2, bb0 + 3);

        return bb0 + 3;
    }

    // Connect two existing nodes
    private void BuildConnect(int start, int end) => new BasicBlockEdge(_cfg, start, end);

    // Form a straight connected sequence of n basic blocks
    private int BuildStraight(int start, int n)
    {
        for (int i = 0; i < n; i++)
        {
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
        FindLoops(_lsg);
        for (int i = 0; i < findLoopIterations; i++)
        {
            FindLoops(new LoopStructureGraph());
        }

        _lsg.CalculateNestingLevel();
        return [_lsg.GetNumLoops(), _cfg.GetNumNodes()];
    }

    // ReSharper disable once InconsistentNaming
    private void ConstructCFG(int parLoops, int pparLoops, int ppparLoops)
    {
        int n = 2;

        for (int parlooptrees = 0; parlooptrees < parLoops; parlooptrees++)
        {
            _cfg.CreateNode(n + 1);
            BuildConnect(2, n + 1);
            n += 1;

            for (int i = 0; i < pparLoops; i++)
            {
                int top = n;
                n = BuildStraight(n, 1);
                for (int j = 0; j < ppparLoops; j++)
                {
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
        for (int dummyloop = 0; dummyloop < numDummyLoops; dummyloop++)
        {
            FindLoops(_lsg);
        }
    }

    private void FindLoops(LoopStructureGraph loopStructure)
    {
        HavlakLoopFinder finder = new(_cfg, loopStructure);
        finder.FindLoops();
    }

    // ReSharper disable once InconsistentNaming
    private void ConstructSimpleCFG()
    {
        _cfg.CreateNode(0);
        BuildBaseLoop(0);
        _cfg.CreateNode(1);
        new BasicBlockEdge(_cfg, 0, 2);
    }
}

internal sealed class SimpleLoop
{
    private readonly IdentitySet<BasicBlock> _basicBlocks;
    private int _nestingLevel;

    private readonly BasicBlock? _header;

    private readonly bool _isReducible;

    public SimpleLoop? Parent { get; private set; }
    public IdentitySet<SimpleLoop> Children { get; }
    public int Counter { get; set; }
    public int DepthLevel { get; set; }
    public bool IsRoot { get; set; }

    public SimpleLoop(BasicBlock? bb, bool isReducible)
    {
        _isReducible = isReducible;
        Parent = null;
        IsRoot = false;
        _nestingLevel = 0;
        DepthLevel = 0;
        _basicBlocks = new IdentitySet<BasicBlock>();
        Children = new IdentitySet<SimpleLoop>();

        if (bb != null)
        {
            _basicBlocks.Add(bb);
        }

        _header = bb;
    }

    public void AddNode(BasicBlock bb) => _basicBlocks.Add(bb);

    private void AddChildLoop(SimpleLoop loop) => Children.Add(loop);

    public void SetParent(SimpleLoop? p)
    {
        Parent = p;
        Parent!.AddChildLoop(this);
    }

    public int NestingLevel
    {
        get => _nestingLevel;
        set
        {
            _nestingLevel = value;
            if (value == 0)
            {
                IsRoot = true;
            }
        }
    }
}

internal sealed class UnionFindNode
{
    private UnionFindNode? _parent;

    public BasicBlock? Bb { get; private set; }
    public SimpleLoop? Loop { get; set; }
    public int DfsNumber { get; private set; }

    // Initialize this node.
    public void InitNode(BasicBlock bb, int dfsNumber)
    {
        _parent = this;
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
        Vector<UnionFindNode> nodeList = new();

        UnionFindNode node = this;
        while (node != node._parent)
        {
            if (node._parent != node._parent!._parent)
            {
                nodeList.Append(node);
            }

            node = node._parent;
        }

        // Path Compression, all nodes' parents point to the 1st level parent.
        nodeList.ForEach(iter => iter.Union(_parent!));
        return node;
    }

    // Union/Find Algorithm - The union routine.
    //
    // Trivial. Assigning parent pointer is enough,
    // we rely on path compression.
    public void Union(UnionFindNode basicBlock)
    {
        _parent = basicBlock;
    }
}