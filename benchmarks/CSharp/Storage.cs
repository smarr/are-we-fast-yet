namespace Benchmarks;

public class Storage : Benchmark
{
  private int count;

  public override object Execute()
  {
    Random random = new Random();
    count = 0;
    BuildTreeDepth(7, random);
    return count;
  }

  private object BuildTreeDepth(int depth, Random random)
  {
    count++;
    if (depth == 1)
    {
      return new object[random.Next() % 10 + 1];
    }
    else
    {
      object[] arr = new object[4];
      for (int i = 0; i < 4; i++)
      {
        arr[i] = this.BuildTreeDepth(depth - 1, random);
      }

      return arr;
    }
  }

  public override bool VerifyResult(object result)
  {
    return 5461 == (int) result;
  }
}