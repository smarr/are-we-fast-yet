public final class List extends Benchmark {

  private static class Element {
    private Object val;
    private Element next;

    public Element(final Object v) {
      val = v;
    }

    public int length() {
      if (next == null) {
        return 1;
      } else {
        return 1 + next.length();
      }
    }

    public Object  getVal()                 { return val; }
    public void    setVal(final Object v)   { val = v; }
    public Element getNext()                { return next; }
    public void    setNext(final Element e) { next = e; }
  }

  @Override
  public Object benchmark() {
    Element result = tail(makeList(15), makeList(10), makeList(6));
    return result.length();
  }

  public Element makeList(final int length) {
    if (length == 0) { return null; } else {
      Element e = new Element(length);
      e.setNext(makeList(length - 1));
      return e;
    }
  }

  public boolean isShorterThan(final Element x, final Element y) {
    Element xTail = x;
    Element yTail = y;

    while (yTail != null) {
      if (xTail == null) { return true; }
      xTail = xTail.getNext();
      yTail = yTail.getNext();
    }
    return false;
  }

  public Element tail(final Element x, final Element y, final Element z) {
    if (isShorterThan(y, x)) {
      return tail(tail(x.getNext(), y, z),
             tail(y.getNext(), z, x),
             tail(z.getNext(), x, y));
    } else {
      return z;
    }
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 10 == (int) result;
  }
}
