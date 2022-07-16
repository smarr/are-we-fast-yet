namespace Benchmarks;

public sealed class TowersDisk
{
  public int Size { get; }
  public TowersDisk? Next { get; set; }

  public TowersDisk(int size)
  {
    this.Size = size;
    this.Next = null;
  }
}

public class Towers : Benchmark
{
  private TowersDisk?[] piles = Array.Empty<TowersDisk>();
  private int movesDone;

  private void PushDisk(TowersDisk disk, int pile)
  {
    TowersDisk? top = piles[pile];
    if (!(top == null) && (disk.Size >= top.Size))
    {
      throw new InvalidOperationException("Cannot put a big disk on a smaller one");
    }

    disk.Next = top;
    piles[pile] = disk;
  }

  private TowersDisk PopDiskFrom(int pile)
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

  private void MoveTopDisk(int fromPile, int toPile)
  {
    PushDisk(PopDiskFrom(fromPile), toPile);
    movesDone++;
  }

  private void BuildTowerAt(int pile, int disks)
  {
    for (int i = disks; i >= 0; i--)
    {
      PushDisk(new TowersDisk(i), pile);
    }
  }

  private void MoveDisks(int disks, int fromPile, int toPile)
  {
    if (disks == 1)
    {
      MoveTopDisk(fromPile, toPile);
    }
    else
    {
      int otherPile = (3 - fromPile) - toPile;
      MoveDisks(disks - 1, fromPile, otherPile);
      MoveTopDisk(fromPile, toPile);
      MoveDisks(disks - 1, otherPile, toPile);
    }
  }

  public override object Execute()
  {
    piles = new TowersDisk[3];
    BuildTowerAt(0, 13);
    movesDone = 0;
    MoveDisks(13, 0, 1);
    return movesDone;
  }

  public override bool VerifyResult(object result)
  {
    return 8191 == (int) result;
  }
}