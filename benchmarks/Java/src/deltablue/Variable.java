/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue;

import som.Vector;

// ------------------------------ variables ------------------------------

// I represent a constrained variable. In addition to my value, I
// maintain the structure of the constraint graph, the current
// dataflow graph, and various parameters of interest to the DeltaBlue
// incremental constraint solver.
final class Variable {

  private int value;       // my value; changed by constraints
  private final Vector<AbstractConstraint> constraints; // normal constraints that reference me
  private AbstractConstraint determinedBy; // the constraint that currently determines
  // my value (or null if there isn't one)
  private int mark;        // used by the planner to mark constraints
  private Strength walkStrength; // my walkabout strength
  private boolean  stay;        // true if I am a planning-time constant

  public static Variable value(final int aValue) {
    Variable v = new Variable();
    v.setValue(aValue);
    return v;
  }

  Variable() {
    value = 0;
    constraints = new Vector<>(2);
    determinedBy = null;
    walkStrength = Strength.absoluteWeakest();
    stay = true;
    mark = 0;
  }

  // Add the given constraint to the set of all constraints that refer to me.
  public void addConstraint(final AbstractConstraint c) {
    constraints.append(c);
  }

  public Vector<AbstractConstraint> getConstraints() {
    return constraints;
  }

  public AbstractConstraint getDeterminedBy() {
    return determinedBy;
  }

  public void setDeterminedBy(final AbstractConstraint c) {
    determinedBy = c;
  }

  public int getMark() {
    return mark;
  }

  public void setMark(final int markValue) {
    mark = markValue;
  }

  // Remove all traces of c from this variable.
  public void removeConstraint(final AbstractConstraint c) {
    constraints.remove(c);
    if (determinedBy == c) {
      determinedBy = null;
    }
  }

  public boolean getStay() {
    return stay;
  }

  public void setStay(final boolean v) {
    stay = v;
  }

  public int getValue() {
    return value;
  }

  public void setValue(final int value) {
    this.value = value;
  }

  public Strength getWalkStrength() {
    return walkStrength;
  }

  public void setWalkStrength(final Strength strength) {
    walkStrength = strength;
  }
}
