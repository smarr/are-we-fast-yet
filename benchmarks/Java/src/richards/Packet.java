package richards;


public class Packet extends RBObject {
  public static Packet create(final Packet link, final int identity, final int kind) {
    Packet p = new Packet();
    p.initialize(link, identity, kind);
    return p;
  }

  private Packet link;
  private int    identity;
  private int    kind;
  private int    datum;
  private int[]  data;

  public int[] getData() { return data; }
  public int   getDatum() { return datum; }
  public void  setDatum(final int someData) { datum = someData; }

  public int  getIdentity() { return identity; }
  public void setIdentity(final int anIdentity) { identity = anIdentity; }

  public int getKind() { return kind; }
  public Packet getLink() { return link; }
  public void setLink(final Packet aLink) { link = aLink; }

  public void initialize(final Packet aLink, final int anIdentity, final int aKind) {
    link     = aLink;
    identity = anIdentity;
    kind     = aKind;
    datum    = 1;
    data     = new int[4];
  }
}
