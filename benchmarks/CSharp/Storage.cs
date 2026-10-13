using Random = Benchmarks.Som.Random;

namespace Benchmarks;

internal sealed class Storage : Benchmark
{
    private int _count;

    public override object Execute()
    {
        Random random = new();
        _count = 0;
        BuildTreeDepth(7, random);
        return _count;
    }

    private object BuildTreeDepth(int depth, Random random)
    {
        _count++;
        if (depth == 1)
        {
            return new object[random.Next() % 10 + 1];
        }

        object[] arr = new object[4];
        for (int i = 0; i < 4; i++)
        {
            arr[i] = BuildTreeDepth(depth - 1, random);
        }

        return arr;
    }

    public override bool VerifyResult(object result) => 5461 == (int)result;
}
