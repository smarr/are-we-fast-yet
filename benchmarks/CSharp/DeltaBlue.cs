namespace Benchmarks;

public class DeltaBlue : Benchmark
{
  public override bool InnerBenchmarkLoop(int innerIterations)
  {
    Planner.ChainTest(innerIterations);
    Planner.ProjectionTest(innerIterations);
    return true;
  }

  public override object Execute()
  {
    throw new NotImplementedException();
  }

  public override bool VerifyResult(object result)
  {
    throw new NotImplementedException();
  }
}

// A Plan is an ordered list of constraints to be executed in sequence
// to resatisfy all currently satisfiable constraints in the face of
// one or more changing inputs.
sealed class Plan : Vector<AbstractConstraint>
{
  public Plan() : base(15)
  {
  }

  public void Execute()
  {
    ForEach(c => c.Execute());
  }
}

sealed class Planner
{
  private int currentMark;

  public Planner()
  {
    currentMark = 1;
  }

  /**
   * Attempt to satisfy the given constraint and, if successful,
   * incrementally update the dataflow graph. Details: If satifying
   * the constraint is successful, it may override a weaker constraint
   * on its output. The algorithm attempts to resatisfy that
   * constraint using some other method. This process is repeated
   * until either a) it reaches a variable that was not previously
   * determined by any constraint or b) it reaches a constraint that
   * is too weak to be satisfied using any of its methods. The
   * variables of constraints that have been processed are marked with
   * a unique mark value so that we know where we've been. This allows
   * the algorithm to avoid getting into an infinite loop even if the
   * constraint graph has an inadvertent cycle.
   */
  public void IncrementalAdd(AbstractConstraint c)
  {
    int mark = NewMark();
    AbstractConstraint? overridden = c.Satisfy(mark, this);

    while (overridden != null)
    {
      overridden = overridden.Satisfy(mark, this);
    }
  }

  /**
  * Entry point for retracting a constraint. Remove the given
  * constraint and incrementally update the dataflow graph.
  * Details: Retracting the given constraint may allow some currently
  * unsatisfiable downstream constraint to be satisfied. We therefore collect
  * a list of unsatisfied downstream constraints and attempt to
  * satisfy each one in turn. This list is traversed by constraint
  * strength, strongest first, as a heuristic for avoiding
  * unnecessarily adding and then overriding weak constraints.
  * Assume: c is satisfied.
  */
  public void IncrementalRemove(AbstractConstraint c)
  {
    Variable output = c.GetOutput();
    c.MarkUnsatisfied();
    c.RemoveFromGraph();

    Vector<AbstractConstraint> unsatisfied = RemovePropagateFrom(output);
    unsatisfied.ForEach(IncrementalAdd);
  }

  /**
  * Extract a plan for resatisfaction starting from the outputs of
  * the given constraints, usually a set of input constraints.
  */
  private Plan ExtractPlanFromConstraints(Vector<AbstractConstraint> constraints)
  {
    Vector<AbstractConstraint> sources = new Vector<AbstractConstraint>();

    constraints.ForEach(c =>
    {
      if (c.IsInput() && c.IsSatisfied())
      {
        sources.Append(c);
      }
    });

    return MakePlan(sources);
  }

  // Extract a plan for resatisfaction starting from the given source
  // constraints, usually a set of input constraints. This method
  // assumes that stay optimization is desired; the plan will contain
  // only constraints whose output variables are not stay. Constraints
  // that do no computation, such as stay and edit constraints, are
  // not included in the plan.
  // Details: The outputs of a constraint are marked when it is added
  // to the plan under construction. A constraint may be appended to
  // the plan when all its input variables are known. A variable is
  // known if either a) the variable is marked (indicating that has
  // been computed by a constraint appearing earlier in the plan), b)
  // the variable is 'stay' (i.e. it is a constant at plan execution
  // time), or c) the variable is not determined by any
  // constraint. The last provision is for past states of history
  // variables, which are not stay but which are also not computed by
  // any constraint.
  // Assume: sources are all satisfied.
  private Plan MakePlan(Vector<AbstractConstraint> sources)
  {
    int mark = NewMark();
    Plan plan = new Plan();
    Vector<AbstractConstraint> todo = sources;

    while (!todo.IsEmpty())
    {
      AbstractConstraint? c = todo.RemoveFirst();

      if (c != null && c.GetOutput().Mark != mark && c.InputsKnown(mark))
      {
        // not in plan already and eligible for inclusion
        plan.Append(c);
        c.GetOutput().Mark = mark;
        AddConstraintsConsumingTo(c.GetOutput(), todo);
      }
    }

    return plan;
  }

