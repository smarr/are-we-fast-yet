/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 * 
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards;


final class IdleTaskDataRecord extends RBObject {
  private int control;
  private int count;

  public int getControl() { return control; }
  public void setControl(final int aNumber) {
    control = aNumber;
  }

  public int getCount() { return count; }
  public void setCount(final int aCount) { count = aCount; }

  IdleTaskDataRecord() {
    control = 1;
    count = 10000;
  }
}
