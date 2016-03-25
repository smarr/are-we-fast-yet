/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 * 
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards;


final class DeviceTaskDataRecord extends RBObject {
  private Packet pending;

  DeviceTaskDataRecord() {
    pending = NO_WORK;
  }

  public Packet getPending() { return pending; }
  public void setPending(final Packet packet) { pending = packet; }
}
