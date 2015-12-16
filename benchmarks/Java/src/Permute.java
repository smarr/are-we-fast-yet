

public final class Permute extends Benchmark {
  private int count;
  private int[] v;

  @Override
  public Object benchmark() {
    count = 0;
    v     = new int[6];
    permute(6);
    return count;
  }

  void permute(final int n) {
    count++;
    if (n != 0) {
      int n1 = n - 1;
      permute(n1);
      for (int i = n1; i >= 0; i--) {
        swap(n1, i);
        permute(n1);
        swap(n1, i);
      }
    }
  }

  private void swap(final int i, final int j) {
    int tmp = v[i];
    v[i] = v[j];
    v[j] = tmp;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return (int) result == 8660;
  }

}
