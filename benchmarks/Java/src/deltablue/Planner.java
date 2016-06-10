/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue;

import java.util.Arrays;

import som.ForEachInterface;
import som.Vector;

public final class Planner {
  private int currentMark;

  public Planner() {
    currentMark = 1;
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
  public void incrementalAdd(final AbstractConstraint c) {
    int mark = newMark();
    AbstractConstraint overridden = c.satisfy(mark, this);

    while (overridden != null) {
      overridden = overridden.satisfy(mark, this);
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
  public void incrementalRemove(final AbstractConstraint c) {
    Variable out = c.getOutput();
    c.markUnsatisfied();
    c.removeFromGraph();

    Vector<AbstractConstraint> unsatisfied = removePropagateFrom(out);
    unsatisfied.forEach(u -> incrementalAdd(u));
  }

  // Extract a plan for resatisfaction starting from the outputs of
  // the given constraints, usually a set of input constraints.
  //
  protected Plan extractPlanFromConstraints(final Vector<AbstractConstraint> constraints) {
    Vector<AbstractConstraint> sources = new Vector<>();

    constraints.forEach(c -> {
      if (c.isInput() && c.isSatisfied()) {
        sources.append(c);
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
  protected Plan makePlan(final Vector<AbstractConstraint> sources) {
    int mark = newMark();
    Plan plan = new Plan();
    Vector<AbstractConstraint> todo = sources;

    while (!todo.isEmpty()) {
      AbstractConstraint c = todo.removeFirst();

      if (c.getOutput().getMark() != mark && c.inputsKnown(mark)) {
        // not in plan already and eligible for inclusion
        plan.append(c);
        c.getOutput().setMark(mark);
        addConstraintsConsumingTo(c.getOutput(), todo);
      }
    }
    return plan;
  }

  // The given variable has changed. Propagate new values downstream.
  public void propagateFrom(final Variable v) {
    Vector<AbstractConstraint> todo = new Vector<>();
    addConstraintsConsumingTo(v, todo);

    while (!todo.isEmpty()) {
      AbstractConstraint c = todo.removeFirst();
      c.execute();
      addConstraintsConsumingTo(c.getOutput(), todo);
    }
  }

  protected void addConstraintsConsumingTo(final Variable v, final Vector<AbstractConstraint> coll) {
    AbstractConstraint determiningC = v.getDeterminedBy();

    v.getConstraints().forEach(c -> {
      if (c != determiningC && c.isSatisfied()) {
        coll.append(c);
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
  public boolean addPropagate(final AbstractConstraint c, final int mark) {
    Vector<AbstractConstraint> todo = Vector.with(c);

    while (!todo.isEmpty()) {
      AbstractConstraint d = todo.removeFirst();

      if (d.getOutput().getMark() == mark) {
        incrementalRemove(c);
        return false;
      }
      d.recalculate();
      addConstraintsConsumingTo(d.getOutput(), todo);
    }
    return true;
  }

  private void change(final Variable var, final int newValue) {
    EditConstraint editC = new EditConstraint(var, Strength.PREFERRED, this);

    Vector<AbstractConstraint> editV = Vector.with(editC);
    Plan plan = extractPlanFromConstraints(editV);
    for (int i = 0; i < 10; i++) {
      var.setValue(newValue);
      plan.execute();
    }
    editC.destroyConstraint(this);
  }

  private void constraintsConsuming(final Variable v,
      final ForEachInterface<AbstractConstraint> fn) {
    AbstractConstraint determiningC = v.getDeterminedBy();
    v.getConstraints().forEach(c -> {
      if (c != determiningC && c.isSatisfied()) {
        fn.apply(c);
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
  protected Vector<AbstractConstraint> removePropagateFrom(final Variable out) {
    Vector<AbstractConstraint> unsatisfied = new Vector<>();

    out.setDeterminedBy(null);
    out.setWalkStrength(Strength.absoluteWeakest());
    out.setStay(true);

    Vector<Variable> todo = Vector.with(out);

    while (!todo.isEmpty()) {
      Variable v = todo.removeFirst();

      v.getConstraints().forEach(c -> {
        if (!c.isSatisfied()) { unsatisfied.append(c); }});

      constraintsConsuming(v, c -> {
        c.recalculate();
        todo.append(c.getOutput());
      });
    }

    unsatisfied.sort((c1, c2) ->
      c1.getStrength().stronger(c2.getStrength()) ? -1 : 1);
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
  public static void chainTest(final int n) {
    Planner planner = new Planner();
    Variable[] vars = new Variable[n + 1];
    Arrays.setAll(vars, i -> new Variable());

    // Build chain of n equality constraints
    for (int i = 0; i < n; i++) {
      Variable v1 = vars[i];
      Variable v2 = vars[i + 1];
      new EqualityConstraint(v1, v2, Strength.REQUIRED, planner);
    }

    new StayConstraint(vars[n], Strength.STRONG_DEFAULT, planner);
    AbstractConstraint editC = new EditConstraint(vars[0], Strength.PREFERRED, planner);

    Vector<AbstractConstraint> editV = Vector.with(editC);
    Plan plan = planner.extractPlanFromConstraints(editV);
    for (int i = 0; i < 100; i++) {
      vars[0].setValue(i);
      plan.execute();
      if (vars[n].getValue() != i) {
        throw new RuntimeException("Chain test failed!");
      }
    }
    editC.destroyConstraint(planner);
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

    Variable src = null;
    Variable dst = null;
    for (int i = 1; i <= n; i++) {
      src = Variable.value(i);
      dst = Variable.value(i);
      dests.append(dst);
      new StayConstraint(src, Strength.DEFAULT, planner);
      new ScaleConstraint(src, scale, offset, dst, Strength.REQUIRED, planner);
    }

    planner.change(src, 17);
    if (dst.getValue() != 1170) {
      throw new RuntimeException("Projection test 1 failed!");
    }

    planner.change(dst, 1050);
    if (src.getValue() != 5) {
      throw new RuntimeException("Projection test 2 failed!");
    }

    planner.change(scale, 5);
    for (int i = 0; i < n - 1; ++i) {
      if (dests.at(i).getValue() != (i + 1) * 5 + 1000) {
        throw new RuntimeException("Projection test 3 failed!");
      }
    }

    planner.change(offset, 2000);
    for (int i = 0; i < n - 1; ++i) {
      if (dests.at(i).getValue() != (i + 1) * 5 + 2000) {
        throw new RuntimeException("Projection test 4 failed!");
      }
    }
  }
}