  private void AddConstraintsConsumingTo(Variable v, Vector<AbstractConstraint> coll)
  {
    AbstractConstraint? determiningC = v.DeterminedBy;

    v.Constraints.ForEach(c =>
    {
      if (c != determiningC && c.IsSatisfied())
      {
        coll.Append(c);
      }
    });
  }

  // Recompute the walkabout strengths and stay flags of all variables
  // downstream of the given constraint and recompute the actual
  // values of all variables whose stay flag is true. If a cycle is
  // detected, remove the given constraint and answer
  // false. Otherwise, answer true.
  // Details: Cycles are detected when a marked variable is
  // encountered downstream of the given constraint. The sender is
  // assumed to have marked the inputs of the given constraint with
  // the given mark. Thus, encountering a marked node downstream of
  // the output constraint means that there is a path from the
  // constraint's output to one of its inputs.
  //
  public bool AddPropagate(AbstractConstraint c, int mark)
  {
    Vector<AbstractConstraint> todo = Vector<AbstractConstraint>.With(c);

    while (!todo.IsEmpty())
    {
      AbstractConstraint? d = todo.RemoveFirst();

      if (d != null)
      {
        if (d.GetOutput().Mark == mark)
        {
          IncrementalRemove(c);
          return false;
        }

        d.Recalculate();
        AddConstraintsConsumingTo(d.GetOutput(), todo);
      }
    }

    return true;
  }

  private void Change(Variable var, int newValue)
  {
    EditConstraint editC = new EditConstraint(var, Strength.PreferredSym, this);

    Vector<AbstractConstraint> editV = Vector<AbstractConstraint>.With(editC);
    Plan plan = ExtractPlanFromConstraints(editV);
    for (int i = 0; i < 10; i++)
    {
      var.Value = newValue;
      plan.Execute();
    }

    editC.DestroyConstraint(this);
  }

  private void ConstraintsConsuming(Variable? v, ForEach<AbstractConstraint> fn)
  {
    AbstractConstraint? determiningC = v?.DeterminedBy;
    v?.Constraints.ForEach(c =>
    {
      if (c != determiningC && c.IsSatisfied())
      {
        fn.Invoke(c);
      }
    });
  }

  // Select a previously unused mark value.
  private int NewMark()
  {
    currentMark++;
    return currentMark;
  }

  // Update the walkabout strengths and stay flags of all variables
  // downstream of the given constraint. Answer a collection of
  // unsatisfied constraints sorted in order of decreasing strength.
  private Vector<AbstractConstraint> RemovePropagateFrom(Variable output)
  {
    Vector<AbstractConstraint> unsatisfied = new Vector<AbstractConstraint>();

    output.DeterminedBy = null;
    output.WalkStrength = Strength.AbsoluteWeakest();
    output.Stay = true;

    Vector<Variable> todo = Vector<Variable>.With(output);

    while (!todo.IsEmpty())
    {
      Variable? v = todo.RemoveFirst();

      v?.Constraints.ForEach(c =>
      {
        if (!c.IsSatisfied())
        {
          unsatisfied.Append(c);
        }
      });

      ConstraintsConsuming(v, c =>
      {
        c.Recalculate();
        todo.Append(c.GetOutput());
      });
    }

    unsatisfied.Sort((c1, c2) =>
      c1.Strength.Stronger(c2.Strength) ? -1 : 1);
    return unsatisfied;
  }

