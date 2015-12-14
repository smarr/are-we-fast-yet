package som;

public abstract class Set<E> {
  private final Vector<E> items;

  public Set() {
    this(Constants.INITIAL_SIZE);
  }

  public Set(final int size) {
    items = new Vector<E>(size);
  }

  public void forEach(final ForEachInterface<E> fn) {
    items.forEach(fn);
  }

  public boolean hasSome(final TestInterface<E> fn) {
    return items.hasSome(fn);
  }

  public E getOne(final TestInterface<E> fn) {
    return items.getOne(fn);
  }

  public void add(final E obj) {
    if (!contains(obj)) {
      items.append(obj);
    }
  }

  public <T> Vector<T> collect(final CollectInterface<E, T> fn) {
    Vector<T> coll = new Vector<T>();

    forEach(e -> {
      coll.append(fn.collect(e));
    });
    return coll;
  }

  // TODO: made Set abstract, because we did not need it as a concrete type yet.
  public abstract boolean contains(E obj);
}
