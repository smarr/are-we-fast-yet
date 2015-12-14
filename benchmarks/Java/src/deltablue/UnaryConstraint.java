package deltablue;

import deltablue.Strength.S;
import som.ForEachInterface;


// I am an abstract superclass for constraints having a single
// possible output variable.
abstract class UnaryConstraint extends AbstractConstraint {

  protected final Variable output; // possible output variable
  protected boolean  satisfied; // true if I am currently satisfied

  public UnaryConstraint(final Variable v, final S strength, final Planner planner) {
    super(strength);
    this.output = v;
    addConstraint(planner);
  }

  // Answer true if this constraint is satisfied in the current solution.
  @Override
  public boolean isSatisfied() {
    return satisfied;
  }

  // Add myself to the constraint graph.
  @Override
  public void addToGraph() {
    output.addConstraint(this);
    satisfied = false;
  }

  // Remove myself from the constraint graph.
  @Override
  public void removeFromGraph() {
    if (output != null) {
      output.removeConstraint(this);
    }
    satisfied = false;
  }

  // Decide if I can be satisfied and record that decision.
  @Override
  protected Direction chooseMethod(final int mark) {
    satisfied = output.getMark() != mark
        && strength.stronger(output.getWalkStrength());
    return null;
  }

  @Override
  public abstract void execute();

  @Override
  public void inputsDo(final ForEachInterface<Variable> fn) {
    // I have no input variables
  }

  @Override
  public boolean inputsHasOne(final som.TestInterface<Variable> fn) {
    return false;
  };

  // Record the fact that I am unsatisfied.
  @Override
  public void markUnsatisfied() {
    satisfied = false;
  }

  // Answer my current output variable.
  @Override
  public Variable getOutput() {
    return output;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied."
  @Override
  public void recalculate() {
    output.setWalkStrength(strength);
    output.setStay(!isInput());
    if (output.getStay()) {
      execute(); // stay optimization
    }
  }
}
