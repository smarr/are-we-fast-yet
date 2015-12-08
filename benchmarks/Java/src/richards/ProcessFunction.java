package som.richards;


@FunctionalInterface
public interface ProcessFunction {
  TaskControlBlock apply(final Packet work, final RBObject word);
}