  // This is the standard DeltaBlue benchmark. A long chain of
  // equality constraints is constructed with a stay constraint on
  // one end. An edit constraint is then added to the opposite end
  // and the time is measured for adding and removing this
  // constraint, and extracting and executing a constraint
  // satisfaction plan. There are two cases. In case 1, the added
  // constraint is stronger than the stay constraint and values must
  // propagate down the entire length of the chain. In case 2, the
  // added constraint is weaker than the stay constraint so it cannot
  // be accomodated. The cost in this case is, of course, very
  // low. Typical situations lie somewhere between these two
  // extremes.
  //
  public static void ChainTest(int n)
  {
    Planner planner = new Planner();
    Variable[] vars = new Variable[n + 1];

    for (int i = 0; i < n + 1; i++)
    {
      vars[i] = new Variable();
    }

    // Build chain of n equality constraints
    for (int i = 0; i < n; i++)
    {
      Variable v1 = vars[i];
      Variable v2 = vars[i + 1];
      new EqualityConstraint(v1, v2, Strength.RequiredSym, planner);
    }

    new StayConstraint(vars[n], Strength.StrongDefaultSym, planner);
    AbstractConstraint editC = new EditConstraint(vars[0], Strength.PreferredSym, planner);

    Vector<AbstractConstraint> editV = Vector<AbstractConstraint>.With(editC);
    Plan plan = planner.ExtractPlanFromConstraints(editV);
    for (int i = 0; i < 100; i++)
    {
      vars[0].Value = i;
      plan.Execute();
      if (vars[n].Value != i)
      {
        throw new Exception("Chain test failed!");
      }
    }

    editC.DestroyConstraint(planner);
  }

  // This test constructs a two sets of variables related to each
  // other by a simple linear transformation (scale and offset). The
  // time is measured to change a variable on either side of the
  // mapping and to change the scale and offset factors.
  //
  public static void ProjectionTest(int n)
  {
    Planner planner = new Planner();

    Vector<Variable> dests = new Vector<Variable>();

    Variable scale = Variable.WithValue(10);
    Variable offset = Variable.WithValue(1000);

    Variable? src = null;
    Variable? dst = null;
    for (int i = 1; i <= n; i++)
    {
      src = Variable.WithValue(i);
      dst = Variable.WithValue(i);
      dests.Append(dst);
      new StayConstraint(src, Strength.DefaultSym, planner);
      new ScaleConstraint(src, scale, offset, dst, Strength.RequiredSym, planner);
    }

    if (src == null || dst == null)
    {
      return;
    }

    planner.Change(src, 17);
    if (dst.Value != 1170)
    {
      throw new Exception("Projection test 1 failed!");
    }

    planner.Change(dst, 1050);
    if (src.Value != 5)
    {
      throw new Exception("Projection test 2 failed!");
    }

    planner.Change(scale, 5);
    for (int i = 0; i < n - 1; ++i)
    {
      if (dests.At(i)?.Value != (i + 1) * 5 + 1000)
      {
        throw new Exception("Projection test 3 failed!");
      }
    }

    planner.Change(offset, 2000);
    for (int i = 0; i < n - 1; ++i)
    {
      if (dests.At(i)?.Value != (i + 1) * 5 + 2000)
      {
        throw new Exception("Projection test 4 failed!");
      }
    }
  }
}

sealed class Sym : ICustomHash
{
  private readonly int hash;

  public Sym(int hash)
  {
    this.hash = hash;
  }

  public int CustomHash()
  {
    return hash;
  }
}

/*
 * Strengths are used to measure the relative importance of constraints. New
 * strengths may be inserted in the strength hierarchy without disrupting
 * current constraints. Strengths cannot be created outside this class, so
 * pointer comparison can be used for value comparison.
 */
sealed class Strength
{
  public static readonly Sym AbsoluteStrongestSym = new(0);
  public static readonly Sym RequiredSym = new(1);
  public static readonly Sym StrongPreferredSym = new(2);
  public static readonly Sym PreferredSym = new(3);
  public static readonly Sym StrongDefaultSym = new(4);
  public static readonly Sym DefaultSym = new(5);
  public static readonly Sym WeakDefaultSym = new(6);
  public static readonly Sym AbsoluteWeakestSym = new(7);

  public int ArithmeticValue { get; }

  private readonly Sym symbolicValue;

  private Strength(Sym symbolicValue)
  {
    this.symbolicValue = symbolicValue;
    ArithmeticValue = (int) (strengthTable.At(symbolicValue) ?? 0);
  }

