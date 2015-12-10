package deltablue;

import som.Vector;

// A Plan is an ordered list of constraints to be executed in sequence
// to resatisfy all currently satisfiable constraints in the face of
// one or more changing inputs.
class Plan extends Vector<AbstractConstraint> {
  public Plan() {
    super(15);
  }

  public void execute() {
    forEach(c -> c.execute());
  }
}
