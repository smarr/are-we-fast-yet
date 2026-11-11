namespace Benchmarks.Som;

internal class Dictionary<TK, TV> where TK : class, ICustomHash
{
    protected const int InitialCapacity = 16;

    private Entry?[] _buckets;
    private int _size;

    protected class Entry
    {
        public readonly int Hash;
        public readonly TK Key;
        public TV Value;
        public Entry? Next;

        public Entry(int hash, TK key, TV value, Entry? next)
        {
            Hash = hash;
            Key = key;
            Value = value;
            Next = next;
        }

        public virtual bool Match(int hash, TK key) => Hash == hash && key.Equals(Key);
    }

    public Dictionary(int size)
    {
        _buckets = new Entry[size];
    }

    public Dictionary() : this(InitialCapacity)
    {
    }

    private static int CalculateHash(TK? key)
    {
        if (key == null)
        {
            return 0;
        }

        int hash = key.CustomHash();
        return hash ^ (hash >>> 16);
    }

    private int GetBucketIdx(int hash) => (_buckets.Length - 1) & hash;

    private Entry? GetBucket(int hash) => _buckets[GetBucketIdx(hash)];

    public TV? At(TK key)
    {
        int hash = CalculateHash(key);
        Entry? e = GetBucket(hash);

        while (e != null)
        {
            if (e.Match(hash, key))
            {
                return e.Value;
            }

            e = e.Next;
        }

        return default;
    }
    public void AtPut(TK key, TV value)
    {
        int hash = CalculateHash(key);
        int i = GetBucketIdx(hash);

        Entry? current = _buckets[i];

        if (current == null)
        {
            _buckets[i] = NewEntry(key, value, hash);
            _size += 1;
        }
        else
        {
            InsertBucketEntry(key, value, hash, current);
        }

        if (_size > _buckets.Length)
        {
            Resize();
        }
    }

    protected virtual Entry NewEntry(TK key, TV value, int hash) => new(hash, key, value, null);

    private void InsertBucketEntry(TK key, TV value, int hash, Entry head)
    {
        Entry current = head;

        while (true)
        {
            if (current.Match(hash, key))
            {
                current.Value = value;
                return;
            }

            if (current.Next == null)
            {
                _size++;
                current.Next = NewEntry(key, value, hash);
                return;
            }

            current = current.Next;
        }
    }

    private void Resize()
    {
        Entry?[] oldStorage = _buckets;

        Entry?[] newStorage = new Entry?[oldStorage.Length * 2];
        _buckets = newStorage;
        TransferEntries(oldStorage);
    }

    private void TransferEntries(Entry?[] oldStorage)
    {
        for (int i = 0; i < oldStorage.Length; ++i)
        {
            Entry? current = oldStorage[i];
            if (current != null)
            {
                oldStorage[i] = null;

                if (current.Next == null)
                {
                    _buckets[current.Hash & (_buckets.Length - 1)] = current;
                }
                else
                {
                    SplitBucket(oldStorage, i, current);
                }
            }
        }
    }

    private void SplitBucket(Entry?[] oldStorage, int i, Entry? head)
    {
        Entry? loHead = null;
        Entry? loTail = null;
        Entry? hiHead = null;
        Entry? hiTail = null;
        Entry? current = head;

        while (current != null)
        {
            if ((current.Hash & oldStorage.Length) == 0)
            {
                if (loTail == null)
                {
                    loHead = current;
                }
                else
                {
                    loTail.Next = current;
                }

                loTail = current;
            }
            else
            {
                if (hiTail == null)
                {
                    hiHead = current;
                }
                else
                {
                    hiTail.Next = current;
                }

                hiTail = current;
            }

            current = current.Next;
        }

        if (loTail != null)
        {
            loTail.Next = null;
            _buckets[i] = loHead;
        }

        if (hiTail != null)
        {
            hiTail.Next = null;
            _buckets[i + oldStorage.Length] = hiHead;
        }
    }

    public void RemoveAll()
    {
        _buckets = new Entry[_buckets.Length];
        _size = 0;
    }

    public Vector<TK> GetKeys()
    {
        Vector<TK> keys = new(_size);
        foreach (Entry? c in _buckets)
        {
            Entry? current = c;
            while (current != null)
            {
                keys.Append(current.Key);
                current = current.Next;
            }
        }

        return keys;
    }
}