  private Strength(bool check)
  {
    this.symbolicValue = new Sym(-1);
    ArithmeticValue = 0;
  }

  public bool SameAs(Strength s)
  {
    return ArithmeticValue == s.ArithmeticValue;
  }

  public bool Stronger(Strength s)
  {
    return ArithmeticValue < s.ArithmeticValue;
  }

  public bool Weaker(Strength s)
  {
    return ArithmeticValue > s.ArithmeticValue;
  }

  public Strength Strongest(Strength s)
  {
    return s.Stronger(this) ? s : this;
  }

  public Strength Weakest(Strength s)
  {
    return s.Weaker(this) ? s : this;
  }

  private static Strength _undefined = new Strength(false);

  public static Strength Of(Sym strength)
  {
    return strengthConstant.At(strength) ?? _undefined;
  }

  private static IdentityDictionary<Sym, object> CreateStrengthTable()
  {
    IdentityDictionary<Sym, object> strengthTable = new IdentityDictionary<Sym, object>();
    strengthTable.AtPut(AbsoluteStrongestSym, -10000);
    strengthTable.AtPut(RequiredSym, -800);
    strengthTable.AtPut(StrongPreferredSym, -600);
    strengthTable.AtPut(PreferredSym, -400);
    strengthTable.AtPut(StrongDefaultSym, -200);
    strengthTable.AtPut(DefaultSym, 0);
    strengthTable.AtPut(WeakDefaultSym, 500);
    strengthTable.AtPut(AbsoluteWeakestSym, 10000);
    return strengthTable;
  }

  private static IdentityDictionary<Sym, Strength> CreateStrengthConstants()
  {
    IdentityDictionary<Sym, Strength> strengthConstant = new IdentityDictionary<Sym, Strength>();
    strengthTable.GetKeys().ForEach(key =>
      strengthConstant.AtPut(key, new Strength(key))
    );
    return strengthConstant;
  }

  public static Strength AbsoluteWeakest()
  {
    return _absoluteWeakest;
  }

  public static Strength Required()
  {
    return _required;
  }

  private static readonly IdentityDictionary<Sym, object> strengthTable = CreateStrengthTable();
  private static readonly IdentityDictionary<Sym, Strength> strengthConstant = CreateStrengthConstants();

  private static readonly Strength _absoluteWeakest = Of(AbsoluteWeakestSym);
  private static readonly Strength _required = Of(RequiredSym);
}

enum Direction
{
  Forward,
  Backward
}

// ------------------------ constraints ------------------------------------

// I am an abstract class representing a system-maintainable
// relationship (or "constraint") between a set of variables. I supply
// a strength instance variable; concrete subclasses provide a means
// of storing the constrained variables and other information required
// to represent a constraint.
abstract class AbstractConstraint
{
  public Strength Strength { get; } // the strength of this constraint

  public AbstractConstraint(Sym strength)
  {
    Strength = Strength.Of(strength);
  }

  // Normal constraints are not input constraints. An input constraint
  // is one that depends on external state, such as the mouse, the
  // keyboard, a clock, or some arbitrary piece of imperative code.
  public virtual bool IsInput()
  {
    return false;
  }

  // Answer true if this constraint is satisfied in the current solution.
  public abstract bool IsSatisfied();

  // Activate this constraint and attempt to satisfy it.
  protected void AddConstraint(Planner planner)
  {
    AddToGraph();
    planner.IncrementalAdd(this);
  }

  // Add myself to the constraint graph.
  public abstract void AddToGraph();

  // Deactivate this constraint, remove it from the constraint graph,
  // possibly causing other constraints to be satisfied, and destroy
  // it.
  public void DestroyConstraint(Planner planner)
  {
    if (IsSatisfied())
    {
      planner.IncrementalRemove(this);
    }

    RemoveFromGraph();
  }

  // Remove myself from the constraint graph.
  public abstract void RemoveFromGraph();

  // Decide if I can be satisfied and record that decision. The output
  // of the chosen method must not have the given mark and must have
  // a walkabout strength less than that of this constraint.
  protected abstract Direction? ChooseMethod(int mark);

  // Enforce this constraint. Assume that it is satisfied.
  public abstract void Execute();

