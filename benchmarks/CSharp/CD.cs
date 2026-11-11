using Benchmarks.Som;

namespace Benchmarks;

// ReSharper disable once InconsistentNaming
internal sealed class CD : Benchmark
{
    private int CalculateCollisions(int numAircraft)
    {
        const int NumFrames = 200;

        Simulator simulator = new(numAircraft);
        CollisionDetector detector = new();

        int actualCollisions = 0;

        for (int i = 0; i < NumFrames; i++)
        {
            double time = i / 10.0;
            Vector<Collision> collisions = detector.HandleNewFrame(simulator.Simulate(time));
            actualCollisions += collisions.Size();
        }

        return actualCollisions;
    }

    public override bool InnerBenchmarkLoop(int innerIterations) => VerifyResult(CalculateCollisions(innerIterations), innerIterations);

    public bool VerifyResult(int actualCollisions, int numAircraft)
    {
        if (numAircraft == 1000)
        {
            return actualCollisions == 14484;
        }

        if (numAircraft == 500)
        {
            return actualCollisions == 14484;
        }

        if (numAircraft == 250)
        {
            return actualCollisions == 10830;
        }

        if (numAircraft == 200)
        {
            return actualCollisions == 8655;
        }

        if (numAircraft == 100)
        {
            return actualCollisions == 4305;
        }

        if (numAircraft == 10)
        {
            return actualCollisions == 390;
        }

        if (numAircraft == 2)
        {
            return actualCollisions == 42;
        }

        Console.WriteLine($"No verification result for {numAircraft} found");
        Console.WriteLine($"Result is: {actualCollisions}");
        return false;
    }

    public override object Execute() => throw new InvalidOperationException("Execute should not be called for CD benchmark.");

    public override bool VerifyResult(object result) => throw new InvalidOperationException("VerifyResult(object) should not be called for CD benchmark.");
}

internal sealed class Aircraft
{
    public readonly CallSign CallSign;
    public readonly Vector3D Position;

    public Aircraft(CallSign callSign, Vector3D position)
    {
        CallSign = callSign;
        Position = position;
    }
}

internal class CallSign : IComparable<CallSign>
{
    private readonly int _value;

    public CallSign(int value)
    {
        _value = value;
    }

    public int CompareTo(CallSign? other)
    {
        if (other == null)
        {
            return 1;
        }

        return _value == other._value ? 0 : _value < other._value ? -1 : 1;
    }
}

internal sealed class Collision
{
    public readonly CallSign AircraftA;
    public readonly CallSign AircraftB;
    public readonly Vector3D Position;

    public Collision(CallSign aircraftA, CallSign aircraftB, Vector3D position)
    {
        AircraftA = aircraftA;
        AircraftB = aircraftB;
        Position = position;
    }
}

internal sealed class CollisionDetector
{
    private readonly RedBlackTree<CallSign, Vector3D> _state = new();

    public Vector<Collision> HandleNewFrame(Vector<Aircraft> frame)
    {
        Vector<Motion> motions = new();
        RedBlackTree<CallSign, bool> seen = new();

        frame.ForEach(aircraft =>
        {
            Vector3D? oldPosition = _state.Put(aircraft.CallSign, aircraft.Position);
            Vector3D newPosition = aircraft.Position;
            seen.Put(aircraft.CallSign, true);

            oldPosition ??= newPosition;

            motions.Append(new Motion(aircraft.CallSign, oldPosition, newPosition));
        });

        // Remove aircraft that are no longer present.
        Vector<CallSign> toRemove = new();
        _state.ForEach(e =>
        {
            if (!seen.Get(e.Key))
            {
                toRemove.Append(e.Key);
            }
        });

        toRemove.ForEach(e => _state.Remove(e));

        Vector<Vector<Motion>> allReduced = ReduceCollisionSet(motions);
        Vector<Collision> collisions = new();
        allReduced.ForEach(reduced =>
        {
            for (int i = 0; i < reduced.Size(); ++i)
            {
                Motion? motion1 = reduced.At(i);
                for (int j = i + 1; j < reduced.Size(); ++j)
                {
                    Motion? motion2 = reduced.At(j);
                    Vector3D? collision = motion1!.FindIntersection(motion2!);
                    if (collision != null)
                    {
                        collisions.Append(new Collision(motion1.CallSign, motion2!.CallSign, collision));
                    }
                }
            }
        });

        return collisions;
    }

