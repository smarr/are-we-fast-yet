/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue;

import deltablue.Strength.Sym;

// I constrain two variables to have the same value: "v1 = v2".
final class EqualityConstraint extends BinaryConstraint {

  // Install a constraint with the given strength equating the given
  // variables.
  EqualityConstraint(final Variable var1, final Variable var2,
      final Sym strength, final Planner planner) {
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
