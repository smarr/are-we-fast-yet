package deltablue;

import java.util.Arrays;
import java.util.Vector;

import deltablue.Constraint.ConstraintBlockFunction;

public class Planner {
  private int currentMark;

  public void initialize() {
    currentMark = 0;
  }

  // Attempt to satisfy the given constraint and, if successful,
  // incrementally update the dataflow graph. Details: If satifying
  // the constraint is successful, it may override a weaker constraint
  // on its output. The algorithm attempts to resatisfy that
  // constraint using some other method. This process is repeated
  // until either a) it reaches a variable that was not previously
  // determined by any constraint or b) it reaches a constraint that
  // is too weak to be satisfied using any of its methods. The
  // variables of constraints that have been processed are marked with
  // a unique mark value so that we know where we've been. This allows
  // the algorithm to avoid getting into an infinite loop even if the
  // constraint graph has an inadvertent cycle.
  //
  public void incrementalAdd(final Constraint c) {
    int mark = newMark();
    Constraint overridden = c.satisfy(mark);
    while (overridden != null) {
      overridden = overridden.satisfy(mark);
    }
  }

  // Entry point for retracting a constraint. Remove the given
  // constraint and incrementally update the dataflow graph.
  // Details: Retracting the given constraint may allow some currently
  // unsatisfiable downstream constraint to be satisfied. We therefore collect
  // a list of unsatisfied downstream constraints and attempt to
  // satisfy each one in turn. This list is traversed by constraint
  // strength, strongest first, as a heuristic for avoiding
  // unnecessarily adding and then overriding weak constraints.
  // Assume: c is satisfied.
  //
  public void incrementalRemove(final Constraint c) {
    Variable out = c.output();
    c.markUnsatisfied();
    c.removeFromGraph();

    Vector<Constraint> unsatisfied = removePropagateFrom(out);
    unsatisfied.forEach(u -> incrementalAdd(u));
  }

