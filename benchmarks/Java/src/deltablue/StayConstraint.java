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

// I mark variables that should, with some level of preference, stay
// the same. I have one method with zero inputs and one output, which
// does nothing. Planners may exploit the fact that, if I am
// satisfied, my output will not change during plan execution. This is
// called "stay optimization".
//
final class StayConstraint extends UnaryConstraint {

  // Install a stay constraint with the given strength on the given variable.
  StayConstraint(final Variable v, final Sym strength,
      final Planner planner) {
   super(v, strength, planner);
  }

  @Override
  public void execute() { } // Stay constraints do nothing.
}
