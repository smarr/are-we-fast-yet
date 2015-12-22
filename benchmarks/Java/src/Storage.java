import java.util.Arrays;

import som.Random;


public final class Storage extends Benchmark {

  private int count;

  @Override
  public Object benchmark() {
    Random random = new Random();
    count = 0;
    buildTreeDepth(7, random);
    return count;
  }

  private Object buildTreeDepth(final int depth, final Random random) {
    count++;
    if (depth == 1) {
      return new Object[random.next() % 10 + 1];
    } else {
      Object[] arr = new Object[4];
      Arrays.setAll(arr, v -> buildTreeDepth(depth - 1, random));
      return arr;
    }
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 5461 == (int) result;
  }
}
