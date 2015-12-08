package som.deltablue;

// I am a unary input constraint used to mark a variable that the
// client wishes to change.
class EditConstraint extends UnaryConstraint {

  public static EditConstraint var(final Variable v, final String strength) {
    EditConstraint c = new EditConstraint();
    c.set(v, strength);
    return c;
  }

  // I indicate that a variable is to be changed by imperative code.
  @Override
  public boolean isInput() {
    return true;
  }

  @Override
  public void execute() {} // Edit constraints do nothing.
}
