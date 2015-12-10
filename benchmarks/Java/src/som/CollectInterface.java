package som;


@FunctionalInterface
public interface CollectInterface<E, T> {
  T collect(E o);
}
