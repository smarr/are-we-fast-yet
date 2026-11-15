namespace Benchmarks.Som;

internal sealed class IdentitySet<TE> : Set<TE> where TE : class
{
    public IdentitySet()
    {
    }

    public IdentitySet(int size) : base(size)
    {
    }

    public override bool Contains(TE obj) => HasSome(e => e == obj);
}
