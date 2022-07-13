namespace Benchmarks;

public class Queens : Benchmark
{
  private bool[] freeMaxs;
  private bool[] freeRows;
  private bool[] freeMins;
  private int[] queenRows;

  public override object Execute()
  {
    bool result = true;
    for (int i = 0; i < 10; i++)
    {
      result = result && queens();
    }
    return result;
  }

  private bool queens()
  {
    freeRows = new bool[8]; Array.Fill(freeRows, true);
    freeMaxs = new bool[16]; Array.Fill(freeMaxs, true);
    freeMins = new bool[16]; Array.Fill(freeMins, true);
    queenRows = new int[8]; Array.Fill(queenRows, -1);

    return PlaceQueen(0);
  }

  bool PlaceQueen(int c)
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

  bool GetRowColumn(int r, int c)
  {
    return freeRows[r] && freeMaxs[c + r] && freeMins[c - r + 7];
  }

  void SetRowColumn(int r, int c, bool v)
  {
    freeRows[r] = v;
    freeMaxs[c + r] = v;
    freeMins[c - r + 7] = v;
  }

  public override bool VerifyResult(object result)
  {
    return (bool)result;
  }
}