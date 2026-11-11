namespace Benchmarks.Som;

/**
 * Porting notes:
 *  - does not use an explicit array bounds check, because Java already does
 *    that. Don't see a point in doing it twice.
 */
internal class Vector<TE>
{
    private TE[]? _storage;
    private int _firstIdx;
    private int _lastIdx;

    public static Vector<TE> With(TE elem)
    {
        Vector<TE> v = new(1);
        v.Append(elem);
        return v;
    }

    public Vector(int size)
    {
        _storage = size == 0 ? null : new TE[size];
    }

    public Vector()
    {
    }

    public TE? At(int idx)
    {
        if (_storage == null || idx >= _storage.Length)
        {
            return default;
        }

        return _storage[idx];
    }

    public void AtPut(int idx, TE val)
    {
        if (_storage == null)
        {
            _storage = new TE[Math.Max(idx + 1, CollectionConstants.InitialSize)];
        }
        else if (idx >= _storage.Length)
        {
            int newLength = _storage.Length;
            while (newLength <= idx)
            {
                newLength *= 2;
            }

            Array.Resize(ref _storage, newLength);
        }

        _storage[idx] = val;
        if (_lastIdx < idx + 1)
        {
            _lastIdx = idx + 1;
        }
    }

    public void Append(TE elem)
    {
        if (_storage == null)
        {
            _storage = new TE[CollectionConstants.InitialSize];
        }
        else if (_lastIdx >= _storage.Length)
        {
            // Need to expand capacity first
            Array.Resize(ref _storage, 2 * _storage.Length);
        }

        _storage[_lastIdx] = elem;
        _lastIdx++;
    }

    public bool IsEmpty() => _lastIdx == _firstIdx;

    public void ForEach(Action<TE> fn)
    {
        for (int i = _firstIdx; i < _lastIdx; i++)
        {
            fn.Invoke(_storage![i]);
        }
    }

    public bool HasSome(Predicate<TE> fn)
    {
        for (int i = _firstIdx; i < _lastIdx; i++)
        {
            if (fn.Invoke(_storage![i]))
            {
                return true;
            }
        }

        return false;
    }

    public TE? RemoveFirst()
    {
        if (IsEmpty())
        {
            return default;
        }

        _firstIdx++;
        return _storage![_firstIdx - 1];
    }

    public bool Remove(TE obj)
    {
        if (_storage == null || IsEmpty())
        {
            return false;
        }

        TE[] newArray = new TE[Capacity()];
        int newLast = 0;
        bool found = false;

        ForEach(it =>
        {
            if (EqualityComparer<TE>.Default.Equals(it, obj))
            {
                found = true;
            }
            else
            {
                newArray[newLast] = it;
                newLast++;
            }
        });

        _storage = newArray;
        _lastIdx = newLast;
        _firstIdx = 0;
        return found;
    }

    public void RemoveAll()
    {
        _firstIdx = 0;
        _lastIdx = 0;
        if (_storage != null)
        {
            _storage = new TE[_storage.Length];
        }
    }

    public int Size() => _lastIdx - _firstIdx;

    public int Capacity() => _storage?.Length ?? 0;

    public void Sort(Comparison<TE>? c)
    {
        if (Size() > 0)
        {
            Sort(_firstIdx, _lastIdx - 1, c);
        }
    }

    private void Sort(int i, int j, Comparison<TE>? c)
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

        TE di = _storage![i];
        TE dj = _storage[j];

        if (c!.Invoke(di, dj) > 0)
        {
            Swap(_storage, i, j);
            TE temp = di;
            di = dj;
            dj = temp;
        }

        if (n > 2)
        {
            int ij = (i + j) / 2;
            TE dij = _storage[ij];

            if (c.Invoke(di, dij) <= 0)
            {
                if (c.Invoke(dij, dj) > 0)
                {
                    Swap(_storage, j, ij);
                    dij = dj;
                }
            }
            else
            {
                Swap(_storage, i, ij);
                dij = di;
            }

            if (n > 3)
            {
                int k = i;
                int l = j - 1;

                while (true)
                {
                    while (k <= l && c.Invoke(dij, _storage[l]) <= 0)
                    {
                        l -= 1;
                    }

                    k += 1;
                    while (k <= l && c.Invoke(_storage[k], dij) <= 0)
                    {
                        k += 1;
                    }

                    if (k > l)
                    {
                        break;
                    }

                    Swap(_storage, k, l);
                }

                Sort(i, l, c);
                Sort(k, j, c);
            }
        }
    }

    private static void Swap(TE[] storage2, int i, int j) => throw new NotImplementedException();

    private void DefaultSort(int i, int j) => throw new NotImplementedException();
}
