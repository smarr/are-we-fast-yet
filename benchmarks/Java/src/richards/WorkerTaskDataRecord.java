/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 * 
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards;


final class WorkerTaskDataRecord extends RBObject {
  private int destination;
  private int count;

  WorkerTaskDataRecord() {
    destination = HANDLER_A;
    count = 0;
  }

  public int getCount() { return count; }
  public void setCount(final int aCount) { count = aCount; }

  public int getDestination() { return destination; }
  public void setDestination(final int aHandler) { destination = aHandler; }
}
