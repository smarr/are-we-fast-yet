namespace Benchmarks;

internal sealed class Towers : Benchmark
{
    private sealed class TowersDisk
    {
        public int Size { get; }
        public TowersDisk? Next { get; set; }

        public TowersDisk(int size)
        {
            Size = size;
        }
    }

    private TowersDisk?[] _piles = null!;
    private int _movesDone;

    private void PushDisk(TowersDisk disk, int pile)
    {
        TowersDisk? top = _piles[pile];
        if (top != null && disk.Size >= top.Size)
        {
            throw new InvalidOperationException("Cannot put a big disk on a smaller one");
        }

        disk.Next = top;
        _piles[pile] = disk;
    }

    private TowersDisk PopDiskFrom(int pile)
    {
        TowersDisk? top = _piles[pile];
        if (top == null)
        {
            throw new InvalidOperationException("Attempting to remove a disk from an empty pile");
        }

        _piles[pile] = top.Next;
        top.Next = null;
        return top;
    }

    private void MoveTopDisk(int fromPile, int toPile)
    {
        PushDisk(PopDiskFrom(fromPile), toPile);
        _movesDone++;
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
            int otherPile = 3 - fromPile - toPile;
            MoveDisks(disks - 1, fromPile, otherPile);
            MoveTopDisk(fromPile, toPile);
            MoveDisks(disks - 1, otherPile, toPile);
        }
    }

    public override object Execute()
    {
        _piles = new TowersDisk[3];
        BuildTowerAt(0, 13);
        _movesDone = 0;
        MoveDisks(13, 0, 1);
        return _movesDone;
    }

    public override bool VerifyResult(object result) => 8191 == (int)result;
}