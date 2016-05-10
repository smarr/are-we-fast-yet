package cd;


public class CallSign implements Comparable<CallSign> {
  private final int value;

  public CallSign(final int value) {
    this.value = value;
  }

  @Override
  public int compareTo(final CallSign other) {
    return (value == other.value) ? 0 : ((value < other.value) ? -1 : 1);
  }
}
