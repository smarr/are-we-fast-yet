namespace Benchmarks;

internal class Permute : Benchmark
{
    private int _count;
    private int[] _v = null!;

    public override object Execute()
    {
        _count = 0;
        _v = new int[6];
        CalculatePermutations(6);
        return _count;
    }

    private void CalculatePermutations(int n)
    {
        _count++;
        if (n != 0)
        {
            int n1 = n - 1;
            CalculatePermutations(n1);
            for (int i = n1; i >= 0; i--)
            {
                Swap(n1, i);
                CalculatePermutations(n1);
                Swap(n1, i);
            }
        }
    }

    private void Swap(int i, int j)
    {
        int tmp = _v[i];
        _v[i] = _v[j];
        _v[j] = tmp;
    }

    public override bool VerifyResult(object result) => (int)result == 8660;
}