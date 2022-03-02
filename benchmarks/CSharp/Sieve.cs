using System;
using System.Collections;

public class Sieve : Benchmark
{
  public static void Main(string[] args)
  {
    Sieve s = new Sieve();
    Object result = s.benchmark();
    bool rr = s.verifyResult(result);
  }

  public override Object benchmark()
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

  public override bool verifyResult(Object result)
  {
    return 669 == (int)result;
  }
}