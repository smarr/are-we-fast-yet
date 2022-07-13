namespace Benchmarks;

public class TowersDisk
{
  public readonly int Size;
  public TowersDisk? Next;

  public TowersDisk(int size)
  {
    this.Size = size;
    this.Next = null;
  }
}

public class Towers : Benchmark
{


  public TowersDisk?[] piles;
  public int movesDone;

  private void pushDisk(TowersDisk disk, int pile)
  {
    TowersDisk? top = piles[pile];
    if (!(top == null) && (disk.Size >= top.Size))
    {
      throw new InvalidOperationException("Cannot put a big disk on a smaller one");
    }

    disk.Next = top;
    piles[pile] = disk;
  }

  private TowersDisk popDiskFrom(int pile)
  {
    TowersDisk? top = piles[pile];
    if (top == null)
    {
      throw new InvalidOperationException("Attempting to remove a disk from an empty pile");
    }

    piles[pile] = top.Next;
    top.Next = null;
    return top;
  }

  private void moveTopDisk(int fromPile, int toPile)
  {
    pushDisk(popDiskFrom(fromPile), toPile);
    movesDone++;
  }

  private void buildTowerAt(int pile, int disks)
  {
    for (int i = disks; i >= 0; i--)
    {
      pushDisk(new TowersDisk(i), pile);
    }
  }

  private void moveDisks(int disks, int fromPile, int toPile)
  {
    if (disks == 1)
    {
      moveTopDisk(fromPile, toPile);
    }
    else
    {
      int otherPile = (3 - fromPile) - toPile;
      moveDisks(disks - 1, fromPile, otherPile);
      moveTopDisk(fromPile, toPile);
      moveDisks(disks - 1, otherPile, toPile);
    }
  }

  public override object Execute()
  {
    piles = new TowersDisk[3];
    buildTowerAt(0, 13);
    movesDone = 0;
    moveDisks(13, 0, 1);
    return movesDone;
  }

  public override bool VerifyResult(object result)
  {
    return 8191 == (int)result;
  }

}
