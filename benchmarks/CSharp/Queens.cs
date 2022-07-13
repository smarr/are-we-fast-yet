using System;

public class Queens : Benchmark
{
    private bool[] freeMaxs;
    private bool[] freeRows;
    private bool[] freeMins;
    private int[]  queenRows;

    public static void Main(string[] args){
        Queens s = new Queens();
        Object result = s.benchmark();
        bool rr = s.verifyResult(result);
    }

    public override Object benchmark() {
        bool result = true;
        for(int i=0; i<10; i++){
            result = result && queens();
        }
        return result;
    }

    private bool queens() {
        freeRows = new bool[ 8]; Array.Fill(freeRows, true);
        freeMaxs = new bool[16]; Array.Fill(freeMaxs, true);
        freeMins = new bool[16]; Array.Fill(freeMins, true);
        queenRows = new int[ 8]; Array.Fill(queenRows, -1);

        return placeQueen(0);
    }

    bool placeQueen(int c) {
        for(int r=0; r<8;r++){
            if(getRowColumn(r, c)){
                queenRows[r] = c;
                setRowColumn(r, c, false);

                if(c==7){
                    return true;
                }

                if(placeQueen(c+1)){
                    return true;
                }
                setRowColumn(r,c,true);
            }
        }
        return false;
    }

    bool getRowColumn(int r, int c){
        return freeRows[r] && freeMaxs[c + r] && freeMins[c - r + 7];
    }

    void setRowColumn(int r, int c, bool v) {
        freeRows[r        ] = v;
        freeMaxs[c + r    ] = v;
        freeMins[c - r + 7] = v;
    }

    public override bool verifyResult(Object result) {
    return (bool) result;
  }
}