  // Extract a plan for resatisfaction starting from the outputs of
  // the given constraints, usually a set of input constraints.
  //
  protected Plan extractPlanFromConstraints(final Vector<Constraint> constraints) {
    Vector<Constraint> sources = new Vector<>();

    constraints.forEach(c -> {
      if (c.isInput() && c.isSatisfied()) {
        sources.add(c);
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
  //
  protected Plan makePlan(final Vector<Constraint> sources) {
    int mark = newMark();
    Plan plan = new Plan();
    Vector<Constraint> todo = sources;

    while (!todo.isEmpty()) {
      Constraint c = todo.remove(0);

      if (c.output().getMark() != mark && c.inputsKnown(mark)) {
        // not in plan already and eligible for inclusion
        plan.add(c);
        c.output().setMark(mark);
        addConstraintsConsumingTo(c.output(), todo);
      }
    }
    return plan;
  }

  // The given variable has changed. Propagate new values downstream.
  public void propagateFrom(final Variable v) {
    Vector<Constraint> todo = new Vector<>();
    addConstraintsConsumingTo(v, todo);
    while (!todo.isEmpty()) {
      Constraint c = todo.remove(0);
      c.execute();
      addConstraintsConsumingTo(c.output(), todo);
    }
  }

  protected void addConstraintsConsumingTo(final Variable v, final Vector<Constraint> coll) {
    Constraint determiningC = v.getDeterminedBy();
    v.getConstraints().forEach(c -> {
      if (c != determiningC && c.isSatisfied()) {
        coll.add(c);
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
  public boolean addPropagate(final Constraint c, final int mark) {
    Vector<Constraint> todo = new Vector<>();
    todo.add(c);
    while (!todo.isEmpty()) {
      Constraint d = todo.remove(0);

      if (d.output().getMark() == mark) {
        incrementalRemove(c);
        return false;
      }
      d.recalculate();
      addConstraintsConsumingTo(d.output(), todo);
    }
    return true;
  }

  private void change(final Variable var, final int newValue) {
    EditConstraint editC = EditConstraint.var(var, "preferred");

    Vector<Constraint> editV = new Vector<>();
    editV.add(editC);
    Plan plan = extractPlanFromConstraints(editV);
    for (int i = 0; i < 10; i++) {
      var.setValue(newValue);
      plan.execute();
    }
    editC.destroyConstraint();
  }

  private void constraintsConsuming(final Variable v,
      final ConstraintBlockFunction block) {
    Constraint determiningC = v.getDeterminedBy();
    v.getConstraints().forEach(c -> {
      if (c != determiningC && c.isSatisfied()) {
        block.apply(c);
      }
    });
  }

  // Select a previously unused mark value.
  private int newMark() {
    return ++currentMark;
  }

  // Update the walkabout strengths and stay flags of all variables
  // downstream of the given constraint. Answer a collection of
  // unsatisfied constraints sorted in order of decreasing strength.
  protected Vector<Constraint> removePropagateFrom(final Variable out) {
    Vector<Constraint> unsatisfied = new Vector<>();

    out.setDeterminedBy(null);
    out.setWalkStrength(Strength.absoluteWeakest);
    out.setStay(true);

    Vector<Variable> todo = new Vector<>();
    todo.add(out);

    while (!todo.isEmpty()) {
      Variable v = todo.remove(0);

      v.getConstraints().forEach(c -> {
        if (!c.isSatisfied()) { unsatisfied.add(c); }});

      constraintsConsuming(v, c -> {
        c.recalculate();
        todo.add(c.output());
      });
    }

    unsatisfied.sort((c1, c2) ->
      c1.getStrength().stronger(c2.getStrength()) ?
          -1 : c1.getStrength().sameAs(c2.getStrength()) ? 0 : 1);
    return unsatisfied;
  }


  public static void error(final String s) {
    System.err.println(s);
    System.exit(1);
  }

  private static Planner currentPlanner;

  public Planner() {
    initialize();
    currentPlanner = this;
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
  public static void chainTest(final int n) {
    Planner planner = new Planner();
    Variable[] vars = new Variable[n+1];
    Arrays.setAll(vars, i -> new Variable());

    // Build chain of n equality constraints
    for (int i = 0; i < n; i++) {
      Variable v1 = vars[i];
      Variable v2 = vars[i + 1];
      EqualityConstraint.var(v1, v2, "required");
    }

    StayConstraint.var(vars[n], "strongDefault");
    Constraint editC = EditConstraint.var(vars[0], "preferred");

    Vector<Constraint> editV = new Vector<>();
    editV.addElement(editC);
    Plan plan = planner.extractPlanFromConstraints(editV);
    for (int i = 0; i < 100; i++) {
      vars[0].setValue(i);
      plan.execute();
      if (vars[n].getValue() != i) {
        error("Chain test failed!");
      }
    }
    editC.destroyConstraint();
  }

  // This test constructs a two sets of variables related to each
  // other by a simple linear transformation (scale and offset). The
  // time is measured to change a variable on either side of the
  // mapping and to change the scale and offset factors.
  //
  public static void projectionTest(final int n) {
    Planner planner = new Planner();

    Vector<Variable> dests = new Vector<>();

    Variable scale  = Variable.value(10);
    Variable offset = Variable.value(1000);

    Variable src = null, dst = null;
    for (int i = 0; i < n; ++i) {
      src = Variable.value(i);
      dst = Variable.value(i);
      dests.add(dst);
      StayConstraint.var(src, "default");
      ScaleConstraint.var(src, scale, offset, dst, "required");
    }

    planner.change(src, 17);
    if (dst.getValue() != 1170) {
      error("Projection test 1 failed!");
    }

    planner.change(dst, 1050);
    if (src.getValue() != 5) {
      error("Projection test 2 failed!");
    }

    planner.change(scale, 5);
    for (int i = 0; i < n - 1; ++i) {
      if (dests.elementAt(i).getValue() != i * 5 + 1000) {
        error("Projection test 3 failed!");
      }
    }

    planner.change(offset, 2000);
    for (int i = 0; i < n - 1; ++i) {
      if (dests.elementAt(i).getValue() != i * 5 + 2000) {
        error("Projection test 4 failed!");
      }
    }
  }

  public static Planner getCurrent() {
    return currentPlanner;
  }
}
