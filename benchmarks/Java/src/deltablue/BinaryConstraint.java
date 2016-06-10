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
import som.ForEachInterface;
import som.TestInterface;

// I am an abstract superclass for constraints having two possible
// output variables.
abstract class BinaryConstraint extends AbstractConstraint {

  protected Variable v1;
  protected Variable v2;          // possible output variables
  protected Direction direction;  // one of the following...

  BinaryConstraint(final Variable var1, final Variable var2,
      final Sym strength, final Planner planner) {
    super(strength);
    v1 = var1;
    v2 = var2;
    direction = null;
  }

  // Answer true if this constraint is satisfied in the current solution.
  @Override
  public boolean isSatisfied() {
    return direction != null;
  }

  // Add myself to the constraint graph.
  @Override
  public void addToGraph() {
    v1.addConstraint(this);
    v2.addConstraint(this);
    direction = null;
  }

  // Remove myself from the constraint graph.
  @Override
  public void removeFromGraph() {
    if (v1 != null) {
      v1.removeConstraint(this);
    }
    if (v2 != null) {
      v2.removeConstraint(this);
    }
    direction = null;
  }

  // Decide if I can be satisfied and which way I should flow based on
  // the relative strength of the variables I relate, and record that
  // decision.
  //
  @Override
  protected Direction chooseMethod(final int mark) {
    if (v1.getMark() == mark) {
      if (v2.getMark() != mark && strength.stronger(v2.getWalkStrength())) {
        direction = Direction.FORWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }

    if (v2.getMark() == mark) {
      if (v1.getMark() != mark && strength.stronger(v1.getWalkStrength())) {
        direction = Direction.BACKWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }

    // If we get here, neither variable is marked, so we have a choice.
    if (v1.getWalkStrength().weaker(v2.getWalkStrength())) {
      if (strength.stronger(v1.getWalkStrength())) {
        direction = Direction.BACKWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    } else {
      if (strength.stronger(v2.getWalkStrength())) {
        direction = Direction.FORWARD;
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }
  }

  @Override
  public void inputsDo(final ForEachInterface<Variable> fn) {
    if (direction == Direction.FORWARD) {
      fn.apply(v1);
    } else {
      fn.apply(v2);
    }
  }

  @Override
  public boolean inputsHasOne(final TestInterface<Variable> fn) {
    if (direction == Direction.FORWARD) {
      return fn.test(v1);
    } else {
      return fn.test(v2);
    }
  }

  // Record the fact that I am unsatisfied.
  @Override
  public void markUnsatisfied() {
    direction = null;
  }


  // Answer my current output variable.
  @Override
  public Variable getOutput() {
    return direction == Direction.FORWARD ? v2 : v1;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  //
  @Override
  public void recalculate() {
    Variable in;
    Variable out;

    if (direction == Direction.FORWARD) {
      in = v1; out = v2;
    } else {
      in = v2; out = v1;
    }

    out.setWalkStrength(strength.weakest(in.getWalkStrength()));
    out.setStay(in.getStay());
    if (out.getStay()) {
      execute();
    }
  }
}
