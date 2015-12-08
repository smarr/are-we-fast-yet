package deltablue;

// I am an abstract superclass for constraints having two possible
// output variables.
abstract class BinaryConstraint extends Constraint {

  protected Variable v1, v2;          // possible output variables
  protected String   direction;       // one of the following...

  public void set(final Variable var1, final Variable var2, final String strength) {
    this.strength = Strength.of(strength);
    v1 = var1;
    v2 = var2;
    direction = null;
    addConstraint();
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
  protected String chooseMethod(final int mark) {
    if (v1.getMark() == mark) {
      if (v2.getMark() != mark && strength.stronger(v2.getWalkStrength())) {
        direction = "forward";
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }

    if (v2.getMark() == mark) {
      if (v1.getMark() != mark && strength.stronger(v1.getWalkStrength())) {
        direction = "backward";
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }

    // If we get here, neither variable is marked, so we have a choice.
    if (v1.getWalkStrength().weaker(v2.getWalkStrength())) {
      if (strength.stronger(v1.getWalkStrength())) {
        direction = "backward";
        return direction;
      } else {
        direction = null;
        return direction;
      }
    } else {
      if (strength.stronger(v2.getWalkStrength())) {
        direction = "forward";
        return direction;
      } else {
        direction = null;
        return direction;
      }
    }
  }

  @Override
  public void inputsDo(final Constraint.BlockFunction block) throws BlockFunction.Return {
    if (direction == "forward") {
      block.apply(v1);
    } else {
      block.apply(v2);
    }
  }

  // Record the fact that I am unsatisfied.
  @Override
  public void markUnsatisfied() {
    direction = null;
  }


  // Answer my current output variable.
  @Override
  public Variable output() {
    return direction == "forward" ? v2 : v1;
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  //
  @Override
  public void recalculate() {
    Variable in, out;

    if (direction == "forward") {
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
