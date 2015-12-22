public final class Towers extends Benchmark {

  private static final class TowersDisk {
    private final int size;
    private TowersDisk next;

    public TowersDisk(final int size) {
      this.size = size;
    }

    public int  getSize() { return size;  }

    public TowersDisk getNext()                 { return next;  }
    public void setNext(final TowersDisk value) { next = value; }
  }

  private TowersDisk[] piles;
  private int movesDone;

  private void pushDisk(final TowersDisk disk, final int pile) {
    TowersDisk top = piles[pile];
    if (!(top == null) && (disk.getSize() >= top.getSize())) {
      throw new RuntimeException("Cannot put a big disk on a smaller one");
    }

    disk.setNext(top);
    piles[pile] = disk;
  }

  private TowersDisk popDiskFrom(final int pile) {
    TowersDisk top = piles[pile];
    if (top == null) {
      throw new RuntimeException("Attempting to remove a disk from an empty pile");
    }

    piles[pile] = top.getNext();
    top.setNext(null);
    return top;
  }

  private void moveTopDisk(final int fromPile, final int toPile) {
    pushDisk(popDiskFrom(fromPile), toPile);
    movesDone++;
  }

  private void buildTowerAt(final int pile, final int disks) {
    for (int i = disks; i >= 0; i--) {
      pushDisk(new TowersDisk(i), pile);
    }
  }

  private void moveDisks(final int disks, final int fromPile, final int toPile) {
    if (disks == 1) {
      moveTopDisk(fromPile, toPile);
    } else {
      int otherPile = (6 - fromPile) - toPile;
      moveDisks(disks - 1, fromPile, otherPile);
      moveTopDisk(fromPile, toPile);
      moveDisks(disks - 1, otherPile, toPile);
    }
  }

  @Override
  public Object benchmark() {
    piles = new TowersDisk[4];
    buildTowerAt(1, 13);
    movesDone = 0;
    moveDisks(13, 1, 2);
    return movesDone;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 8191 == (int) result;
  }
}