    private static bool IsInVoxel(Vector2D voxel, Motion motion)
    {
        if (voxel.X > Constants.MaxX ||
            voxel.X < Constants.MinX ||
            voxel.Y > Constants.MaxY ||
            voxel.Y < Constants.MinY)
        {
            return false;
        }

        Vector3D init = motion.PosOne;
        Vector3D fin = motion.PosTwo;

        // ReSharper disable once InconsistentNaming
        const double VS = Constants.GoodVoxelSize;
        const double R = Constants.ProximityRadius / 2.0;

        double vX = voxel.X;
        double x0 = init.X;
        double xv = fin.X - init.X;

        double vY = voxel.Y;
        double y0 = init.Y;
        double yv = fin.Y - init.Y;

        double lowX = (vX - R - x0) / xv;
        double highX = (vX + VS + R - x0) / xv;

        if (xv < 0.0)
        {
            double tmp = lowX;
            lowX = highX;
            highX = tmp;
        }

        double lowY = (vY - R - y0) / yv;
        double highY = (vY + VS + R - y0) / yv;

        if (yv < 0.0)
        {
            double tmp = lowY;
            lowY = highY;
            highY = tmp;
        }

        return ((xv == 0.0 && vX <= x0 + R && x0 - R <= vX + VS) /* no motion in x */ ||
                (lowX <= 1.0 && 1.0 <= highX) || (lowX <= 0.0 && 0.0 <= highX) ||
                (0.0 <= lowX && highX <= 1.0)) &&
               ((yv == 0.0 && vY <= y0 + R && y0 - R <= vY + VS) /* no motion in y */ ||
                (lowY <= 1.0 && 1.0 <= highY) || (lowY <= 0.0 && 0.0 <= highY) ||
                (0.0 <= lowY && highY <= 1.0)) &&
               (xv == 0.0 || yv == 0.0 || /* no motion in x or y or both */
                (lowY <= highX && highX <= highY) ||
                (lowY <= lowX && lowX <= highY) ||
                (lowX <= lowY && highY <= highX));
    }

    private static readonly Vector2D _horizontal = new(Constants.GoodVoxelSize, 0.0);
    private static readonly Vector2D _vertical = new(0.0, Constants.GoodVoxelSize);

    private static void PutIntoMap(RedBlackTree<Vector2D, Vector<Motion>> voxelMap, Vector2D voxel, Motion motion)
    {
        Vector<Motion>? array = voxelMap.Get(voxel);
        if (array == null)
        {
            array = new Vector<Motion>();
            voxelMap.Put(voxel, array);
        }

        array.Append(motion);
    }

    private static void Recurse(
        RedBlackTree<Vector2D, Vector<Motion>> voxelMap,
        RedBlackTree<Vector2D, bool> seen,
        Vector2D nextVoxel, Motion motion)
    {
        if (!IsInVoxel(nextVoxel, motion))
        {
            return;
        }

        if (seen.Put(nextVoxel, true))
        {
            return;
        }

        PutIntoMap(voxelMap, nextVoxel, motion);

        Recurse(voxelMap, seen, nextVoxel.Minus(_horizontal), motion);
        Recurse(voxelMap, seen, nextVoxel.Plus(_horizontal), motion);
        Recurse(voxelMap, seen, nextVoxel.Minus(_vertical), motion);
        Recurse(voxelMap, seen, nextVoxel.Plus(_vertical), motion);
        Recurse(voxelMap, seen, nextVoxel.Minus(_horizontal).Minus(_vertical), motion);
        Recurse(voxelMap, seen, nextVoxel.Minus(_horizontal).Plus(_vertical), motion);
        Recurse(voxelMap, seen, nextVoxel.Plus(_horizontal).Minus(_vertical), motion);
        Recurse(voxelMap, seen, nextVoxel.Plus(_horizontal).Plus(_vertical), motion);
    }

    private static Vector<Vector<Motion>> ReduceCollisionSet(Vector<Motion> motions)
    {
        RedBlackTree<Vector2D, Vector<Motion>> voxelMap = new();
        motions.ForEach(motion => DrawMotionOnVoxelMap(voxelMap, motion));

        Vector<Vector<Motion>> result = new();
        voxelMap.ForEach(e =>
        {
            if (e.Value.Size() > 1)
            {
                result.Append(e.Value);
            }
        });
        return result;
    }

