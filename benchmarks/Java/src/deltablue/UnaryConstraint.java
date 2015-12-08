package som.deltablue;

import som.deltablue.Constraint.BlockFunction.Return;

// I am an abstract superclass for constraints having a single
// possible output variable.
//
abstract class UnaryConstraint extends Constraint {

  protected Variable myOutput; // possible output variable
  protected boolean  satisfied; // true if I am currently satisfied

  public void set(final Variable v, final String strength) {
    this.strength = Strength.of(strength);
    myOutput = v;
    satisfied = false;
    addConstraint();
  }

  // Answer true if this constraint is satisfied in the current solution.
  @Override
  public boolean isSatisfied() {
    return satisfied;
  }

  // Add myself to the constraint graph.
  @Override
  public void addToGraph() {
    myOutput.addConstraint(this);
    satisfied = false;
  }

  // Remove myself from the constraint graph.
  @Override
  public void removeFromGraph() {
    if (myOutput != null) {
      myOutput.removeConstraint(this);
    }
    satisfied = false;
  }

  // Decide if I can be satisfied and record that decision.
  @Override
  protected String chooseMethod(final int mark) {
    satisfied = myOutput.getMark() != mark
        && strength.stronger(myOutput.getWalkStrength());
    return null;
  }

  @Override
  public abstract void execute();

  @Override
  public void inputsDo(final BlockFunction block) throws Return {
    // I have no input variables
  }

  // Record the fact that I am unsatisfied.
  @Override
  public void markUnsatisfied() {
    satisfied = false;
  }

  // Answer my current output variable.
  @Override
  public Variable output() {
    return myOutput;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied."
  @Override
  public void recalculate() {
    myOutput.setWalkStrength(strength);
    myOutput.setStay(!isInput());
    if (myOutput.getStay()) {
      execute(); // stay optimization
    }
  }
}
