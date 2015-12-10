package deltablue;

import deltablue.Strength.S;

// I mark variables that should, with some level of preference, stay
// the same. I have one method with zero inputs and one output, which
// does nothing. Planners may exploit the fact that, if I am
// satisfied, my output will not change during plan execution. This is
// called "stay optimization".
//
class StayConstraint extends UnaryConstraint {

  // Install a stay constraint with the given strength on the given variable.
  public StayConstraint(final Variable v, final S strength, final Planner planner) {
   super(v, strength, planner);
  }

  @Override
  public void execute() {} // Stay constraints do nothing.
}
