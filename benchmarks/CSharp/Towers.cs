using System;
using System.Collections;

public class Towers : Benchmark {

    public class TowersDisk {
        public readonly int Size;
        public TowersDisk Next;

        public TowersDisk(int size){
            this.Size = size;
            this.Next = null;
        }
    }

    public static void Main(string[] args){
        Towers s = new Towers();
        Object result = s.benchmark();
        Console.WriteLine(result);
        bool rr = s.verifyResult(result);
        Console.WriteLine(rr);
    }

    public TowersDisk[] piles;
    public int movesDone;

    public void pushDisk(TowersDisk disk, int pile){
        TowersDisk top = piles[pile];
        if(!(top ==null) && (disk.Size >= top.Size)){
            throw new InvalidOperationException("Cannot put a big disk on a smaller one");
        }

        disk.Next = top;
        piles[pile] = disk;
    }

    public TowersDisk popDiskFrom(int pile){
        TowersDisk top = piles[pile];
        if(top == null) {
            throw new InvalidOperationException("Attempting to remove a disk from an empty pile");
        }
        
        piles[pile] = top.Next;
        top.Next = null;
        return top;
    }

    public void moveTopDisk(int fromPile, int toPile){
        pushDisk(popDiskFrom(fromPile), toPile);
        movesDone++;
    }

    public void buildTowerAt(int pile, int disks){
        for(int i = disks; i>=0; i--){
            pushDisk(new TowersDisk(i), pile);
        }
    }

    public void moveDisks(int disks, int fromPile, int toPile){
        if(disks == 1){
            moveTopDisk(fromPile, toPile);
        }
        else{
            int otherPile = (3-fromPile) - toPile;
            moveDisks(disks - 1, fromPile, otherPile);
            moveTopDisk(fromPile, toPile);
            moveDisks(disks - 1, otherPile, toPile);
        }
    }

    public override Object benchmark(){
        piles = new TowersDisk[3];
        buildTowerAt(0,13);
        movesDone = 0;
        moveDisks(13,0,1);
        return movesDone;
    }

    public override bool verifyResult(Object result){
        return 8191 == (int) result;
    }

}
