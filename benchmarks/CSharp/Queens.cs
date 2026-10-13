namespace Benchmarks;

internal class Queens : Benchmark
{
    private bool[] _freeMaxs = null!;
    private bool[] _freeRows = null!;
    private bool[] _freeMins = null!;
    private int[] _queenRows = null!;

    public override object Execute()
    {
        bool result = true;
        for (int i = 0; i < 10; i++)
        {
            result = result && GenerateQueens();
        }

        return result;
    }

    private bool GenerateQueens()
    {
        _freeRows = new bool[8];
        Array.Fill(_freeRows, true);
        _freeMaxs = new bool[16];
        Array.Fill(_freeMaxs, true);
        _freeMins = new bool[16];
        Array.Fill(_freeMins, true);
        _queenRows = new int[8];
        Array.Fill(_queenRows, -1);

        return PlaceQueen(0);
    }

    private bool PlaceQueen(int c)
    {
        for (int r = 0; r < 8; r++)
        {
            if (GetRowColumn(r, c))
            {
                _queenRows[r] = c;
                SetRowColumn(r, c, false);

                if (c == 7)
                {
                    return true;
                }

                if (PlaceQueen(c + 1))
                {
                    return true;
                }

                SetRowColumn(r, c, true);
            }
        }

        return false;
    }

    private bool GetRowColumn(int r, int c) => _freeRows[r] && _freeMaxs[c + r] && _freeMins[c - r + 7];

    private void SetRowColumn(int r, int c, bool v)
    {
        _freeRows[r] = v;
        _freeMaxs[c + r] = v;
        _freeMins[c - r + 7] = v;
    }

    public override bool VerifyResult(object result) => (bool)result;
}