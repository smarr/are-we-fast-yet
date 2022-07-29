namespace Benchmarks;

public class DeltaBlue : Benchmark {

  public override bool InnerBenchmarkLoop(int innerIterations)
  {
    Planner.chainTest(innerIterations);
    Planner.projectionTest(innerIterations);
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
sealed class Plan : Vector<AbstractConstraint> {
  public Plan() : base(15) {
  }

  public void execute() {
    ForEach(c => c.execute());
  }
}

sealed class Planner {
  private int currentMark;

  public Planner() {
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
    public void incrementalAdd(AbstractConstraint c) {
    int mark = newMark();
    AbstractConstraint overridden = c.satisfy(mark, this);

    while (overridden != null) {
      overridden = overridden.satisfy(mark, this);
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
  public void incrementalRemove(AbstractConstraint c) {
    Variable output = c.getOutput();
    c.markUnsatisfied();
    c.removeFromGraph();

    Vector<AbstractConstraint> unsatisfied = removePropagateFrom(output);
    unsatisfied.ForEach(u => incrementalAdd(u));
  }

  /**
  * Extract a plan for resatisfaction starting from the outputs of
  * the given constraints, usually a set of input constraints.
  */
  protected Plan extractPlanFromConstraints(Vector<AbstractConstraint> constraints) {
    Vector<AbstractConstraint> sources = new Vector<AbstractConstraint>();

    constraints.ForEach(c => {
      if (c.isInput() && c.isSatisfied()) {
        sources.Append(c);
      }});

    return makePlan(sources);
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
  protected Plan makePlan(Vector<AbstractConstraint> sources) {
    int mark = newMark();
    Plan plan = new Plan();
    Vector<AbstractConstraint> todo = sources;

    while (!todo.IsEmpty()) {
      AbstractConstraint c = todo.RemoveFirst();

      if (c.getOutput().getMark() != mark && c.inputsKnown(mark)) {
        // not in plan already and eligible for inclusion
        plan.Append(c);
        c.getOutput().setMark(mark);
        addConstraintsConsumingTo(c.getOutput(), todo);
      }
    }
    return plan;
  }

  // The given variable has changed. Propagate new values downstream.
  public void propagateFrom(Variable v) {
    Vector<AbstractConstraint> todo = new Vector<AbstractConstraint>();
    addConstraintsConsumingTo(v, todo);

    while (!todo.IsEmpty()) {
      AbstractConstraint c = todo.RemoveFirst();
      c.execute();
      addConstraintsConsumingTo(c.getOutput(), todo);
    }
  }

  protected void addConstraintsConsumingTo(Variable v, Vector<AbstractConstraint> coll) {
    AbstractConstraint determiningC = v.getDeterminedBy();

    v.getConstraints().ForEach(c => {
      if (c != determiningC && c.isSatisfied()) {
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
  public bool addPropagate(AbstractConstraint c, int mark) {
    Vector<AbstractConstraint> todo = Vector<AbstractConstraint>.With(c);

    while (!todo.IsEmpty()) {
      AbstractConstraint d = todo.RemoveFirst();

      if (d.getOutput().getMark() == mark) {
        incrementalRemove(c);
        return false;
      }
      d.recalculate();
      addConstraintsConsumingTo(d.getOutput(), todo);
    }
    return true;
  }

  private void change(Variable var, int newValue) {
    EditConstraint editC = new EditConstraint(var, Strength.PREFERRED, this);

    Vector<AbstractConstraint> editV = Vector<AbstractConstraint>.With(editC);
    Plan plan = extractPlanFromConstraints(editV);
    for (int i = 0; i < 10; i++) {
      var.setValue(newValue);
      plan.execute();
    }
    editC.destroyConstraint(this);
  }

  private void constraintsConsuming(Variable v, ForEach<AbstractConstraint> fn) {
    AbstractConstraint determiningC = v.getDeterminedBy();
    v.getConstraints().ForEach(c => {
      if (c != determiningC && c.isSatisfied()) {
        fn.Invoke(c);
      }
    });
  }

  // Select a previously unused mark value.
  private int newMark() {
    currentMark++;
    return currentMark;
  }

  // Update the walkabout strengths and stay flags of all variables
  // downstream of the given constraint. Answer a collection of
  // unsatisfied constraints sorted in order of decreasing strength.
  protected Vector<AbstractConstraint> removePropagateFrom(Variable output) {
    Vector<AbstractConstraint> unsatisfied = new Vector<AbstractConstraint>();

    output.setDeterminedBy(null);
    output.setWalkStrength(Strength.absoluteWeakest());
    output.setStay(true);

    Vector<Variable> todo = Vector<Variable>.With(output);

    while (!todo.IsEmpty()) {
      Variable v = todo.RemoveFirst();

      v.getConstraints().ForEach(c => {
        if (!c.isSatisfied()) { unsatisfied.Append(c); }});

      constraintsConsuming(v, c => {
        c.recalculate();
        todo.Append(c.getOutput());
      });
    }

    unsatisfied.Sort((c1, c2) =>
      c1.Strength.stronger(c2.Strength) ? -1 : 1);
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
  public static void chainTest(int n) {
    Planner planner = new Planner();
    Variable[] vars = new Variable[n + 1];

for (int i = 0; i < n + 1; i++) {
  vars[i] = new Variable();
}

    // Build chain of n equality constraints
    for (int i = 0; i < n; i++) {
      Variable v1 = vars[i];
      Variable v2 = vars[i + 1];
      new EqualityConstraint(v1, v2, Strength.REQUIRED, planner);
    }

    new StayConstraint(vars[n], Strength.STRONG_DEFAULT, planner);
    AbstractConstraint editC = new EditConstraint(vars[0], Strength.PREFERRED, planner);

    Vector<AbstractConstraint> editV = Vector<AbstractConstraint>.With(editC);
    Plan plan = planner.extractPlanFromConstraints(editV);
    for (int i = 0; i < 100; i++) {
      vars[0].setValue(i);
      plan.execute();
      if (vars[n].getValue() != i) {
        throw new Exception("Chain test failed!");
      }
    }
    editC.destroyConstraint(planner);
  }

  // This test constructs a two sets of variables related to each
  // other by a simple linear transformation (scale and offset). The
  // time is measured to change a variable on either side of the
  // mapping and to change the scale and offset factors.
  //
  public static void projectionTest(int n) {
    Planner planner = new Planner();

    Vector<Variable> dests = new Vector<Variable>();

    Variable scale  = Variable.withValue(10);
    Variable offset = Variable.withValue(1000);

    Variable src = null;
    Variable dst = null;
    for (int i = 1; i <= n; i++) {
      src = Variable.withValue(i);
      dst = Variable.withValue(i);
      dests.Append(dst);
      new StayConstraint(src, Strength.DEFAULT, planner);
      new ScaleConstraint(src, scale, offset, dst, Strength.REQUIRED, planner);
    }

    planner.change(src, 17);
    if (dst.getValue() != 1170) {
      throw new Exception("Projection test 1 failed!");
    }

    planner.change(dst, 1050);
    if (src.getValue() != 5) {
      throw new Exception("Projection test 2 failed!");
    }

    planner.change(scale, 5);
    for (int i = 0; i < n - 1; ++i) {
      if (dests.At(i).getValue() != (i + 1) * 5 + 1000) {
        throw new Exception("Projection test 3 failed!");
      }
    }

    planner.change(offset, 2000);
    for (int i = 0; i < n - 1; ++i) {
      if (dests.At(i).getValue() != (i + 1) * 5 + 2000) {
        throw new Exception("Projection test 4 failed!");
      }
    }
  }
}

sealed class Sym : ICustomHash {

  private readonly int hash;

  public Sym(int hash) {
    this.hash = hash;
  }

  public int CustomHash() {
    return hash;
  }
}

/*
 * Strengths are used to measure the relative importance of constraints. New
 * strengths may be inserted in the strength hierarchy without disrupting
 * current constraints. Strengths cannot be created outside this class, so
 * pointer comparison can be used for value comparison.
 */
sealed class Strength {

  public static readonly Sym ABSOLUTE_STRONGEST = new Sym(0);
  public static readonly Sym REQUIRED           = new Sym(1);
  public static readonly Sym STRONG_PREFERRED   = new Sym(2);
  public static readonly Sym PREFERRED          = new Sym(3);
  public static readonly Sym STRONG_DEFAULT     = new Sym(4);
  public static readonly Sym DEFAULT            = new Sym(5);
  public static readonly Sym WEAK_DEFAULT       = new Sym(6);
  public static readonly Sym ABSOLUTE_WEAKEST   = new Sym(7);

  public int ArithmeticValue {get;}

  private readonly Sym   symbolicValue;

  private Strength(Sym symbolicValue) {
    this.symbolicValue = symbolicValue;
    ArithmeticValue = (int) strengthTable.At(symbolicValue);
  }

  public bool sameAs(Strength s) {
    return ArithmeticValue == s.ArithmeticValue;
  }

  public bool stronger(Strength s) {
    return ArithmeticValue < s.ArithmeticValue;
  }

  public bool weaker(Strength s) {
    return ArithmeticValue > s.ArithmeticValue;
  }

  public Strength strongest(Strength s) {
    return s.stronger(this) ? s : this;
  }

  public Strength weakest(Strength s) {
    return s.weaker(this) ? s : this;
  }


  public static Strength of(Sym strength) {
    return strengthConstant.At(strength);
  }

  private static IdentityDictionary<Sym, object> createStrengthTable() {
    IdentityDictionary<Sym, object> strengthTable = new IdentityDictionary<Sym, object>();
    strengthTable.AtPut(ABSOLUTE_STRONGEST, -10000);
    strengthTable.AtPut(REQUIRED,           -800);
    strengthTable.AtPut(STRONG_PREFERRED,   -600);
    strengthTable.AtPut(PREFERRED,          -400);
    strengthTable.AtPut(STRONG_DEFAULT,     -200);
    strengthTable.AtPut(DEFAULT,             0);
    strengthTable.AtPut(WEAK_DEFAULT,        500);
    strengthTable.AtPut(ABSOLUTE_WEAKEST,    10000);
    return strengthTable;
  }

  private static IdentityDictionary<Sym, Strength> createStrengthConstants() {
    IdentityDictionary<Sym, Strength> strengthConstant = new IdentityDictionary<Sym, Strength>();
    strengthTable.GetKeys().ForEach(key =>
      strengthConstant.AtPut(key, new Strength(key))
    );
    return strengthConstant;
  }

  public static Strength absoluteWeakest() {
    return _absoluteWeakest;
  }

  public static Strength required() {
    return _required;
  }

  private static readonly Strength _absoluteWeakest = of(ABSOLUTE_WEAKEST);
  private static readonly Strength _required = of(REQUIRED);

  private static readonly IdentityDictionary<Sym, object>  strengthTable = createStrengthTable();
  private static readonly IdentityDictionary<Sym, Strength> strengthConstant = createStrengthConstants();
}

enum Direction {
  FORWARD, BACKWARD
}

// ------------------------ constraints ------------------------------------

// I am an abstract class representing a system-maintainable
// relationship (or "constraint") between a set of variables. I supply
// a strength instance variable; concrete subclasses provide a means
// of storing the constrained variables and other information required
// to represent a constraint.
abstract class AbstractConstraint {

  public Strength Strength {get;} // the strength of this constraint

  public AbstractConstraint(Sym strength) {
    Strength = Strength.of(strength);
  }

  // Normal constraints are not input constraints. An input constraint
  // is one that depends on external state, such as the mouse, the
  // keyboard, a clock, or some arbitrary piece of imperative code.
  public virtual bool isInput() {
    return false;
  }

  // Answer true if this constraint is satisfied in the current solution.
  public abstract bool isSatisfied();

  // Activate this constraint and attempt to satisfy it.
  protected void addConstraint(Planner planner) {
    addToGraph();
    planner.incrementalAdd(this);
  }

  // Add myself to the constraint graph.
  public abstract void addToGraph();

  // Deactivate this constraint, remove it from the constraint graph,
  // possibly causing other constraints to be satisfied, and destroy
  // it.
  public void destroyConstraint(Planner planner) {
    if (isSatisfied()) {
      planner.incrementalRemove(this);
    }
    removeFromGraph();
  }

  // Remove myself from the constraint graph.
  public abstract void removeFromGraph();

  // Decide if I can be satisfied and record that decision. The output
  // of the chosen method must not have the given mark and must have
  // a walkabout strength less than that of this constraint.
  protected abstract Direction? chooseMethod(int mark);

  // Enforce this constraint. Assume that it is satisfied.
  public abstract void execute();

  public abstract void inputsDo(ForEach<Variable> fn);
  public abstract bool inputsHasOne(Test<Variable> fn);

  // Assume that I am satisfied. Answer true if all my current inputs
  // are known. A variable is known if either a) it is 'stay' (i.e. it
  // is a constant at plan execution time), b) it has the given mark
  // (indicating that it has been computed by a constraint appearing
  // earlier in the plan), or c) it is not determined by any
  // constraint.
  public bool inputsKnown(int mark) {
    return !inputsHasOne(v => {
        return !(v.getMark() == mark || v.getStay() || v.getDeterminedBy() == null);
    });
  }

  // Record the fact that I am unsatisfied.
  public abstract void markUnsatisfied();

  // Answer my current output variable. Raise an error if I am not
  // currently satisfied.
  public abstract Variable getOutput();

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  public abstract void recalculate();

  // Attempt to find a way to enforce this constraint. If successful,
  // record the solution, perhaps modifying the current dataflow
  // graph. Answer the constraint that this constraint overrides, if
  // there is one, or nil, if there isn't.
  // Assume: I am not already satisfied.
  //
  public AbstractConstraint satisfy(int mark, Planner planner) {
    AbstractConstraint overridden;

    chooseMethod(mark);

    if (isSatisfied()) {
      // constraint can be satisfied
      // mark inputs to allow cycle detection in addPropagate
      inputsDo(input => input.setMark(mark));

      Variable output = getOutput();
      overridden = output.getDeterminedBy();
      if (overridden != null) {
        overridden.markUnsatisfied();
      }
      output.setDeterminedBy(this);
      if (!planner.addPropagate(this, mark)) {
        throw new Exception("Cycle encountered");
      }
      output.setMark(mark);
    } else {
      overridden = null;
      if (Strength.sameAs(Strength.required())) {
        throw new Exception("Could not satisfy a required constraint");
      }
    }
    return overridden;
  }
}


// I am an abstract superclass for constraints having two possible
// output variables.
abstract class BinaryConstraint : AbstractConstraint {

  protected Variable v1;
  protected Variable v2;          // possible output variables
  protected Direction? direction;  // one of the following...

  public BinaryConstraint(Variable var1, Variable var2,
      Sym strength, Planner planner) : base(strength){
    v1 = var1;
    v2 = var2;
    direction = null;
  }

  // Answer true if this constraint is satisfied in the current solution.
  public override bool isSatisfied() {
    return direction != null;
  }

  // Add myself to the constraint graph.
  public override void addToGraph() {
    v1.addConstraint(this);
    v2.addConstraint(this);
    direction = null;
  }

  // Remove myself from the constraint graph.
  public override void removeFromGraph() {
    if (v1 != null) {
      v1.removeConstraint(this);
    }
    if (v2 != null) {
      v2.removeConstraint(this);
    }
    direction = null;
  }

  // Decide if I can be satisfied and which way I should flow based on
  // the relative strength of the variables I relate, and record that
  // decision.
  //
  protected override Direction? chooseMethod(int mark) {
    if (v1.getMark() == mark) {
      if (v2.getMark() != mark && Strength.stronger(v2.getWalkStrength())) {
        direction = Direction.FORWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }

    if (v2.getMark() == mark) {
      if (v1.getMark() != mark && Strength.stronger(v1.getWalkStrength())) {
        direction = Direction.BACKWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }

    // If we get here, neither variable is marked, so we have a choice.
    if (v1.getWalkStrength().weaker(v2.getWalkStrength())) {
      if (Strength.stronger(v1.getWalkStrength())) {
        direction = Direction.BACKWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    } else {
      if (Strength.stronger(v2.getWalkStrength())) {
        direction = Direction.FORWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }
  }

  public override void inputsDo(ForEach<Variable> fn) {
    if (direction == Direction.FORWARD) {
      fn.Invoke(v1);
    } else {
      fn.Invoke(v2);
    }
  }

  public override bool inputsHasOne(Test<Variable> fn) {
    if (direction == Direction.FORWARD) {
      return fn.Invoke(v1);
    } else {
      return fn.Invoke(v2);
    }
  }

  // Record the fact that I am unsatisfied.
  public override void markUnsatisfied() {
    direction = null;
  }


  // Answer my current output variable.
  public override Variable getOutput() {
    return direction == Direction.FORWARD ? v2 : v1;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  //
  public override void recalculate() {
    Variable input;
    Variable output;

    if (direction == Direction.FORWARD) {
      input = v1; output = v2;
    } else {
      input = v2; output = v1;
    }

    output.setWalkStrength(Strength.weakest(input.getWalkStrength()));
    output.setStay(input.getStay());
    if (output.getStay()) {
      execute();
    }
  }
}


// I am an abstract superclass for constraints having a single
// possible output variable.
abstract class UnaryConstraint : AbstractConstraint {

  protected readonly Variable output; // possible output variable
  protected bool  satisfied; // true if I am currently satisfied

  public UnaryConstraint(Variable v, Sym strength, Planner planner) : base(strength) {
    this.output = v;
    addConstraint(planner);
  }

  // Answer true if this constraint is satisfied in the current solution.
  public override bool isSatisfied() {
    return satisfied;
  }

  // Add myself to the constraint graph.
  public override void addToGraph() {
    output.addConstraint(this);
    satisfied = false;
  }

  // Remove myself from the constraint graph.
  public override void removeFromGraph() {
    if (output != null) {
      output.removeConstraint(this);
    }
    satisfied = false;
  }

  // Decide if I can be satisfied and record that decision.
  protected override Direction? chooseMethod(int mark) {
    satisfied = output.getMark() != mark
        && Strength.stronger(output.getWalkStrength());
    return null;
  }

  public override abstract void execute();

  public override void inputsDo(ForEach<Variable> fn) {
    // I have no input variables
  }

  public override bool inputsHasOne(Test<Variable> fn) {
    return false;
  }

  // Record the fact that I am unsatisfied.
  public override void markUnsatisfied() {
    satisfied = false;
  }

  // Answer my current output variable.
  public override Variable getOutput() {
    return output;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied."
  public override void recalculate() {
    output.setWalkStrength(Strength);
    output.setStay(!isInput());
    if (output.getStay()) {
      execute(); // stay optimization
    }
  }
}


// I am a unary input constraint used to mark a variable that the
// client wishes to change.
sealed class EditConstraint : UnaryConstraint {

  public EditConstraint(Variable v, Sym strength, Planner planner) : base(v, strength, planner) {
  }

  // I indicate that a variable is to be changed by imperative code.
  public override bool isInput() {
    return true;
  }

  public override void execute() { } // Edit constraints do nothing.
}


// I constrain two variables to have the same value: "v1 = v2".
sealed class EqualityConstraint : BinaryConstraint {

  // Install a constraint with the given strength equating the given
  // variables.
  public EqualityConstraint(Variable var1, Variable var2,
      Sym strength, Planner planner) :base(var1, var2, strength, planner) {
    addConstraint(planner);
  }

  // Enforce this constraint. Assume that it is satisfied.
  public override void execute() {
    if (direction == Direction.FORWARD) {
      v2.setValue(v1.getValue());
    } else {
      v1.setValue(v2.getValue());
    }
  }
}

// I relate two variables by the linear scaling relationship: "v2 =
// (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
// this relationship but the scale factor and offset are considered
// read-only.
sealed class ScaleConstraint : BinaryConstraint {

  protected readonly Variable scale;  // scale factor input variable
  protected readonly Variable offset; // offset input variable

  public ScaleConstraint(Variable src, Variable scale,
      Variable offset, Variable dest, Sym strength,
      Planner planner):base(src, dest, strength, planner) {
    this.scale = scale;
    this.offset = offset;
    addConstraint(planner);
  }

  // Add myself to the constraint graph.
  public override void addToGraph() {
    v1.addConstraint(this);
    v2.addConstraint(this);
    scale.addConstraint(this);
    offset.addConstraint(this);
    direction = null;
  }

  // Remove myself from the constraint graph.
  public override void removeFromGraph() {
    if (v1 != null) { v1.removeConstraint(this); }
    if (v2 != null) { v2.removeConstraint(this); }
    if (scale  != null) { scale.removeConstraint(this); }
    if (offset != null) { offset.removeConstraint(this); }
    direction = null;
  }

  // Enforce this constraint. Assume that it is satisfied.
  public override void execute() {
    if (direction == Direction.FORWARD) {
      v2.setValue(v1.getValue() * scale.getValue() + offset.getValue());
    } else {
      v1.setValue((v2.getValue() - offset.getValue()) / scale.getValue());
    }
  }

  public override void inputsDo(ForEach<Variable> fn) {
    if (direction == Direction.FORWARD) {
      fn.Invoke(v1);
      fn.Invoke(scale);
      fn.Invoke(offset);
    } else {
      fn.Invoke(v2);
      fn.Invoke(scale);
      fn.Invoke(offset);
    }
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  public override void recalculate() {
    Variable input;
    Variable output;

    if (direction == Direction.FORWARD) {
      input  = v1; output = v2;
    } else {
      output = v1; input  = v2;
    }

    output.setWalkStrength(Strength.weakest(input.getWalkStrength()));
    output.setStay(input.getStay() && scale.getStay() && offset.getStay());
    if (output.getStay()) {
      execute(); // stay optimization
    }
  }
}


// I mark variables that should, with some level of preference, stay
// the same. I have one method with zero inputs and one output, which
// does nothing. Planners may exploit the fact that, if I am
// satisfied, my output will not change during plan execution. This is
// called "stay optimization".
//
sealed class StayConstraint : UnaryConstraint {

  // Install a stay constraint with the given strength on the given variable.
  public StayConstraint(Variable v, Sym strength, Planner planner) : base(v, strength, planner) {
  }

  public override void execute() { } // Stay constraints do nothing.
}


// ------------------------------ variables ------------------------------

// I represent a constrained variable. In addition to my value, I
// maintain the structure of the constraint graph, the current
// dataflow graph, and various parameters of interest to the DeltaBlue
// incremental constraint solver.
sealed class Variable {

  private int value;       // my value; changed by constraints
  private readonly Vector<AbstractConstraint> constraints; // normal constraints that reference me
  private AbstractConstraint determinedBy; // the constraint that currently determines
  // my value (or null if there isn't one)
  private int mark;        // used by the planner to mark constraints
  private Strength walkStrength; // my walkabout strength
  private bool  stay;        // true if I am a planning-time constant

  public static Variable withValue(int aValue) {
    Variable v = new Variable();
    v.setValue(aValue);
    return v;
  }

  public Variable() {
    value = 0;
    constraints = new Vector<AbstractConstraint>(2);
    determinedBy = null;
    walkStrength = Strength.absoluteWeakest();
    stay = true;
    mark = 0;
  }

  // Add the given constraint to the set of all constraints that refer to me.
  public void addConstraint(AbstractConstraint c) {
    constraints.Append(c);
  }

  public Vector<AbstractConstraint> getConstraints() {
    return constraints;
  }

  public AbstractConstraint getDeterminedBy() {
    return determinedBy;
  }

  public void setDeterminedBy(AbstractConstraint c) {
    determinedBy = c;
  }

  public int getMark() {
    return mark;
  }

  public void setMark(int markValue) {
    mark = markValue;
  }

  // Remove all traces of c from this variable.
  public void removeConstraint(AbstractConstraint c) {
    constraints.Remove(c);
    if (determinedBy == c) {
      determinedBy = null;
    }
  }

  public bool getStay() {
    return stay;
  }

  public void setStay(bool v) {
    stay = v;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public Strength getWalkStrength() {
    return walkStrength;
  }

  public void setWalkStrength(Strength strength) {
    walkStrength = strength;
  }
}
