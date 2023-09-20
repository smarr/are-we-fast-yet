namespace Benchmarks;

public static class Constants
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

public sealed class CD : Benchmark
{
  private int _benchmark(int numAircrafts)
  {
    int numFrames = 200;

    Simulator simulator = new Simulator(numAircrafts);
    CollisionDetector detector = new CollisionDetector();

    int actualCollisions = 0;

    for (int i = 0; i < numFrames; i++)
    {
      double time = i / 10.0;
      Vector<Collision> collisions = detector.HandleNewFrame(simulator.Simulate(time));
      actualCollisions += collisions.Size();
    }

    return actualCollisions;
  }

  public override bool InnerBenchmarkLoop(int innerIterations)
  {
    return VerifyResult(_benchmark(innerIterations), innerIterations);
  }

  public bool VerifyResult(int actualCollisions, int numAircrafts)
  {
    if (numAircrafts == 1000)
    {
      return actualCollisions == 14484;
    }

    if (numAircrafts == 500)
    {
      return actualCollisions == 14484;
    }

    if (numAircrafts == 250)
    {
      return actualCollisions == 10830;
    }

    if (numAircrafts == 200)
    {
      return actualCollisions == 8655;
    }

    if (numAircrafts == 100)
    {
      return actualCollisions == 4305;
    }

    if (numAircrafts == 10)
    {
      return actualCollisions == 390;
    }

    if (numAircrafts == 2)
    {
      return actualCollisions == 42;
    }

    Console.WriteLine("No verification result for " + numAircrafts + " found");
    Console.WriteLine("Result is: " + actualCollisions);
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

  private sealed class Vector2D : ICompareTo<Vector2D>
  {
    public double X { get; }
    public double Y { get; }

    public Vector2D(double x, double y)
    {
      X = x;
      Y = y;
    }

    public Vector2D Plus(Vector2D other)
    {
      return new Vector2D(X + other.X,
        Y + other.Y);
    }

    public Vector2D Minus(Vector2D other)
    {
      return new Vector2D(X - other.X,
        Y - other.Y);
    }

    public int CompareTo(in Vector2D other)
    {
      int result = CompareNumbers(this.X, other.X);
      if (result != 0)
      {
        return result;
      }

      return CompareNumbers(this.Y, other.Y);
    }

    private static int CompareNumbers(double a, double b)
    {
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

      // We say that NaN is smaller than non-NaN.
#pragma warning disable CS1718
      if (a == a)
      {
        return 1;
      }
#pragma warning restore CS1718

      return -1;
    }
  }


  private sealed class Vector3D
  {
    public double X { get; }
    public double Y { get; }
    public double Z { get; }

    public Vector3D(double x, double y, double z)
    {
      X = x;
      Y = y;
      Z = z;
    }

    public Vector3D Plus(Vector3D other)
    {
      return new Vector3D(X + other.X,
        Y + other.Y,
        Z + other.Z);
    }

    public Vector3D Minus(Vector3D other)
    {
      return new Vector3D(X - other.X,
        Y - other.Y,
        Z - other.Z);
    }

    public double Dot(Vector3D other)
    {
      return X * other.X + Y * other.Y + Z * other.Z;
    }

    public double SquaredMagnitude()
    {
      return this.Dot(this);
    }

    public double Magnitude()
    {
      return Math.Sqrt(SquaredMagnitude());
    }

    public Vector3D Times(double amount)
    {
      return new Vector3D(X * amount,
        Y * amount,
        Z * amount);
    }
  }


  private sealed class RedBlackTree<TK, TV> where TK : ICompareTo<TK>
  {
    Node? root;

    public RedBlackTree()
    {
      root = null;
    }

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
      public TK Key { get; }
      public TV Value { get; set; }
      public Node? Left { get; set; }
      public Node? Right { get; set; }
      public Node? Parent { get; set; }
      public Color Color { get; set; }

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

      while (x != root && x!.Parent!.Color == Color.Red)
      {
        if (x.Parent == x.Parent!.Parent!.Left)
        {
          Node? y = x.Parent.Parent.Right;
          if (y != null && y.Color == Color.Red)
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
            x!.Parent!.Color = Color.Black;
            x.Parent.Parent!.Color = Color.Red;
            RightRotate(x.Parent.Parent);
          }
        }
        else
        {
          // Same as "then" clause with "right" and "left" exchanged.
          Node? y = x.Parent.Parent.Left;
          if (y != null && y.Color == Color.Red)
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
            x!.Parent!.Color = Color.Black;
            x.Parent.Parent!.Color = Color.Red;
            LeftRotate(x.Parent.Parent);
          }
        }
      }

