using System.Collections;
using System.Collections.Generic;

namespace Benchmarks;

public static class CollectionConstants
{
  public const int INITIAL_SIZE = 10;
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
public class Vector<E> where E : class
{
  private E[] storage;
  private int firstIdx;
  private int lastIdx;

  public static Vector<E> With(E elem)
  {
    Vector<E> v = new Vector<E>(1);
    v.Append(elem);
    return v;
  }

  public Vector(int size)
  {
    storage = new E[size];
  }

  public Vector() : this(50)
  {
  }

  public E? At(int idx)
  {
    if (idx >= storage.Length)
    {
      return default(E);
    }

    return storage[idx];
  }

  public void AtPut(int idx, E val)
  {
    if (idx >= storage.Length)
    {
      int newLength = storage.Length;
      while (newLength <= idx)
      {
        newLength *= 2;
      }

      E[] newStorage = new E[newLength];
      storage.CopyTo(newStorage, 0);
      storage = newStorage;
    }

    storage[idx] = val;
    if (lastIdx < idx + 1)
    {
      lastIdx = idx + 1;
    }
  }

  public void Append(E elem)
  {
    if (lastIdx >= storage.Length)
    {
      // Need to expand capacity first
      E[] newStorage = new E[2 * storage.Length];
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

  public void ForEach(ForEach<E> fn)
  {
    for (int i = firstIdx; i < lastIdx; i++)
    {
      fn.Invoke((E) storage[i]);
    }
  }

  public bool HasSome(Test<E> fn)
  {
    for (int i = firstIdx; i < lastIdx; i++)
    {
      if (fn.Invoke((E) storage[i]))
      {
        return true;
      }
    }

    return false;
  }

  public E? GetOne(Test<E> fn)
  {
    for (int i = firstIdx; i < lastIdx; i++)
    {
      E e = (E) storage[i];
      if (fn.Invoke(e))
      {
        return e;
      }
    }

    return default(E);
  }

  public E? First()
  {
    if (IsEmpty())
    {
      return default(E);
    }

    return (E) storage[firstIdx];
  }

  public E? RemoveFirst()
  {
    if (IsEmpty())
    {
      return default(E);
    }

    firstIdx++;
    return (E) storage[firstIdx - 1];
  }

  public bool Remove(E obj)
  {
    E[] newArray = new E[Capacity()];
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
    storage = new E[storage.Length];
  }

  public int Size()
  {
    return lastIdx - firstIdx;
  }

  public int Capacity()
  {
    return storage.Length;
  }

  public void Sort(Comparer<E> c)
  {
    if (Size() > 0)
    {
      Sort(firstIdx, lastIdx - 1, c);
    }
  }

  private void Sort(int i, int j, Comparer<E> c)
  {
    if (c == null)
    {
      defaultSort(i, j);
    }

    int n = j + 1 - i;
    if (n <= 1)
    {
      return;
    }

    E di = (E) storage[i];
    E dj = (E) storage[j];

    if (c!.Compare(di, dj) > 0)
    {
      swap(storage, i, j);
      E tt = di;
      di = dj;
      dj = tt;
    }

    if (n > 2)
    {
      int ij = (i + j) / 2;
      E dij = (E) storage[ij];

      if (c.Compare(di, dij) <= 0)
      {
        if (c.Compare(dij, dj) > 0)
        {
          swap(storage, j, ij);
          dij = dj;
        }
      }
      else
      {
        swap(storage, i, ij);
        dij = di;
      }

      if (n > 3)
      {
        int k = i;
        int l = j - 1;

        while (true)
        {
          while (k <= l && c.Compare(dij, (E) storage[l]) <= 0)
          {
            l -= 1;
          }

          k += 1;
          while (k <= l && c.Compare((E) storage[k], dij) <= 0)
          {
            k += 1;
          }

          if (k > l)
          {
            break;
          }

          swap(storage, k, l);
        }

        Sort(i, l, c);
        Sort(k, j, c);
      }
    }
  }

  private static void swap(object[] storage2, int i, int j)
  {
    throw new NotImplementedException();
  }

  private void defaultSort(int i, int j)
  {
    throw new NotImplementedException();
  }
}

public class Set<E> where E : class
{
  private readonly Vector<E> items;

  public Set() : this(CollectionConstants.INITIAL_SIZE)
  {
  }

  public Set(int size)
  {
    items = new Vector<E>(size);
  }

  public int Size()
  {
    return items.Size();
  }

  public void ForEach(ForEach<E> fn)
  {
    items.ForEach(fn);
  }

  public bool HasSome(Test<E> fn)
  {
    return items.HasSome(fn);
  }

  public E? GetOne(Test<E> fn)
  {
    return items.GetOne(fn);
  }

  public void Add(E obj)
  {
    if (!Contains(obj))
    {
      items.Append(obj);
    }
  }

  public Vector<T> Collect<T>(Collect<E, T> fn) where T : class
  {
    Vector<T> coll = new Vector<T>();

    ForEach(e => { coll.Append(fn.Invoke(e)); });
    return coll;
  }

  public virtual bool Contains(E obj)
  {
    return HasSome(e => { return e.Equals(obj); });
  }

  public void RemoveAll()
  {
    items.RemoveAll();
  }
}

public sealed class IdentitySet<E> : Set<E> where E : class
{
  public IdentitySet() : base()
  {
  }

  public IdentitySet(int size) : base(size)
  {
  }

  public override bool Contains(E obj)
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

public class Dictionary<K, V> where K : class, CustomHash where V : class
{
  protected const int INITIAL_CAPACITY = 16;

  private Entry?[] buckets;
  private int size;

  protected class Entry
  {
    public int Hash { get; }
    public K Key { get; }
    public V Value { get; set; }
    public Entry? Next { get; set; }

    public Entry(int hash, K key, V value, Entry? next)
    {
      Hash = hash;
      Key = key;
      Value = value;
      Next = next;
    }

    public virtual bool Match(int hash, K key)
    {
      return Hash == hash && key.Equals(Key);
    }
  }

  public Dictionary(int size)
  {
    this.buckets = new Entry[size];
  }

  public Dictionary() : this(INITIAL_CAPACITY)
  {
  }

  private static int calculateHash(K key)
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

  private int getBucketIdx(int hash)
  {
    return (buckets.Length - 1) & hash;
  }

  private Entry? getBucket(int hash)
  {
    return buckets[getBucketIdx(hash)];
  }

  public V? At(K key)
  {
    int hash = calculateHash(key);
    Entry? e = getBucket(hash);

    while (e != null)
    {
      if (e.Match(hash, key))
      {
        return e.Value;
      }

      e = e.Next;
    }

    return default(V);
  }

  public bool ContainsKey(K key)
  {
    int hash = calculateHash(key);
    Entry? e = getBucket(hash);

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

  public void AtPut(K key, V value)
  {
    int hash = calculateHash(key);
    int i = getBucketIdx(hash);

    Entry? current = buckets[i];

    if (current == null)
    {
      buckets[i] = NewEntry(key, value, hash);
      size += 1;
    }
    else
    {
      insertBucketEntry(key, value, hash, current);
    }

    if (size > buckets.Length)
    {
      resize();
    }
  }

  protected virtual Entry NewEntry(K key, V value, int hash)
  {
    return new Entry(hash, key, value, null);
  }

  private void insertBucketEntry(K key, V value, int hash, Entry head)
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

  private void resize()
  {
    Entry?[] oldStorage = buckets;

    Entry?[] newStorage = new Entry?[oldStorage.Length * 2];
    buckets = newStorage;
    transferEntries(oldStorage);
  }

  private void transferEntries(Entry?[] oldStorage)
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
          splitBucket(oldStorage, i, current);
        }
      }
    }
  }

  private void splitBucket(Entry?[] oldStorage, int i, Entry? head)
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

  public void removeAll()
  {
    buckets = new Entry[buckets.Length];
    size = 0;
  }

  public Vector<K> getKeys()
  {
    Vector<K> keys = new Vector<K>(size);
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

  public Vector<V> GetValues()
  {
    Vector<V> values = new Vector<V>(size);
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

  public IdentityDictionary() : base(INITIAL_CAPACITY)
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