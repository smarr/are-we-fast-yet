package deltablue;

import deltablue.Constraint.BlockFunction.Return;

// ------------------------ constraints ------------------------------------

// I am an abstract class representing a system-maintainable
// relationship (or "constraint") between a set of variables. I supply
// a strength instance variable; concrete subclasses provide a means
// of storing the constrained variables and other information required
// to represent a constraint.
abstract class Constraint {

  protected Strength strength; // the strength of this constraint

  public Strength getStrength() {
    return strength;
  }

  public void setStrength(final String strength) {
    this.strength = Strength.of(strength);
  }

  // Normal constraints are not input constraints. An input constraint
  // is one that depends on external state, such as the mouse, the
  // keyboard, a clock, or some arbitrary piece of imperative code.
  public boolean isInput() {
    return false;
  }

  // Answer true if this constraint is satisfied in the current solution.
  public abstract boolean isSatisfied();

  // Activate this constraint and attempt to satisfy it.
  protected void addConstraint() {
    addToGraph();
    Planner.getCurrent().incrementalAdd(this);
  }

  // Add myself to the constraint graph.
  public abstract void addToGraph();

  // Deactivate this constraint, remove it from the constraint graph,
  // possibly causing other constraints to be satisfied, and destroy
  // it.
  public void destroyConstraint() {
    if (isSatisfied()) {
      Planner.getCurrent().incrementalRemove(this);
    }
    removeFromGraph();
  }

  // Remove myself from the constraint graph.
  public abstract void removeFromGraph();

  // Decide if I can be satisfied and record that decision. The output
  // of the choosen method must not have the given mark and must have
  // a walkabout strength less than that of this constraint.
  protected abstract String chooseMethod(int mark);

  // Enforce this constraint. Assume that it is satisfied.
  public abstract void execute();

  @FunctionalInterface
  public interface BlockFunction {
    class Return extends Exception {
      private static final long serialVersionUID = 5527046579317358033L;
      private final Object value;
      public Return(final Object value) {
        this.value = value;
      }
      public Object getValue() {
        return value;
      }
    }
    void apply(final Variable var) throws BlockFunction.Return;
  }

  @FunctionalInterface
  public interface ConstraintBlockFunction {
    void apply(final Constraint c);
  }

  public abstract void inputsDo(Constraint.BlockFunction block) throws BlockFunction.Return;

  // Assume that I am satisfied. Answer true if all my current inputs
  // are known. A variable is known if either a) it is 'stay' (i.e. it
  // is a constant at plan execution time), b) it has the given mark
  // (indicating that it has been computed by a constraint appearing
  // earlier in the plan), or c) it is not determined by any
  // constraint.
  public boolean inputsKnown(final int mark) {
    try {
      inputsDo(v -> {
        if (!(v.getMark() == mark ||
            v.getStay() ||
            v.getDeterminedBy() == null)) { throw new Return(false); } });
    } catch (BlockFunction.Return r) {
      return false;
    }
    return true;
  }

  // Record the fact that I am unsatisfied.
  public abstract void markUnsatisfied();

  // Answer my current output variable. Raise an error if I am not
  // currently satisfied.
  public abstract Variable output();

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
  public Constraint satisfy(final int mark) {
    Constraint overridden;

    chooseMethod(mark);

    if (isSatisfied()) {
      // constraint can be satisfied
      // mark inputs to allow cycle detection in addPropagate
      try {
        inputsDo(in -> in.setMark(mark));
      } catch (BlockFunction.Return e) {/* Doesn't throw return... */}

      Variable out = output();
      overridden = out.getDeterminedBy();
      if (overridden != null) {
        overridden.markUnsatisfied();
      }
      out.setDeterminedBy(this);
      if (!Planner.getCurrent().addPropagate(this, mark)) {
        System.out.println("Cycle encountered");
        return null;
      }
      out.setMark(mark);
    } else {
      overridden = null;
      if (strength.sameAs(Strength.required)) {
        throw new RuntimeException("Could not satisfy a required constraint");
      }
    }
    return overridden;
  }
}
