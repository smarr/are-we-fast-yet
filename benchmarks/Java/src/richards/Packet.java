/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 *
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards;


final class Packet extends RBObject {
  public static final int DATA_SIZE = 4;

  Packet(final Packet link, final int identity, final int kind) {
    this.link     = link;
    this.identity = identity;
    this.kind     = kind;
    this.datum    = 0;
    this.data     = new int[DATA_SIZE];
  }

  private Packet      link;
  private int         identity;
  private final int   kind;
  private int         datum;
  private final int[] data;

  public int[] getData() { return data; }
  public int   getDatum() { return datum; }
  public void  setDatum(final int someData) { datum = someData; }

  public int  getIdentity() { return identity; }
  public void setIdentity(final int anIdentity) { identity = anIdentity; }

  public int getKind() { return kind; }
  public Packet getLink() { return link; }
  public void setLink(final Packet aLink) { link = aLink; }

  @Override
  public String toString() {
    return "Packet id: " + identity + " kind: " + kind;
  }
}
