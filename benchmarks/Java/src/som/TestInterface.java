package som;


@FunctionalInterface
public interface TestInterface<E> {
  boolean test(E elem);
}
