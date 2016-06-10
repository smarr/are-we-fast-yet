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

// I relate two variables by the linear scaling relationship: "v2 =
// (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
// this relationship but the scale factor and offset are considered
// read-only.
final class ScaleConstraint extends BinaryConstraint {

  protected final Variable scale;  // scale factor input variable
  protected final Variable offset; // offset input variable

  ScaleConstraint(final Variable src, final Variable scale,
      final Variable offset, final Variable dest, final Sym strength,
      final Planner planner) {
    super(src, dest, strength, planner);
    this.scale = scale;
    this.offset = offset;
    addConstraint(planner);
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
    if (direction == Direction.FORWARD) {
      v2.setValue(v1.getValue() * scale.getValue() + offset.getValue());
    } else {
      v1.setValue((v2.getValue() - offset.getValue()) / scale.getValue());
    }
  }

  @Override
  public void inputsDo(final ForEachInterface<Variable> fn) {
    if (direction == Direction.FORWARD) {
      fn.apply(v1);
      fn.apply(scale);
      fn.apply(offset);
    } else {
      fn.apply(v2);
      fn.apply(scale);
      fn.apply(offset);
    }
  }

  // Calculate the walkabout strength, the stay flag, and, if it is
  // 'stay', the value for the current output of this
  // constraint. Assume this constraint is satisfied.
  @Override
  public void recalculate() {
    Variable in;
    Variable out;

    if (direction == Direction.FORWARD) {
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
}
