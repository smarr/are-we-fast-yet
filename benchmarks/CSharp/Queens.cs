namespace Benchmarks;

public class Queens : Benchmark
{
  private bool[] freeMaxs = Array.Empty<bool>();
  private bool[] freeRows = Array.Empty<bool>();
  private bool[] freeMins = Array.Empty<bool>();
  private int[] queenRows = Array.Empty<int>();

  public override object Execute()
  {
    bool result = true;
    for (int i = 0; i < 10; i++)
    {
      result = result && _queens();
    }

    return result;
  }

  private bool _queens()
  {
    freeRows = new bool[8];
    Array.Fill(freeRows, true);
    freeMaxs = new bool[16];
    Array.Fill(freeMaxs, true);
    freeMins = new bool[16];
    Array.Fill(freeMins, true);
    queenRows = new int[8];
    Array.Fill(queenRows, -1);

    return PlaceQueen(0);
  }

  private bool PlaceQueen(int c)
  {
    for (int r = 0; r < 8; r++)
    {
      if (GetRowColumn(r, c))
      {
        queenRows[r] = c;
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

  private bool GetRowColumn(int r, int c)
  {
    return freeRows[r] && freeMaxs[c + r] && freeMins[c - r + 7];
  }

  private void SetRowColumn(int r, int c, bool v)
  {
    freeRows[r] = v;
    freeMaxs[c + r] = v;
    freeMins[c - r + 7] = v;
  }

  public override bool VerifyResult(object result)
  {
    return (bool) result;
  }
}