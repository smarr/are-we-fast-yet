namespace Benchmarks;

public static class CollectionConstants
{
  public const int InitialSize = 10;
}

public sealed class Pair<K, V>
{
  public K Key { get; set; }
  private V Value { get; set; }

  public Pair(K key, V value)
  {
    Key = key;
    Value = value;
  }
}

public delegate void ForEach<E>(E elem);

public delegate bool Test<E>(E elem);

public delegate T Collect<E, T>(E o);

/**
 * Porting notes:
 *  - does not use an explicit array bounds check, because Java already does
 *    that. Don't see a point in doing it twice.
 */
public class Vector<TE> where TE : class
{
  private TE[] storage;
  private int firstIdx;
  private int lastIdx;

  public static Vector<TE> With(TE elem)
  {
    Vector<TE> v = new Vector<TE>(1);
    v.Append(elem);
    return v;
  }

  public Vector(int size)
  {
    storage = new TE[size];
  }

  public Vector() : this(50)
  {
  }

  public TE? At(int idx)
  {
    if (idx >= storage.Length)
    {
      return default(TE);
    }

    return storage[idx];
  }

  public void AtPut(int idx, TE val)
  {
    if (idx >= storage.Length)
    {
      int newLength = storage.Length;
      while (newLength <= idx)
      {
        newLength *= 2;
      }

      TE[] newStorage = new TE[newLength];
      storage.CopyTo(newStorage, 0);
      storage = newStorage;
    }

    storage[idx] = val;
    if (lastIdx < idx + 1)
    {
      lastIdx = idx + 1;
    }
  }

  public void Append(TE elem)
  {
    if (lastIdx >= storage.Length)
    {
      // Need to expand capacity first
      TE[] newStorage = new TE[2 * storage.Length];
      storage.CopyTo(newStorage, 0);
      storage = newStorage;
    }

    storage[lastIdx] = elem;
    lastIdx++;
  }

  public bool IsEmpty()
  {
    return lastIdx == firstIdx;
  }

  public void ForEach(ForEach<TE> fn)
  {
    for (int i = firstIdx; i < lastIdx; i++)
    {
      fn.Invoke(storage[i]);
    }
  }

  public bool HasSome(Test<TE> fn)
  {
    for (int i = firstIdx; i < lastIdx; i++)
    {
      if (fn.Invoke(storage[i]))
      {
        return true;
      }
    }

    return false;
  }

  public TE? GetOne(Test<TE> fn)
  {
    for (int i = firstIdx; i < lastIdx; i++)
    {
      TE e = storage[i];
      if (fn.Invoke(e))
      {
        return e;
      }
    }

    return default(TE);
  }

  public TE? First()
  {
    if (IsEmpty())
    {
      return default(TE);
    }

    return storage[firstIdx];
  }

  public TE? RemoveFirst()
  {
    if (IsEmpty())
    {
      return default(TE);
    }

    firstIdx++;
    return storage[firstIdx - 1];
  }

  public bool Remove(TE obj)
  {
    TE[] newArray = new TE[Capacity()];
    int[] newLast = new int[] {0};
    bool[] found = new bool[] {false};

    ForEach(it =>
    {
      if (it == obj)
      {
        found[0] = true;
      }
      else
      {
        newArray[newLast[0]] = it;
        newLast[0]++;
      }
    });

    storage = newArray;
    lastIdx = newLast[0];
    firstIdx = 0;
    return found[0];
  }

  public void RemoveAll()
  {
    firstIdx = 0;
    lastIdx = 0;
    storage = new TE[storage.Length];
  }

  public int Size()
  {
    return lastIdx - firstIdx;
  }

  public int Capacity()
  {
    return storage.Length;
  }

  public void Sort(Comparer<TE>? c)
  {
    if (Size() > 0)
    {
      Sort(firstIdx, lastIdx - 1, c);
    }
  }

  private void Sort(int i, int j, Comparer<TE>? c)
  {
    if (c == null)
    {
      DefaultSort(i, j);
    }

    int n = j + 1 - i;
    if (n <= 1)
    {
      return;
    }

    TE di = storage[i];
    TE dj = storage[j];

    if (c!.Compare(di, dj) > 0)
    {
      Swap(storage, i, j);
      TE tt = di;
      di = dj;
      dj = tt;
    }

    if (n > 2)
    {
      int ij = (i + j) / 2;
      TE dij = storage[ij];

      if (c.Compare(di, dij) <= 0)
      {
        if (c.Compare(dij, dj) > 0)
        {
          Swap(storage, j, ij);
          dij = dj;
        }
      }
      else
      {
        Swap(storage, i, ij);
        dij = di;
      }

      if (n > 3)
      {
        int k = i;
        int l = j - 1;

        while (true)
        {
          while (k <= l && c.Compare(dij, storage[l]) <= 0)
          {
            l -= 1;
          }

          k += 1;
          while (k <= l && c.Compare(storage[k], dij) <= 0)
          {
            k += 1;
          }

          if (k > l)
          {
            break;
          }

          Swap(storage, k, l);
        }

        Sort(i, l, c);
        Sort(k, j, c);
      }
    }
  }

  private static void Swap(TE[] storage2, int i, int j)
  {
    throw new NotImplementedException();
  }

  private void DefaultSort(int i, int j)
  {
    throw new NotImplementedException();
  }
}

public class Set<TE> where TE : class
{
  private readonly Vector<TE> items;

  public Set() : this(CollectionConstants.InitialSize)
  {
  }

