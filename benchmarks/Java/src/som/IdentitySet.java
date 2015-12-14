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
    return hasSome(e -> { return e == obj; } );
  }
}