    private static Vector2D VoxelHash(Vector3D position)
    {
        int xDiv = (int)(position.X / Constants.GoodVoxelSize);
        int yDiv = (int)(position.Y / Constants.GoodVoxelSize);

        double x = Constants.GoodVoxelSize * xDiv;
        double y = Constants.GoodVoxelSize * yDiv;

        if (position.X < 0)
        {
            x -= Constants.GoodVoxelSize;
        }

        if (position.Y < 0)
        {
            y -= Constants.GoodVoxelSize;
        }

        return new Vector2D(x, y);
    }

    private static void DrawMotionOnVoxelMap(RedBlackTree<Vector2D, Vector<Motion>> voxelMap, Motion motion)
    {
        RedBlackTree<Vector2D, bool> seen = new();
        Recurse(voxelMap, seen, VoxelHash(motion.PosOne), motion);
    }
}

internal sealed class Constants
{
    public const double MinX = 0.0;
    public const double MinY = 0.0;
    public const double MaxX = 1000.0;
    public const double MaxY = 1000.0;
    public const double MinZ = 0.0;
    public const double MaxZ = 10.0;
    public const double ProximityRadius = 1.0;
    public const double GoodVoxelSize = ProximityRadius * 2.0;
}

internal sealed class Motion
{
    public readonly CallSign CallSign;
    public readonly Vector3D PosOne;
    public readonly Vector3D PosTwo;

    public Motion(CallSign callSign, Vector3D posOne, Vector3D posTwo)
    {
        CallSign = callSign;
        PosOne = posOne;
        PosTwo = posTwo;
    }

    private Vector3D Delta() => PosTwo.Minus(PosOne);

    public Vector3D? FindIntersection(Motion other)
    {
        Vector3D init1 = PosOne;
        Vector3D init2 = other.PosOne;
        Vector3D vec1 = Delta();
        Vector3D vec2 = other.Delta();
        const double Radius = Constants.ProximityRadius;

        // this test is not geometrical 3-d intersection test, it takes the fact that the aircraft move
        // into account ; so it is more like a 4d test
        // (it assumes that both of the aircraft have a constant speed over the tested interval)

        // we thus have two points, each of them moving on its line segment at constant speed ; we are looking
        // for times when the distance between these two points is smaller than r

        // vec1 is vector of aircraft 1
        // vec2 is vector of aircraft 2

        // a = (V2 - V1)^T * (V2 - V1)
        double a = vec2.Minus(vec1).SquaredMagnitude();

        if (a != 0.0)
        {
            // we are first looking for instances of time when the planes are exactly r from each other
            // at least one plane is moving ; if the planes are moving in parallel, they do not have constant speed

            // if the planes are moving in parallel, then
            //   if the faster starts behind the slower, we can have 2, 1, or 0 solutions
            //   if the faster plane starts in front of the slower, we can have 0 or 1 solutions

            // if the planes are not moving in parallel, then

            // point P1 = I1 + vV1
            // point P2 = I2 + vV2
            //   - looking for v, such that dist(P1,P2) = || P1 - P2 || = r

            // it follows that || P1 - P2 || = sqrt( < P1-P2, P1-P2 > )
            //   0 = -r^2 + < P1 - P2, P1 - P2 >
            //  from properties of dot product
            //   0 = -r^2 + <I1-I2,I1-I2> + v * 2<I1-I2, V1-V2> + v^2 *<V1-V2,V1-V2>
            //   so we calculate a, b, c - and solve the quadratic equation
            //   0 = c + bv + av^2

            // b = 2 * <I1-I2, V1-V2>
            double b = 2.0 * init1.Minus(init2).Dot(vec1.Minus(vec2));

            // c = -r^2 + (I2 - I1)^T * (I2 - I1)
            double c = -Radius * Radius + init2.Minus(init1).SquaredMagnitude();

            double discr = b * b - 4.0 * a * c;
            if (discr < 0.0)
            {
                return null;
            }

            double v1 = (-b - Math.Sqrt(discr)) / (2.0 * a);
            double v2 = (-b + Math.Sqrt(discr)) / (2.0 * a);

            if (v1 <= v2 && ((v1 <= 1.0 && 1.0 <= v2) ||
                             (v1 <= 0.0 && 0.0 <= v2) ||
                             (0.0 <= v1 && v2 <= 1.0)))
            {
                // Pick a good "time" at which to report the collision.
                double v =
                    // The collision started before this frame. Report it at the start of the frame.
                    // Otherwise, the collision started during this frame. Report it at that moment.
                    v1 <= 0.0 ? 0.0 : v1;

                Vector3D result1 = init1.Plus(vec1.Times(v));
                Vector3D result2 = init2.Plus(vec2.Times(v));

                Vector3D result = result1.Plus(result2).Times(0.5);
                if (result.X >= Constants.MinX &&
                    result.X <= Constants.MaxX &&
                    result.Y >= Constants.MinY &&
                    result.Y <= Constants.MaxY &&
                    result.Z >= Constants.MinZ &&
                    result.Z <= Constants.MaxZ)
                {
                    return result;
                }
            }

            return null;
        }

        // the planes have the same speeds and are moving in parallel (or they are not moving at all)
        // they  thus have the same distance all the time ; we calculate it from the initial point

        // dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
        double dist = init2.Minus(init1).Magnitude();
        return dist <= Radius ? init1.Plus(init2).Times(0.5) : null;
    }
}

