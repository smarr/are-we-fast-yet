namespace Benchmarks.Som;

internal class Set<TE>
{
    private readonly Vector<TE> _items;

    public Set() : this(CollectionConstants.InitialSize)
    {
    }

    public Set(int size)
    {
        _items = new Vector<TE>(size);
    }

    public int Size() => _items.Size();

    public void ForEach(Action<TE> fn) => _items.ForEach(fn);

    public bool HasSome(Predicate<TE> fn) => _items.HasSome(fn);

    public TE? GetOne(Predicate<TE> fn) => _items.GetOne(fn);

    public void Add(TE obj)
    {
        if (!Contains(obj))
        {
            _items.Append(obj);
        }
    }

    public Vector<T> Collect<T>(Func<TE, T> fn) where T : class
    {
        Vector<T> coll = new();

        ForEach(e => coll.Append(fn.Invoke(e)));
        return coll;
    }

    public virtual bool Contains(TE obj) => HasSome(e => e!.Equals(obj));

    public void RemoveAll() => _items.RemoveAll();
}
