package som;


public final class IdentitySet<E> extends Set<E> {

  public IdentitySet() {
    super();
  }

  public IdentitySet(final int size) {
    super(size);
  }

  @Override
  public boolean contains(final E obj) {
    for (E e : this) {
      if (e == obj) {
        return true;
      }
    }

    return false;
  }
}