  public abstract void InputsDo(ForEach<Variable> fn);
  public abstract bool InputsHasOne(Test<Variable> fn);

  // Assume that I am satisfied. Answer true if all my current inputs
  // are known. A variable is known if either a) it is 'stay' (i.e. it
  // is a constant at plan execution time), b) it has the given mark
  // (indicating that it has been computed by a constraint appearing
  // earlier in the plan), or c) it is not determined by any
  // constraint.
  public bool InputsKnown(int mark)
  {
    return !InputsHasOne(v => !(v.Mark == mark || v.Stay || v.DeterminedBy == null));
  }

  // Record the fact that I am unsatisfied.
  public abstract void MarkUnsatisfied();

  // Answer my current output variable. Raise an error if I am not
  // currently satisfied.
  public abstract Variable GetOutput();

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  public abstract void Recalculate();

  // Attempt to find a way to enforce this constraint. If successful,
  // record the solution, perhaps modifying the current dataflow
  // graph. Answer the constraint that this constraint overrides, if
  // there is one, or nil, if there isn't.
  // Assume: I am not already satisfied.
  //
  public AbstractConstraint? Satisfy(int mark, Planner planner)
  {
    AbstractConstraint? overridden;

    ChooseMethod(mark);

    if (IsSatisfied())
    {
      // constraint can be satisfied
      // mark inputs to allow cycle detection in addPropagate
      InputsDo(input => input.Mark = mark);

      Variable output = GetOutput();
      overridden = output.DeterminedBy;
      if (overridden != null)
      {
        overridden.MarkUnsatisfied();
      }

      output.DeterminedBy = this;
      if (!planner.AddPropagate(this, mark))
      {
        throw new Exception("Cycle encountered");
      }

      output.Mark = mark;
    }
    else
    {
      overridden = null;
      if (Strength.SameAs(Strength.Required()))
      {
        throw new Exception("Could not satisfy a required constraint");
      }
    }

    return overridden;
  }
}

// I am an abstract superclass for constraints having two possible
// output variables.
abstract class BinaryConstraint : AbstractConstraint
{
  protected Variable V1;
  protected Variable V2; // possible output variables
  protected Direction? Direction; // one of the following...

  public BinaryConstraint(Variable var1, Variable var2,
    Sym strength, Planner planner) : base(strength)
  {
    V1 = var1;
    V2 = var2;
    Direction = null;
  }

  // Answer true if this constraint is satisfied in the current solution.
  public override bool IsSatisfied()
  {
    return Direction != null;
  }

  // Add myself to the constraint graph.
  public override void AddToGraph()
  {
    V1.AddConstraint(this);
    V2.AddConstraint(this);
    Direction = null;
  }

  // Remove myself from the constraint graph.
  public override void RemoveFromGraph()
  {
    if (V1 != null)
    {
      V1.RemoveConstraint(this);
    }

    if (V2 != null)
    {
      V2.RemoveConstraint(this);
    }

    Direction = null;
  }

  // Decide if I can be satisfied and which way I should flow based on
  // the relative strength of the variables I relate, and record that
  // decision.
  //
  protected override Direction? ChooseMethod(int mark)
  {
    if (V1.Mark == mark)
    {
      if (V2.Mark != mark && Strength.Stronger(V2.WalkStrength))
      {
        Direction = Benchmarks.Direction.Forward;
        return Direction;
      }
      else
      {
        Direction = null;
        return Direction;
      }
    }

    if (V2.Mark == mark)
    {
      if (V1.Mark != mark && Strength.Stronger(V1.WalkStrength))
      {
        Direction = Benchmarks.Direction.Backward;
        return Direction;
      }
      else
      {
        Direction = null;
        return Direction;
      }
    }

    // If we get here, neither variable is marked, so we have a choice.
    if (V1.WalkStrength.Weaker(V2.WalkStrength))
    {
      if (Strength.Stronger(V1.WalkStrength))
      {
        Direction = Benchmarks.Direction.Backward;
        return Direction;
      }
      else
      {
        Direction = null;
        return Direction;
      }
    }
    else
    {
      if (Strength.Stronger(V2.WalkStrength))
      {
        Direction = Benchmarks.Direction.Forward;
        return Direction;
      }
      else
      {
        Direction = null;
        return Direction;
      }
    }
  }

