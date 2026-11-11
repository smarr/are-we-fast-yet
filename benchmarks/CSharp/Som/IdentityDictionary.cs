namespace Benchmarks.Som;

internal class IdentityDictionary<TK, TV> : Dictionary<TK, TV> where TK : class, ICustomHash
{
    private sealed class IdEntry : Entry
    {
        public IdEntry(int hash, TK key, TV value, Entry? next) : base(hash, key, value, next)
        {
        }

        public override bool Match(int hash, TK key) => Hash == hash && Key == key;
    }

    public IdentityDictionary(int size) : base(size)
    {
    }

    public IdentityDictionary() : base(InitialCapacity)
    {
    }

    protected override Entry NewEntry(TK key, TV value, int hash) => new IdEntry(hash, key, value, null);
}