  public Set(int size)
  {
    items = new Vector<TE>(size);
  }

  public int Size()
  {
    return items.Size();
  }

  public void ForEach(ForEach<TE> fn)
  {
    items.ForEach(fn);
  }

  public bool HasSome(Test<TE> fn)
  {
    return items.HasSome(fn);
  }

  public TE? GetOne(Test<TE> fn)
  {
    return items.GetOne(fn);
  }

  public void Add(TE obj)
  {
    if (!Contains(obj))
    {
      items.Append(obj);
    }
  }

  public Vector<T> Collect<T>(Collect<TE, T> fn) where T : class
  {
    Vector<T> coll = new Vector<T>();

    ForEach(e => { coll.Append(fn.Invoke(e)); });
    return coll;
  }

  public virtual bool Contains(TE obj)
  {
    return HasSome(e => { return e.Equals(obj); });
  }

  public void RemoveAll()
  {
    items.RemoveAll();
  }
}

public sealed class IdentitySet<TE> : Set<TE> where TE : class
{
  public IdentitySet() : base()
  {
  }

  public IdentitySet(int size) : base(size)
  {
  }

  public override bool Contains(TE obj)
  {
    return HasSome(e => { return e == obj; });
  }
}

public interface Comparable<V>
{
  int CompareTo(V v);
}

public interface CustomHash
{
  int CustomHash();
}

public class Dictionary<TK, TV> where TK : class, CustomHash where TV : class
{
  protected const int InitialCapacity = 16;

  private Entry?[] buckets;
  private int size;

  protected class Entry
  {
    public int Hash { get; }
    public TK Key { get; }
    public TV Value { get; set; }
    public Entry? Next { get; set; }

    public Entry(int hash, TK key, TV value, Entry? next)
    {
      Hash = hash;
      Key = key;
      Value = value;
      Next = next;
    }

    public virtual bool Match(int hash, TK key)
    {
      return Hash == hash && key.Equals(Key);
    }
  }

  public Dictionary(int size)
  {
    this.buckets = new Entry[size];
  }

  public Dictionary() : this(InitialCapacity)
  {
  }

  private static int CalculateHash(TK key)
  {
    if (key == null)
    {
      return 0;
    }

    int hash = key.CustomHash();
    return hash ^ hash >> 16;
  }

  public virtual int Size()
  {
    return size;
  }

  public bool IsEmpty()
  {
    return size == 0;
  }

  private int GetBucketIdx(int hash)
  {
    return (buckets.Length - 1) & hash;
  }

  private Entry? GetBucket(int hash)
  {
    return buckets[GetBucketIdx(hash)];
  }

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

    return default(TV);
  }

  public bool ContainsKey(TK key)
  {
    int hash = CalculateHash(key);
    Entry? e = GetBucket(hash);

    while (e != null)
    {
      if (e.Match(hash, key))
      {
        return true;
      }

      e = e.Next;
    }

    return false;
  }

  public void AtPut(TK key, TV value)
  {
    int hash = CalculateHash(key);
    int i = GetBucketIdx(hash);

    Entry? current = buckets[i];

    if (current == null)
    {
      buckets[i] = NewEntry(key, value, hash);
      size += 1;
    }
    else
    {
      InsertBucketEntry(key, value, hash, current);
    }

    if (size > buckets.Length)
    {
      Resize();
    }
  }

  protected virtual Entry NewEntry(TK key, TV value, int hash)
  {
    return new Entry(hash, key, value, null);
  }

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
        size += 1;
        current.Next = NewEntry(key, value, hash);
        return;
      }

      current = current.Next;
    }
  }

  private void Resize()
  {
    Entry?[] oldStorage = buckets;

    Entry?[] newStorage = new Entry?[oldStorage.Length * 2];
    buckets = newStorage;
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
          buckets[current.Hash & (buckets.Length - 1)] = current;
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
      buckets[i] = loHead;
    }

    if (hiTail != null)
    {
      hiTail.Next = null;
      buckets[i + oldStorage.Length] = hiHead;
    }
  }

  public void RemoveAll()
  {
    buckets = new Entry[buckets.Length];
    size = 0;
  }

  public Vector<TK> GetKeys()
  {
    Vector<TK> keys = new Vector<TK>(size);
    for (int i = 0; i < buckets.Length; ++i)
    {
      Entry? current = buckets[i];
      while (current != null)
      {
        keys.Append(current.Key);
        current = current.Next;
      }
    }

    return keys;
  }

  public Vector<TV> GetValues()
  {
    Vector<TV> values = new Vector<TV>(size);
    for (int i = 0; i < buckets.Length; ++i)
    {
      Entry? current = buckets[i];
      while (current != null)
      {
        values.Append(current.Value);
        current = current.Next;
      }
    }

    return values;
  }
}

public class IdentityDictionary<K, V> : Dictionary<K, V> where K : class, CustomHash where V : class
{
  private sealed class IdEntry : Entry
  {
    public IdEntry(int hash, K key, V value, Entry? next) : base(hash, key, value, next)
    {
    }

    public override bool Match(int hash, K key)
    {
      return Hash == hash && Key == key;
    }
  }

  public IdentityDictionary(int size) : base(size)
  {
  }

  public IdentityDictionary() : base(InitialCapacity)
  {
  }

  protected override Entry NewEntry(K key, V value, int hash)
  {
    return new IdEntry(hash, key, value, null);
  }
}

public class Random
{
  private int seed = 74755;

  public int Next()
  {
    seed = ((seed * 1309) + 13849) & 65535;
    return seed;
  }
}