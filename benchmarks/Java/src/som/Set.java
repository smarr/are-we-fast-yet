package som;

import java.util.Iterator;

public abstract class Set<E> implements Iterable<E> {
  private final Vector<E> items;

  public Set() {
    this(Constants.INITIAL_SIZE);
  }

  public Set(final int size) {
    items = new Vector<E>(size);
  }

  @Override
  public Iterator<E> iterator() {
    return items.iterator();
  }

  public void add(final E obj) {
    if (!contains(obj)) {
      items.append(obj);
    }
  }

  public <T> Vector<T> collect(final CollectInterface<E, T> fn) {
    Vector<T> coll = new Vector<T>();
    for (E e : this) {
      coll.append(fn.collect(e));
    }
    return coll;
  }

  // TODO: made Set abstract, because we did not need it as a concrete type yet.
  public abstract boolean contains(E obj);
}
