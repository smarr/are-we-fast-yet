namespace Benchmarks.Som;

internal class Random
{
    private int _seed = 74755;

    public int Next()
    {
        _seed = (_seed * 1309 + 13849) & 65535;
        return _seed;
    }
}