  public override void InputsDo(ForEach<Variable> fn)
  {
    if (Direction == Benchmarks.Direction.Forward)
    {
      fn.Invoke(V1);
    }
    else
    {
      fn.Invoke(V2);
    }
  }

  public override bool InputsHasOne(Test<Variable> fn)
  {
    if (Direction == Benchmarks.Direction.Forward)
    {
      return fn.Invoke(V1);
    }
    else
    {
      return fn.Invoke(V2);
    }
  }

  // Record the fact that I am unsatisfied.
  public override void MarkUnsatisfied()
  {
    Direction = null;
  }


  // Answer my current output variable.
  public override Variable GetOutput()
  {
    return Direction == Benchmarks.Direction.Forward ? V2 : V1;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  //
  public override void Recalculate()
  {
    Variable input;
    Variable output;

    if (Direction == Benchmarks.Direction.Forward)
    {
      input = V1;
      output = V2;
    }
    else
    {
      input = V2;
      output = V1;
    }

    output.WalkStrength = Strength.Weakest(input.WalkStrength);
    output.Stay = input.Stay;
    if (output.Stay)
    {
      Execute();
    }
  }
}

// I am an abstract superclass for constraints having a single
// possible output variable.
abstract class UnaryConstraint : AbstractConstraint
{
  protected readonly Variable Output; // possible output variable
  protected bool Satisfied; // true if I am currently satisfied

  public UnaryConstraint(Variable v, Sym strength, Planner planner) : base(strength)
  {
    this.Output = v;
    AddConstraint(planner);
  }

  // Answer true if this constraint is satisfied in the current solution.
  public override bool IsSatisfied()
  {
    return Satisfied;
  }

  // Add myself to the constraint graph.
  public override void AddToGraph()
  {
    Output.AddConstraint(this);
    Satisfied = false;
  }

  // Remove myself from the constraint graph.
  public override void RemoveFromGraph()
  {
    if (Output != null)
    {
      Output.RemoveConstraint(this);
    }

    Satisfied = false;
  }

  // Decide if I can be satisfied and record that decision.
  protected override Direction? ChooseMethod(int mark)
  {
    Satisfied = Output.Mark != mark
                && Strength.Stronger(Output.WalkStrength);
    return null;
  }

  public override abstract void Execute();

  public override void InputsDo(ForEach<Variable> fn)
  {
    // I have no input variables
  }

  public override bool InputsHasOne(Test<Variable> fn)
  {
    return false;
  }

  // Record the fact that I am unsatisfied.
  public override void MarkUnsatisfied()
  {
    Satisfied = false;
  }

  // Answer my current output variable.
  public override Variable GetOutput()
  {
    return Output;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied."
  public override void Recalculate()
  {
    Output.WalkStrength = Strength;
    Output.Stay = !IsInput();
    if (Output.Stay)
    {
      Execute(); // stay optimization
    }
  }
}

// I am a unary input constraint used to mark a variable that the
// client wishes to change.
sealed class EditConstraint : UnaryConstraint
{
  public EditConstraint(Variable v, Sym strength, Planner planner) : base(v, strength, planner)
  {
  }

  // I indicate that a variable is to be changed by imperative code.
  public override bool IsInput()
  {
    return true;
  }

  public override void Execute()
  {
  } // Edit constraints do nothing.
}

// I constrain two variables to have the same value: "v1 = v2".
sealed class EqualityConstraint : BinaryConstraint
{
  // Install a constraint with the given strength equating the given
  // variables.
  public EqualityConstraint(Variable var1, Variable var2,
    Sym strength, Planner planner) : base(var1, var2, strength, planner)
  {
    AddConstraint(planner);
  }

  // Enforce this constraint. Assume that it is satisfied.
  public override void Execute()
  {
    if (Direction == Benchmarks.Direction.Forward)
    {
      V2.Value = V1.Value;
    }
    else
    {
      V1.Value = V2.Value;
    }
  }
}

// I relate two variables by the linear scaling relationship: "v2 =
// (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
// this relationship but the scale factor and offset are considered
// read-only.
sealed class ScaleConstraint : BinaryConstraint
{
  private readonly Variable scale; // scale factor input variable
  private readonly Variable offset; // offset input variable

