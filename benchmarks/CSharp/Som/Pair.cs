namespace Benchmarks.Som;

internal sealed class Pair<TK, TV>
{
    public TK Key { get; set; }
    private TV Value { get; set; }

    public Pair(TK key, TV value)
    {
        Key = key;
        Value = value;
    }
}
