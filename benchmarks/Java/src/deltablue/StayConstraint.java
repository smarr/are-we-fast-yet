package deltablue;

// I mark variables that should, with some level of preference, stay
// the same. I have one method with zero inputs and one output, which
// does nothing. Planners may exploit the fact that, if I am
// satisfied, my output will not change during plan execution. This is
// called "stay optimization".
//
class StayConstraint extends UnaryConstraint {

  // Install a stay constraint with the given strength on the given variable.
  public static StayConstraint var(final Variable v, final String strength) {
    StayConstraint c = new StayConstraint();
    c.set(v, strength);
    return c;
  }

  @Override
  public void execute() {} // Stay constraints do nothing.
}
