package som.deltablue;

// I constrain two variables to have the same value: "v1 = v2".
class EqualityConstraint extends BinaryConstraint {

  // Install a constraint with the given strength equating the given
  // variables.
  public static EqualityConstraint var(final Variable var1, final Variable var2,
      final String strength) {
    EqualityConstraint c = new EqualityConstraint();
    c.set(var1, var2, strength);
    return c;
  }

  // Enforce this constraint. Assume that it is satisfied.
  @Override
  public void execute() {
    if (direction == "forward") {
      v2.setValue(v1.getValue());
    } else {
      v1.setValue(v2.getValue());
    }
  }
}
