package deltablue;

import deltablue.Constraint.BlockFunction.Return;

// I relate two variables by the linear scaling relationship: "v2 =
// (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
// this relationship but the scale factor and offset are considered
// read-only.
class ScaleConstraint extends BinaryConstraint {

  protected Variable scale; // scale factor input variable
  protected Variable offset; // offset input variable

  // Install a scale constraint with the given strength on the given
  // variables.
  public void set(final Variable src, final Variable scale,
      final Variable offset, final Variable dest, final String strength) {
    this.strength = Strength.of(strength);
    v1 = src;
    v2 = dest;
    this.scale = scale;
    this.offset = offset;
    direction = null;
    addConstraint();
  }

  // Add myself to the constraint graph.
  @Override
  public void addToGraph() {
    v1.addConstraint(this);
    v2.addConstraint(this);
    scale.addConstraint(this);
    offset.addConstraint(this);
    direction = null;
  }

  // Remove myself from the constraint graph.
  @Override
  public void removeFromGraph() {
    if (v1 != null) { v1.removeConstraint(this); }
    if (v2 != null) { v2.removeConstraint(this); }
    if (scale  != null) { scale.removeConstraint(this); }
    if (offset != null) { offset.removeConstraint(this); }
    direction = null;
  }

  // Enforce this constraint. Assume that it is satisfied.
  @Override
  public void execute() {
    if (direction == "forward") {
      v2.setValue(v1.getValue() * scale.getValue() + offset.getValue());
    } else {
      v1.setValue((v2.getValue() - offset.getValue()) / scale.getValue());
    }
  }

  @Override
  public void inputsDo(final BlockFunction block) throws Return {
    if (direction == "forward") {
      block.apply(v1);
      block.apply(scale);
      block.apply(offset);
    } else {
      block.apply(v2);
      block.apply(scale);
      block.apply(offset);
    }
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  @Override
  public void recalculate() {
    Variable in, out;

    if (direction == "forward") {
      in  = v1; out = v2;
    } else {
      out = v1; in  = v2;
    }

    out.setWalkStrength(strength.weakest(in.getWalkStrength()));
    out.setStay(in.getStay() && scale.getStay() && offset.getStay());
    if (out.getStay()) {
      execute(); // stay optimization
    }
  }

  public static ScaleConstraint var(final Variable src, final Variable scale,
      final Variable offset, final Variable dest, final String strength) {
    ScaleConstraint c = new ScaleConstraint();
    c.set(src, scale, offset, dest, strength);
    return c;
  }
}
