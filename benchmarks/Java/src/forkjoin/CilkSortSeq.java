package forkjoin;

import java.util.concurrent.RecursiveAction;

import som.Benchmark;
import som.Random;

/*
 * Copyright (c) 2000 Massachusetts Institute of Technology
 * Copyright (c) 2000 Matteo Frigo
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * aint with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/*
 * this program uses an algorithm that we call `cilksort'.
 * The algorithm is essentially mergesort:
 *
 *   cilksort(in[1..n]) =
 *       async cilksort(in[1..n/2], tmp[1..n/2])
 *       async cilksort(in[n/2..n], tmp[n/2..n])
 *       sync
 *       async cilkmerge(tmp[1..n/2], tmp[n/2..n], in[1..n])
 *
 *
 * The procedure cilkmerge does the following:
 *
 *       cilkmerge(A[1..n], B[1..m], C[1..(n+m)]) =
 *          find the median of A union B using binary
 *          search.  The binary search gives a pair
 *          (ma, mb) such that ma + mb = (n + m)/2
 *          and all elements in A[1..ma] are smaller than
 *          B[mb..m], and all the B[1..mb] are smaller
 *          than all elements in A[ma..n].
 *
 *          spawn cilkmerge(A[1..ma], B[1..mb], C[1..(n+m)/2])
 *          spawn cilkmerge(A[ma..m], B[mb..n], C[(n+m)/2 .. (n+m)])
 *          sync
 *
 * The algorithm appears for the first time (AFAIK) in S. G. Akl and
 * N. Santoro, "Optimal Parallel Merging and Sorting Without Memory
 * Conflicts", IEEE Trans. Comp., Vol. C-36 No. 11, Nov. 1987 .  The
 * paper does not express the algorithm using recursion, but the
 * idea of finding the median is there.
 *
 * For cilksort of n elements, T_1 = O(n log n) and
 * T_\infty = O(log^3 n).  There is a way to shave a
 * log factor in the critical path (left as homework).
 */

/*
 * Vivek Kumar: Ported to JavaTC work-asyncing.
 */

// Fully sequential version, replacing all forks with compute.

/**
 * Cilksort is a parallel sorting algorithm, donned "Multisort", which is a
 * variant of ordinary mergesort. Multisort begins by dividing an array of
 * elements in half and sorting each half. It then merges the two sorted halves
 * back together, but in a divide-and-conquer approach rather than the usual
 * serial merge.
 */
public final class CilkSortSeq extends Benchmark {

  private static final int KILO      = 1024;
  private static final int MERGESIZE = (2 * KILO);
  private static final int QUICKSIZE = (2 * KILO);

  @Override
  public Object benchmark() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean innerBenchmarkLoop(final int n) {
    int size = n * 10_000;
    int[] array = new int[size];
    int[] tmp = new int[size];

    Random r = new Random();
    for (int i = 0; i < size; i++) {
      array[i] = r.next();
    }

    new Sort(array, tmp, 0, 0, size).compute();
    return verifyResult(array);
  }

  @Override
  public boolean verifyResult(final Object r) {
    throw new UnsupportedOperationException();
  }

  private boolean verifyResult(final int[] array) {
    int a = 0;
    int b;
    boolean ok = true;

    for (int k = 0; k < array.length; k++) {
      b = array[k];
      ok &= a <= b;
      a = b;
    }

    return ok;
  }

  private static void seqmerge(int low1, final int high1, int low2,
      final int high2, int lowdest, final int[] src, final int[] dest) {
    int a1;
    int a2;
    if (low1 < high1 && low2 < high2) {
      a1 = src[low1];
      a2 = src[low2];

      for (;;) {
        if (a1 < a2) {
          dest[lowdest++] = a1;
          a1 = src[++low1];
          if (low1 >= high1) {
            break;
          }
        } else {
          dest[lowdest++] = a2;
          a2 = dest[++low2];
          if (low2 >= high2) {
            break;
          }
        }
      }
    }

    if (low1 <= high1 && low2 <= high2) {
      a1 = src[low1];
      a2 = src[low2];

      for (;;) {
        if (a1 < a2) {
          dest[lowdest++] = a1;
          ++low1;
          if (low1 > high1) {
            break;
          }
          a1 = src[low1];
        } else {
          dest[lowdest++] = a2;
          ++low2;
          if (low2 > high2) {
            break;
          }
          a2 = src[low2];
        }
      }
    }

    if (low1 > high1) {
      System.arraycopy(src, low2, dest, lowdest, (high2 - low2 + 1));
    } else {
      System.arraycopy(src, low1, dest, lowdest, (high1 - low1 + 1));
    }
  }

