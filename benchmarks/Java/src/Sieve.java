import java.util.Arrays;


public class Sieve extends Benchmark {

  @Override
  public Object benchmark() {
    boolean[] flags = new boolean[5000];
    return sieve(flags, 5000);
  }

  int sieve(final boolean[] flags, final int size) {
    int primeCount = 0;
    Arrays.fill(flags, true);

    for (int i = 2; i <= size; i++) {
      if (flags[i - 1]) {
        primeCount++;
        int k = i + i;
        while (k <= size) {
          flags[k - 1] = false;
          k += i;
        }
      }
    }
    return primeCount;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 669 == (int) result;
  }
}