internal sealed class RedBlackTree<TK, TV> where TK : IComparable<TK>
{
    private Node? _root;

    private enum Color
    {
        Red,
        Black
    }

    private static Node TreeMinimum(Node x)
    {
        Node current = x;
        while (current.Left != null)
        {
            current = current.Left;
        }

        return current;
    }

    private sealed class Node
    {
        public readonly TK Key;
        public TV Value;
        public Node? Left;
        public Node? Right;
        public Node? Parent;
        public Color Color;

        public Node(TK key, TV value)
        {
            Key = key;
            Value = value;
            Left = null;
            Right = null;
            Parent = null;
            Color = Color.Red;
        }

        public Node? Successor()
        {
            Node x = this;
            if (x.Right != null)
            {
                return TreeMinimum(x.Right);
            }

            Node? y = x.Parent;
            while (y != null && x == y.Right)
            {
                x = y;
                y = y.Parent;
            }

            return y;
        }
    }

    public TV? Put(TK key, TV value)
    {
        InsertResult insertionResult = TreeInsert(key, value);
        if (!insertionResult.IsNewEntry)
        {
            return insertionResult.OldValue;
        }

        Node? x = insertionResult.NewNode;

        while (x != _root && x!.Parent!.Color == Color.Red)
        {
            if (x.Parent == x.Parent.Parent!.Left)
            {
                Node? y = x.Parent.Parent.Right;
                if (y?.Color == Color.Red)
                {
                    // Case 1
                    x.Parent.Color = Color.Black;
                    y.Color = Color.Black;
                    x.Parent.Parent.Color = Color.Red;
                    x = x.Parent.Parent;
                }
                else
                {
                    if (x == x.Parent.Right)
                    {
                        // Case 2
                        x = x.Parent;
                        LeftRotate(x);
                    }

                    // Case 3
                    x.Parent!.Color = Color.Black;
                    x.Parent.Parent!.Color = Color.Red;
                    RightRotate(x.Parent.Parent);
                }
            }
            else
            {
                // Same as "then" clause with "right" and "left" exchanged.
                Node? y = x.Parent.Parent.Left;
                if (y?.Color == Color.Red)
                {
                    // Case 1
                    x.Parent.Color = Color.Black;
                    y.Color = Color.Black;
                    x.Parent.Parent.Color = Color.Red;
                    x = x.Parent.Parent;
                }
                else
                {
                    if (x == x.Parent.Left)
                    {
                        // Case 2
                        x = x.Parent;
                        RightRotate(x);
                    }

                    // Case 3
                    x.Parent!.Color = Color.Black;
                    x.Parent.Parent!.Color = Color.Red;
                    LeftRotate(x.Parent.Parent);
                }
            }
        }

        _root!.Color = Color.Black;
        return default;
    }

    public TV? Remove(TK key)
    {
        Node? z = FindNode(key);
        if (z == null)
        {
            return default;
        }

        // Y is the node to be unlinked from the tree.
        Node? y;
        if (z.Left == null || z.Right == null)
        {
            y = z;
        }
        else
        {
            y = z.Successor();
        }

        // Y is guaranteed to be non-null at this point.
        Node? x = y!.Left != null ? y.Left : y.Right;

        // X is the child of y which might potentially replace y in the tree. X might be null at
        // this point.
        Node? xParent;
        if (x != null)
        {
            x.Parent = y.Parent;
            xParent = x.Parent;
        }
        else
        {
            xParent = y.Parent;
        }

        if (y.Parent == null)
        {
            _root = x;
        }
        else
        {
            if (y == y.Parent.Left)
            {
                y.Parent.Left = x;
            }
            else
            {
                y.Parent.Right = x;
            }
        }

        if (y != z)
        {
            if (y.Color == Color.Black)
            {
                RemoveFixup(x, xParent);
            }

            y.Parent = z.Parent;
            y.Color = z.Color;
            y.Left = z.Left;
            y.Right = z.Right;

            z.Left?.Parent = y;

            z.Right?.Parent = y;

            if (z.Parent != null)
            {
                if (z.Parent.Left == z)
                {
                    z.Parent.Left = y;
                }
                else
                {
                    z.Parent.Right = y;
                }
            }
            else
            {
                _root = y;
            }
        }
        else if (y.Color == Color.Black)
        {
            RemoveFixup(x, xParent);
        }

        return z.Value;
    }