      root!.Color = Color.Black;
      return default(TV);
    }

    public TV? Remove(TK key)
    {
      Node? z = FindNode(key);
      if (z == null)
      {
        return default(TV);
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
      Node? x;
      if (y!.Left != null)
      {
        x = y.Left;
      }
      else
      {
        x = y.Right;
      }

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
        root = x;
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

        if (z.Left != null)
        {
          z.Left.Parent = y;
        }

        if (z.Right != null)
        {
          z.Right.Parent = y;
        }

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
          root = y;
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
      if (node == null)
      {
        return default(TV);
      }

      return node.Value;
    }

    public sealed class Entry
    {
      public TK Key { get; }
      public TV Value { get; }

      public Entry(TK key, TV value)
      {
        Key = key;
        Value = value;
      }
    }

    public void ForEach(ForEach<Entry> fn)
    {
      if (root == null)
      {
        return;
      }

      Node? current = TreeMinimum(root);
      while (current != null)
      {
        fn.Invoke(new Entry(current.Key, current.Value));
        current = current.Successor();
      }
    }

    private Node? FindNode(TK key)
    {
      Node? current = root;
      while (current != null)
      {
        int comparisonResult = key.CompareTo(current.Key);
        if (comparisonResult == 0)
        {
          return current;
        }

        if (comparisonResult < 0)
        {
          current = current.Left;
        }
        else
        {
          current = current.Right;
        }
      }

      return null;
    }

    private sealed class InsertResult
    {
      public bool IsNewEntry { get; }
      public Node? NewNode { get; }
      public TV? OldValue { get; }

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
      Node? x = root;

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

      Node z = new Node(key, value);
      z.Parent = y;
      if (y == null)
      {
        root = z;
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

      return new InsertResult(true, z, default(TV));
    }

    private Node LeftRotate(Node x)
    {
      Node? y = x.Right;

      // Turn y's left subtree into x's right subtree.
      x.Right = y!.Left;
      if (y.Left != null)
      {
        y.Left.Parent = x;
      }

      // Link x's parent to y.
      y.Parent = x.Parent;
      if (x.Parent == null)
      {
        root = y;
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
      if (x.Right != null)
      {
        x.Right.Parent = y;
      }

      // Link y's parent to x;
      x.Parent = y.Parent;
      if (y.Parent == null)
      {
        root = x;
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
      while (x != root && (x == null || x.Color == Color.Black))
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
            xParent = x.Parent;
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
            if (w.Right != null)
            {
              w.Right.Color = Color.Black;
            }

            LeftRotate(xParent);
            x = root;
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
            xParent = x.Parent;
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
            if (w.Left != null)
            {
              w.Left.Color = Color.Black;
            }

            RightRotate(xParent);
            x = root;
            xParent = x!.Parent;
          }
        }
      }

      if (x != null)
      {
        x.Color = Color.Black;
      }
    }
  }

  private class Motion
  {
    public CallSign CallSign { get; }
    public Vector3D PosOne { get; }
    public Vector3D PosTwo { get; }

    public Motion(CallSign callSign, Vector3D posOne, Vector3D posTwo)
    {
      CallSign = callSign;
      PosOne = posOne;
      PosTwo = posTwo;
    }

    private Vector3D Delta()
    {
      return PosTwo.Minus(PosOne);
    }

    public Vector3D? FindIntersection(Motion other)
    {
      Vector3D init1 = PosOne;
      Vector3D init2 = other.PosOne;
      Vector3D vec1 = Delta();
      Vector3D vec2 = other.Delta();
      double radius = Constants.ProximityRadius;

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
        double c = -radius * radius + init2.Minus(init1).SquaredMagnitude();

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
          double v;
          if (v1 <= 0.0)
          {
            // The collision started before this frame. Report it at the start of the frame.
            v = 0.0;
          }
          else
          {
            // The collision started during this frame. Report it at that moment.
            v = v1;
          }

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
      if (dist <= radius)
      {
        return init1.Plus(init2).Times(0.5);
      }

      return null;
    }
  }

  private class CallSign : ICompareTo<CallSign>
  {
    private int Value { get; }

    public CallSign(int value)
    {
      Value = value;
    }

    public int CompareTo(in CallSign other)
    {
      return (Value == other.Value) ? 0 : ((Value < other.Value) ? -1 : 1);
    }
  }


  private class Collision
  {
    public CallSign AircraftA { get; }
    public CallSign AircraftB { get; }
    public Vector3D Position { get; }

    public Collision(CallSign aircraftA, CallSign aircraftB, Vector3D position)
    {
      AircraftA = aircraftA;
      AircraftB = aircraftB;
      Position = position;
    }
  }


  private class CollisionDetector
  {
    private readonly RedBlackTree<CallSign, Vector3D> state;

    public CollisionDetector()
    {
      state = new RedBlackTree<CallSign, Vector3D>();
    }

    public Vector<Collision> HandleNewFrame(Vector<Aircraft> frame)
    {
      Vector<Motion> motions = new Vector<Motion>();
      RedBlackTree<CallSign, bool> seen = new RedBlackTree<CallSign, bool>();

      frame.ForEach((aircraft) =>
      {
        Vector3D? oldPosition = state.Put(aircraft.CallSign, aircraft.Position);
        Vector3D newPosition = aircraft.Position;
        seen.Put(aircraft.CallSign, true);

        if (oldPosition == null)
        {
          // Treat newly introduced aircraft as if they were stationary.
          oldPosition = newPosition;
        }

        motions.Append(new Motion(aircraft.CallSign, oldPosition, newPosition));
      });

      // Remove aircraft that are no longer present.
      Vector<CallSign> toRemove = new Vector<CallSign>();
      state.ForEach(e =>
      {
        if (!seen.Get(e.Key))
        {
          toRemove.Append(e.Key);
        }
      });

      toRemove.ForEach(e => state.Remove(e));

      Vector<Vector<Motion>> allReduced = ReduceCollisionSet(motions);
      Vector<Collision> collisions = new Vector<Collision>();
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

      double vS = Constants.GoodVoxelSize;
      double r = Constants.ProximityRadius / 2.0;

      double vX = voxel.X;
      double x0 = init.X;
      double xv = fin.X - init.X;

      double vY = voxel.Y;
      double y0 = init.Y;
      double yv = fin.Y - init.Y;

      double lowX = (vX - r - x0) / xv;
      double highX = (vX + vS + r - x0) / xv;

      if (xv < 0.0)
      {
        double tmp = lowX;
        lowX = highX;
        highX = tmp;
      }

      double lowY = (vY - r - y0) / yv;
      double highY = (vY + vS + r - y0) / yv;

      if (yv < 0.0)
      {
        double tmp = lowY;
        lowY = highY;
        highY = tmp;
      }

      return (((xv == 0.0 && vX <= x0 + r && x0 - r <= vX + vS) /* no motion in x */ ||
               (lowX <= 1.0 && 1.0 <= highX) || (lowX <= 0.0 && 0.0 <= highX) ||
               (0.0 <= lowX && highX <= 1.0)) &&
              ((yv == 0.0 && vY <= y0 + r && y0 - r <= vY + vS) /* no motion in y */ ||
               ((lowY <= 1.0 && 1.0 <= highY) || (lowY <= 0.0 && 0.0 <= highY) ||
                (0.0 <= lowY && highY <= 1.0))) &&
              (xv == 0.0 || yv == 0.0 || /* no motion in x or y or both */
               (lowY <= highX && highX <= highY) ||
               (lowY <= lowX && lowX <= highY) ||
               (lowX <= lowY && highY <= highX)));
    }

    private static readonly Vector2D Horizontal = new(Constants.GoodVoxelSize, 0.0);
    private static readonly Vector2D Vertical = new(0.0, Constants.GoodVoxelSize);

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

      Recurse(voxelMap, seen, nextVoxel.Minus(Horizontal), motion);
      Recurse(voxelMap, seen, nextVoxel.Plus(Horizontal), motion);
      Recurse(voxelMap, seen, nextVoxel.Minus(Vertical), motion);
      Recurse(voxelMap, seen, nextVoxel.Plus(Vertical), motion);
      Recurse(voxelMap, seen, nextVoxel.Minus(Horizontal).Minus(Vertical), motion);
      Recurse(voxelMap, seen, nextVoxel.Minus(Horizontal).Plus(Vertical), motion);
      Recurse(voxelMap, seen, nextVoxel.Plus(Horizontal).Minus(Vertical), motion);
      Recurse(voxelMap, seen, nextVoxel.Plus(Horizontal).Plus(Vertical), motion);
    }

    private static Vector<Vector<Motion>> ReduceCollisionSet(Vector<Motion> motions)
    {
      RedBlackTree<Vector2D, Vector<Motion>> voxelMap = new RedBlackTree<Vector2D, Vector<Motion>>();
      motions.ForEach(motion => DrawMotionOnVoxelMap(voxelMap, motion));

      Vector<Vector<Motion>> result = new Vector<Vector<Motion>>();
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
      int xDiv = (int) (position.X / Constants.GoodVoxelSize);
      int yDiv = (int) (position.Y / Constants.GoodVoxelSize);

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

    private static void DrawMotionOnVoxelMap(
      RedBlackTree<Vector2D, Vector<Motion>> voxelMap, Motion motion)
    {
      RedBlackTree<Vector2D, bool> seen = new RedBlackTree<Vector2D, bool>();
      Recurse(voxelMap, seen, VoxelHash(motion.PosOne), motion);
    }
  }


  private class Aircraft
  {
    public CallSign CallSign { get; }
    public Vector3D Position { get; }

    public Aircraft(CallSign callSign, Vector3D position)
    {
      CallSign = callSign;
      Position = position;
    }
  }

  private sealed class Simulator
  {
    private readonly Vector<CallSign> aircraft;

    public Simulator(int numAircraft)
    {
      aircraft = new Vector<CallSign>();
      for (int i = 0; i < numAircraft; i++)
      {
        aircraft.Append(new CallSign(i));
      }
    }

    public Vector<Aircraft> Simulate(double time)
    {
      Vector<Aircraft> frame = new Vector<Aircraft>();
      for (int i = 0; i < aircraft.Size(); i += 2)
      {
        frame.Append(new Aircraft(aircraft.At(i)!,
          new Vector3D(time, Math.Cos(time) * 2 + i * 3, 10)));
        frame.Append(new Aircraft(aircraft.At(i + 1)!,
          new Vector3D(time, Math.Sin(time) * 2 + i * 3, 10)));
      }

      return frame;
    }
  }
}