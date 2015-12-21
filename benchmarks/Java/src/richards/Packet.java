package richards;


final class Packet extends RBObject {
  public final static int DATA_SIZE = 4;

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