    public TV? Get(TK key)
    {
        Node? node = FindNode(key);

        return node == null ? default : node.Value;
    }

    internal sealed class Entry
    {
        public readonly TK Key;
        public readonly TV Value;

        public Entry(TK key, TV value)
        {
            Key = key;
            Value = value;
        }
    }

    public void ForEach(Action<Entry> fn)
    {
        if (_root == null)
        {
            return;
        }

        Node? current = TreeMinimum(_root);
        while (current != null)
        {
            fn.Invoke(new Entry(current.Key, current.Value));
            current = current.Successor();
        }
    }

    private Node? FindNode(TK key)
    {
        Node? current = _root;
        while (current != null)
        {
            int comparisonResult = key.CompareTo(current.Key);
            if (comparisonResult == 0)
            {
                return current;
            }

            current = comparisonResult < 0 ? current.Left : current.Right;
        }

        return null;
    }

    private sealed class InsertResult
    {
        public readonly bool IsNewEntry;
        public readonly Node? NewNode;
        public readonly TV? OldValue;

        public InsertResult(bool isNewEntry, Node? newNode, TV? oldValue)
        {
            IsNewEntry = isNewEntry;
            NewNode = newNode;
            OldValue = oldValue;
        }
    }

    private InsertResult TreeInsert(TK key, TV value)
    {
        Node? y = null;
        Node? x = _root;

        while (x != null)
        {
            y = x;
            int comparisonResult = key.CompareTo(x.Key);
            if (comparisonResult < 0)
            {
                x = x.Left;
            }
            else if (comparisonResult > 0)
            {
                x = x.Right;
            }
            else
            {
                TV oldValue = x.Value;
                x.Value = value;
                return new InsertResult(false, null, oldValue);
            }
        }

        Node z = new(key, value) { Parent = y };


        if (y == null)
        {
            _root = z;
        }
        else
        {
            if (key.CompareTo(y.Key) < 0)
            {
                y.Left = z;
            }
            else
            {
                y.Right = z;
            }
        }

        return new InsertResult(true, z, default);
    }

    private Node LeftRotate(Node x)
    {
        Node? y = x.Right;

        // Turn y's left subtree into x's right subtree.
        x.Right = y!.Left;
        y.Left?.Parent = x;

        // Link x's parent to y.
        y.Parent = x.Parent;
        if (x.Parent == null)
        {
            _root = y;
        }
        else
        {
            if (x == x.Parent.Left)
            {
                x.Parent.Left = y;
            }
            else
            {
                x.Parent.Right = y;
            }
        }

        // Put x on y's left.
        y.Left = x;
        x.Parent = y;

        return y;
    }

    private Node RightRotate(Node y)
    {
        Node? x = y.Left;

        // Turn x's right subtree into y's left subtree.
        y.Left = x!.Right;
        x.Right?.Parent = y;

        // Link y's parent to x;
        x.Parent = y.Parent;
        if (y.Parent == null)
        {
            _root = x;
        }
        else
        {
            if (y == y.Parent.Left)
            {
                y.Parent.Left = x;
            }
            else
            {
                y.Parent.Right = x;
            }
        }

        x.Right = y;
        y.Parent = x;

        return x;
    }

