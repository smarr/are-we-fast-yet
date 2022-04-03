namespace Benchmarks;

public class Permute : Benchmark
{
  int count;
  int[] v;

  public override object Execute()
  {
    count = 0;
    v = new int[6];
    _permute(6);
    return count;
  }

  private void _permute(int n)
  {
    count++;
    if (n != 0)
    {
      int n1 = n - 1;
      _permute(n1);
      for (int i = n1; i >= 0; i--)
      {
        _swap(n1, i);
        _permute(n1);
        _swap(n1, i);
      }
    }
  }

  private void _swap(int i, int j)
  {
    int tmp = v[i];
    v[i] = v[j];
    v[j] = tmp;
  }

  public override bool VerifyResult(object result)
  {
    return (int)result == 8660;
  }
}