namespace Benchmarks;

public class Sieve : Benchmark
{
  public override object Execute()
  {
    bool[] flags = new bool[5000];
    Array.Fill(flags, true);
    return sieve(flags, 5000);
  }

  int sieve(bool[] flags, int size)
  {
    int primeCount = 0;

    for (int i = 2; i <= size; i++)
    {
      if (flags[i - 1])
      {
        primeCount++;
        int k = i + i;
        while (k <= size)
        {
          flags[k - 1] = false;
          k += i;
        }
      }
    }

    return primeCount;
  }

  public override bool VerifyResult(object result)
  {
    return 669 == (int) result;
  }
}