    private void RemoveFixup(Node? x, Node? xParent)
    {
        while (x != _root && (x == null || x.Color == Color.Black))
        {
            if (x == xParent!.Left)
            {
                // Note: the text points out that w cannot be null. The reason is not obvious from
                // simply looking at the code; it comes about from the properties of the red-black
                // tree.
                Node? w = xParent.Right;
                if (w!.Color == Color.Red)
                {
                    // Case 1
                    w.Color = Color.Black;
                    xParent.Color = Color.Red;
                    LeftRotate(xParent);
                    w = xParent.Right;
                }

                if ((w!.Left == null || w.Left.Color == Color.Black)
                    && (w.Right == null || w.Right.Color == Color.Black))
                {
                    // Case 2
                    w.Color = Color.Red;
                    x = xParent;
                    xParent = x.Parent!;
                }
                else
                {
                    if (w.Right == null || w.Right.Color == Color.Black)
                    {
                        // Case 3
                        w.Left!.Color = Color.Black;
                        w.Color = Color.Red;
                        RightRotate(w);
                        w = xParent.Right;
                    }

                    // Case 4
                    w!.Color = xParent.Color;
                    xParent.Color = Color.Black;
                    w.Right?.Color = Color.Black;

                    LeftRotate(xParent);
                    x = _root;
                    xParent = x!.Parent;
                }
            }
            else
            {
                // Same as "then" clause with "right" and "left" exchanged.
                Node? w = xParent.Left;
                if (w!.Color == Color.Red)
                {
                    // Case 1
                    w.Color = Color.Black;
                    xParent.Color = Color.Red;
                    RightRotate(xParent);
                    w = xParent.Left;
                }

                if ((w!.Right == null || w.Right.Color == Color.Black)
                    && (w.Left == null || w.Left.Color == Color.Black))
                {
                    // Case 2
                    w.Color = Color.Red;
                    x = xParent;
                    xParent = x.Parent!;
                }
                else
                {
                    if (w.Left == null || w.Left.Color == Color.Black)
                    {
                        // Case 3
                        w.Right!.Color = Color.Black;
                        w.Color = Color.Red;
                        LeftRotate(w);
                        w = xParent.Left;
                    }

                    // Case 4
                    w!.Color = xParent.Color;
                    xParent.Color = Color.Black;
                    w.Left?.Color = Color.Black;

                    RightRotate(xParent);
                    x = _root;
                    xParent = x!.Parent;
                }
            }
        }

        x?.Color = Color.Black;
    }
}

internal sealed class Simulator
{
    private readonly Vector<CallSign> _aircraft;

    public Simulator(int numAircraft)
    {
        _aircraft = new Vector<CallSign>();
        for (int i = 0; i < numAircraft; i++)
        {
            _aircraft.Append(new CallSign(i));
        }
    }

    public Vector<Aircraft> Simulate(double time)
    {
        Vector<Aircraft> frame = new();
        for (int i = 0; i < _aircraft.Size(); i += 2)
        {
            frame.Append(new Aircraft(_aircraft.At(i)!, new Vector3D(time, Math.Cos(time) * 2 + i * 3, 10)));
            frame.Append(new Aircraft(_aircraft.At(i + 1)!, new Vector3D(time, Math.Sin(time) * 2 + i * 3, 10)));
        }

        return frame;
    }
}

internal sealed class Vector2D : IComparable<Vector2D>
{
    public readonly double X;
    public readonly double Y;

    public Vector2D(double x, double y)
    {
        X = x;
        Y = y;
    }

    public Vector2D Plus(Vector2D other) => new(X + other.X, Y + other.Y);

    public Vector2D Minus(Vector2D other) => new(X - other.X, Y - other.Y);

    public int CompareTo(Vector2D? other)
    {
        int result = CompareNumbers(X, other!.X);
        return result != 0 ? result : CompareNumbers(Y, other.Y);
    }

    private static int CompareNumbers(double a, double b)
    {
        // ReSharper disable once CompareOfFloatsByEqualityOperator
        if (a == b)
        {
            return 0;
        }

        if (a < b)
        {
            return -1;
        }

        if (a > b)
        {
            return 1;
        }

#pragma warning disable CS1718
        // We say that NaN is smaller than non-NaN.
        // ReSharper disable once CompareOfFloatsByEqualityOperator
        // ReSharper disable once EqualExpressionComparison
        if (a == a)
        {
            return 1;
        }
#pragma warning restore CS1718

        return -1;
    }
}

internal sealed class Vector3D
{
    public readonly double X;
    public readonly double Y;
    public readonly double Z;

    public Vector3D(double x, double y, double z)
    {
        X = x;
        Y = y;
        Z = z;
    }

    public Vector3D Plus(Vector3D other) => new(X + other.X, Y + other.Y, Z + other.Z);

    public Vector3D Minus(Vector3D other) => new(X - other.X, Y - other.Y, Z - other.Z);

    public double Dot(Vector3D other) => X * other.X + Y * other.Y + Z * other.Z;

    public double SquaredMagnitude() => Dot(this);

    public double Magnitude() => Math.Sqrt(SquaredMagnitude());

    public Vector3D Times(double amount) => new(X * amount, Y * amount, Z * amount);
}
