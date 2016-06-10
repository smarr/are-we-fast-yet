/* This code is based on the SOM class library.
 *
 * Copyright (c) 2001-2016 see AUTHORS.md file
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public final class Towers extends Benchmark {

  private static final class TowersDisk {
    private final int size;
    private TowersDisk next;

    TowersDisk(final int size) {
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
      int otherPile = (3 - fromPile) - toPile;
      moveDisks(disks - 1, fromPile, otherPile);
      moveTopDisk(fromPile, toPile);
      moveDisks(disks - 1, otherPile, toPile);
    }
  }

  @Override
  public Object benchmark() {
    piles = new TowersDisk[3];
    buildTowerAt(0, 13);
    movesDone = 0;
    moveDisks(13, 0, 1);
    return movesDone;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 8191 == (int) result;
  }
}
