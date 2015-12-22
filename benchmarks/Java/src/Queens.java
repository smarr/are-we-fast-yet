import java.util.Arrays;


public class Queens extends Benchmark {

  private boolean[] freeMaxs;
  private boolean[] freeRows;
  private boolean[] freeMins;
  private int[]     queenRows;

  @Override
  public Object benchmark() {
    boolean result = true;
    for (int i = 0; i < 10; i++) {
      result = result && queens();
    }
    return result;
  }

  private boolean queens() {
    freeRows  = new boolean[ 8]; Arrays.fill(freeRows, true);
    freeMaxs  = new boolean[16]; Arrays.fill(freeMaxs, true);
    freeMins  = new boolean[16]; Arrays.fill(freeMins, true);
    queenRows = new     int[ 8]; Arrays.fill(queenRows, -1);

    return placeQueen(0);
  }

  boolean placeQueen(final int c) {
    for (int r = 0; r < 8; r++) {
      if (getRowColumn(r, c)) {
        queenRows[r] = c;
        setRowColumn(r, c, false);

        if (c == 7) {
          return true;
        }

        if (placeQueen(c + 1)) {
          return true;
        }
        setRowColumn(r, c, true);
      }
    }
    return false;
  }

  boolean getRowColumn(final int r, final int c) {
    return freeRows[r] && freeMaxs[c + r] && freeMins[c - r + 7];
  }

  void setRowColumn(final int r, final int c, final boolean v) {
    freeRows[r        ] = v;
    freeMaxs[c + r    ] = v;
    freeMins[c - r + 7] = v;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return (boolean) result;
  }
}
