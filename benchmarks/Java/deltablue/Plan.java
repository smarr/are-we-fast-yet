package som.deltablue;

import java.util.Vector;

// A Plan is an ordered list of constraints to be executed in sequence
// to resatisfy all currently satisfiable constraints in the face of
// one or more changing inputs.
class Plan extends Vector<Constraint> {
  private static final long serialVersionUID = -5753541792336307202L;

  public void execute() {
    this.forEach(c -> c.execute());
  }
}
