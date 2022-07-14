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
  public TowersDisk?[] Piles = Array.Empty<TowersDisk>();
  public int MovesDone;

  private void PushDisk(TowersDisk disk, int pile)
  {
    TowersDisk? top = Piles[pile];
    if (!(top == null) && (disk.Size >= top.Size))
    {
      throw new InvalidOperationException("Cannot put a big disk on a smaller one");
    }

    disk.Next = top;
    Piles[pile] = disk;
  }

  private TowersDisk PopDiskFrom(int pile)
  {
    TowersDisk? top = Piles[pile];
    if (top == null)
    {
      throw new InvalidOperationException("Attempting to remove a disk from an empty pile");
    }

    Piles[pile] = top.Next;
    top.Next = null;
    return top;
  }

  private void MoveTopDisk(int fromPile, int toPile)
  {
    PushDisk(PopDiskFrom(fromPile), toPile);
    MovesDone++;
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
    Piles = new TowersDisk[3];
    BuildTowerAt(0, 13);
    MovesDone = 0;
    MoveDisks(13, 0, 1);
    return MovesDone;
  }

  public override bool VerifyResult(object result)
  {
    return 8191 == (int) result;
  }
}