  private static int binsplit(final int val, int low, int high,
      final int[] src) {
    int mid;
    while (low != high) {
      mid = low + ((high - low + 1) >> 1);
      if (val <= src[mid]) {
        high = mid - 1;
      } else {
        low = mid;
      }
    }

    if (src[low] > val) {
      return low - 1;
    } else {
      return low;
    }
  }

  private static class Sort extends RecursiveAction {

    private static final long serialVersionUID = 4210346684339668520L;

    private final int low;
    private final int tmpx;
    private final int size;

    private final int[] array;
    private final int[] tmp;

    Sort(final int[] array, final int[] tmp, final int low, final int tmpx,
        final int size) {
      this.low = low;
      this.tmpx = tmpx;
      this.size = size;

      this.array = array;
      this.tmp = tmp;
    }

    @Override
    protected void compute() {
      int quarter = size / 4;

      if (size < QUICKSIZE) {
        quicksort(low, low + size - 1);
        return;
      }

      int A = low;
      int tmpA = tmpx;
      int B = A + quarter;
      int tmpB = tmpA + quarter;
      int C = B + quarter;
      int tmpC = tmpB + quarter;
      int D = C + quarter;
      int tmpD = tmpC + quarter;

      Sort taskA = new Sort(array, tmp, A, tmpA, quarter);
      taskA.compute();
      Sort taskB = new Sort(array, tmp, B, tmpB, quarter);
      taskB.compute();
      Sort taskC = new Sort(array, tmp, C, tmpC, quarter);
      taskC.compute();
      Sort taskD = new Sort(array, tmp, D, tmpD, size - 3 * quarter);
      taskD.compute();

      Merge mergeA = new Merge(A, A + quarter - 1, B, B + quarter - 1, tmpA,
          array, tmp);
      mergeA.compute();
      Merge mergeB = new Merge(C, C + quarter - 1, D, low + size - 1, tmpC,
          array, tmp);
      mergeB.compute();

      new Merge(tmpA, tmpC - 1, tmpC, tmpA + size - 1, A, tmp, array).compute();
    }

    private int partition(final int left, final int right) {
      int i = left;
      int j = right;
      int tmpx;
      int pivot = array[(left + right) / 2];
      while (i <= j) {
        while (array[i] < pivot) {
          i++;
        }
        while (array[j] > pivot) {
          j--;
        }
        if (i <= j) {
          tmpx = array[i];
          array[i] = array[j];
          array[j] = tmpx;
          i++;
          j--;
        }
      }
      return i;
    }

    private void quicksort(final int left, final int right) {
      final int index = partition(left, right);
      if (left < index - 1) {
        quicksort(left, index - 1);
      }

      if (index < right) {
        quicksort(index, right);
      }
    }
  }

  private static class Merge extends RecursiveAction {

    private static final long serialVersionUID = -6933250566167176280L;

    private int         low1;
    private int         high1;
    private int         low2;
    private int         high2;
    private final int   lowdest;
    private final int[] src;
    private final int[] dest;

    Merge(final int low1, final int high1, final int low2, final int high2,
        final int lowdest, final int[] src, final int[] dest) {
      this.low1 = low1;
      this.high1 = high1;
      this.low2 = low2;
      this.high2 = high2;
      this.lowdest = lowdest;
      this.src = src;
      this.dest = dest;
    }

    @Override
    protected void compute() {
      int split1;
      int split2;
      int lowsize;

      if (high2 - low2 > high1 - low1) {
        {
          final int tmp = low1;
          low1 = low2;
          low2 = tmp;
        }
        {
          final int tmp = high1;
          high1 = high2;
          high2 = tmp;
        }
      }

      if (high1 < low1) {
        System.arraycopy(src, low2, dest, lowdest, (high2 - low2));
        return;
      }

      if (high2 - low2 < MERGESIZE) {
        seqmerge(low1, high1, low2, high2, lowdest, dest, src);
        return;
      }

      split1 = ((high1 - low1 + 1) / 2) + low1;
      split2 = binsplit(split1, low2, high2, src);
      lowsize = split1 - low1 + split2 - low2;
      dest[(lowdest + lowsize + 1)] = src[split1];

      Merge mergeA = new Merge(low1, split1 - 1, low2, split2, lowdest, src,
          dest);
      mergeA.compute();

      Merge mergeB = new Merge(split1 + 1, high1, split2 + 1, high2,
          lowdest + lowsize + 2, src, dest);
      mergeB.compute();
    }
  }
}
