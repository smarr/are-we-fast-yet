package deltablue;

import deltablue.Strength.S;

// I constrain two variables to have the same value: "v1 = v2".
class EqualityConstraint extends BinaryConstraint {

  // Install a constraint with the given strength equating the given
  // variables.
  public EqualityConstraint(final Variable var1, final Variable var2,
      final S strength, final Planner planner) {
    super(var1, var2, strength, planner);
    addConstraint(planner);
  }

  // Enforce this constraint. Assume that it is satisfied.
  @Override
  public void execute() {
    if (direction == Direction.FORWARD) {
      v2.setValue(v1.getValue());
    } else {
      v1.setValue(v2.getValue());
    }
  }
}