  public ScaleConstraint(Variable src, Variable scale,
    Variable offset, Variable dest, Sym strength,
    Planner planner) : base(src, dest, strength, planner)
  {
    this.scale = scale;
    this.offset = offset;
    AddConstraint(planner);
  }

  // Add myself to the constraint graph.
  public override void AddToGraph()
  {
    V1.AddConstraint(this);
    V2.AddConstraint(this);
    scale.AddConstraint(this);
    offset.AddConstraint(this);
    Direction = null;
  }

  // Remove myself from the constraint graph.
  public override void RemoveFromGraph()
  {
    if (V1 != null)
    {
      V1.RemoveConstraint(this);
    }

    if (V2 != null)
    {
      V2.RemoveConstraint(this);
    }

    if (scale != null)
    {
      scale.RemoveConstraint(this);
    }

    if (offset != null)
    {
      offset.RemoveConstraint(this);
    }

    Direction = null;
  }

  // Enforce this constraint. Assume that it is satisfied.
  public override void Execute()
  {
    if (Direction == Benchmarks.Direction.Forward)
    {
      V2.Value = V1.Value * scale.Value + offset.Value;
    }
    else
    {
      V1.Value = (V2.Value - offset.Value) / scale.Value;
    }
  }

  public override void InputsDo(ForEach<Variable> fn)
  {
    if (Direction == Benchmarks.Direction.Forward)
    {
      fn.Invoke(V1);
      fn.Invoke(scale);
      fn.Invoke(offset);
    }
    else
    {
      fn.Invoke(V2);
      fn.Invoke(scale);
      fn.Invoke(offset);
    }
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  public override void Recalculate()
  {
    Variable input;
    Variable output;

    if (Direction == Benchmarks.Direction.Forward)
    {
      input = V1;
      output = V2;
    }
    else
    {
      output = V1;
      input = V2;
    }

    output.WalkStrength = Strength.Weakest(input.WalkStrength);
    output.Stay = input.Stay && scale.Stay && offset.Stay;
    if (output.Stay)
    {
      Execute(); // stay optimization
    }
  }
}

// I mark variables that should, with some level of preference, stay
// the same. I have one method with zero inputs and one output, which
// does nothing. Planners may exploit the fact that, if I am
// satisfied, my output will not change during plan execution. This is
// called "stay optimization".
//
sealed class StayConstraint : UnaryConstraint
{
  // Install a stay constraint with the given strength on the given variable.
  public StayConstraint(Variable v, Sym strength, Planner planner) : base(v, strength, planner)
  {
  }

  public override void Execute()
  {
  } // Stay constraints do nothing.
}

// ------------------------------ variables ------------------------------

// I represent a constrained variable. In addition to my value, I
// maintain the structure of the constraint graph, the current
// dataflow graph, and various parameters of interest to the DeltaBlue
// incremental constraint solver.
sealed class Variable
{
  public int Value { get; set; } // my value; changed by constraints
  public Vector<AbstractConstraint> Constraints { get; } // normal constraints that reference me

  public AbstractConstraint? DeterminedBy { get; set; } // the constraint that currently determines

  // my value (or null if there isn't one)
  public int Mark { get; set; } // used by the planner to mark constraints
  public Strength WalkStrength { get; set; } // my walkabout strength
  public bool Stay { get; set; } // true if I am a planning-time constant

  public static Variable WithValue(int aValue)
  {
    Variable v = new Variable();
    v.Value = aValue;
    return v;
  }

  public Variable()
  {
    Value = 0;
    Constraints = new Vector<AbstractConstraint>(2);
    DeterminedBy = null;
    WalkStrength = Strength.AbsoluteWeakest();
    Stay = true;
    Mark = 0;
  }

  // Add the given constraint to the set of all constraints that refer to me.
  public void AddConstraint(AbstractConstraint c)
  {
    Constraints.Append(c);
  }

  // Remove all traces of c from this variable.
  public void RemoveConstraint(AbstractConstraint c)
  {
    Constraints.Remove(c);
    if (DeterminedBy == c)
    {
      DeterminedBy = null;
    }
  }
}