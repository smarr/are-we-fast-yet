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
 *
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

import java.util.Random;

public class CilkSort {
	public static final int KILO = 1024;
	public static final int MERGESIZE = (2 * KILO);
	public static final int QUICKSIZE = (2 * KILO);
	public static int partition(int left, int right) {
		int i = left;
		int j = right;
		int tmpx;
		int pivot = array[(left + right) / 2];
		while(i <= j){
			while(array[i] < pivot)
				i++;
			while(array[j] > pivot)
				j--;
			if(i <= j) {
				tmpx = array[i];
				array[i] = array[j];
				array[j] = tmpx;
				i++;
				j--;
			}
		}
		return i;
	}

	public static void quicksort(int left, int right) {
		final int index = partition(left, right);
		if(left < index - 1) 
			quicksort(left, index - 1);
		if(index < right) 
			quicksort(index, right);
	}

	public static void seqmerge(int low1, int high1, int low2, int high2, int lowdest, int[] src, int[] dest) {
		int a1;
		int a2;
		if(low1 < high1 && low2 < high2) {
			a1 = src[low1];
			a2 = src[low2];
			for(; true; ) {
				if(a1 < a2) {
					dest[lowdest++] = a1;
					a1 = src[++low1];
					if(low1 >= high1) 
						break ;
				}
				else {
					dest[lowdest++] = a2;
					a2 = dest[++low2];
					if(low2 >= high2) 
						break ;
				}
			}
		}
		if(low1 <= high1 && low2 <= high2) {
			a1 = src[low1];
			a2 = src[low2];
			for(; true; ) {
				if(a1 < a2) {
					dest[lowdest++] = a1;
					++low1;
					if(low1 > high1) 
						break ;
					a1 = src[low1];
				}
				else {
					dest[lowdest++] = a2;
					++low2;
					if(low2 > high2) 
						break ;
					a2 = src[low2];
				}
			}
		}
		if(low1 > high1) {
			System.arraycopy(src, low2, dest, lowdest, (high2 - low2 + 1));
		}
		else {
			System.arraycopy(src, low1, dest, lowdest, (high1 - low1 + 1));
		}
	}

	public static int binsplit(int val, int low, int high, int[] src) {
		int mid;
		while(low != high){
			mid = low + ((high - low + 1) >> 1);
			if(val <= src[mid]) 
				high = mid - 1;
			else 
				low = mid;
		}
		if(src[low] > val) 
			return low - 1;
		else 
			return low;
	}

	public static void cilkmerge(int low1, int high1, int low2, int high2, int lowdest, int[] src, int[] dest) {
		int split1;
		int split2;
		int lowsize;
		if(high2 - low2 > high1 - low1) {
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
		if(high1 < low1) {
			System.arraycopy(src, low2, dest, lowdest, (high2 - low2));
			return ;
		}
		if(high2 - low2 < MERGESIZE) {
			seqmerge(low1, high1, low2, high2, lowdest, dest, src);
			return ;
		}
		split1 = ((high1 - low1 + 1) / 2) + low1;
		split2 = binsplit(split1, low2, high2, src);
		lowsize = split1 - low1 + split2 - low2;
		dest[(lowdest + lowsize + 1)] = src[split1];

		finish {
			async {
				cilkmerge(low1, split1 - 1, low2, split2, lowdest, src, dest);
				cilkmerge(split1 + 1, high1, split2 + 1, high2, lowdest + lowsize + 2, src, dest);
			}
		}
	}

	public static void cilksort(int low, int tmpx, int size) {
		int quarter = size / 4;
		int A;
		int B;
		int C;
		int D;
		int tmpA;
		int tmpB;
		int tmpC;
		int tmpD;
		if(size < QUICKSIZE) {
			quicksort(low, low + size - 1);
			return ;
		}
		A = low;
		tmpA = tmpx;
		B = A + quarter;
		tmpB = tmpA + quarter;
		C = B + quarter;
		tmpC = tmpB + quarter;
		D = C + quarter;
		tmpD = tmpC + quarter;

		finish {
			async {
				cilksort(A, tmpA, quarter);
				cilksort(B, tmpB, quarter);
				cilksort(C, tmpC, quarter);
				cilksort(D, tmpD, size - 3 * quarter);
			}
		}

		finish {
			async {
				cilkmerge(A, A + quarter - 1, B, B + quarter - 1, tmpA, array, tmp);
				cilkmerge(C, C + quarter - 1, D, low + size - 1, tmpC, array, tmp);
			}
		}

		cilkmerge(tmpA, tmpC - 1, tmpC, tmpA + size - 1, A, tmp, array);
	}

	public static void usage() {
		System.out.println("========================================================================");
		System.out.println("Cilksort is a parallel sorting algorithm, donned \"Multisort\", which");
		System.out.println("is a variant of ordinary mergesort.  Multisort begins by dividing an");
		System.out.println("array of elements in half and sorting each half.  It then merges the");
		System.out.println("two sorted halves back together, but in a divide-and-conquer approach");
		System.out.println("rather than the usual serial merge.");
		System.out.println("========================================================================");
	}

	public static int[] array;
	public static int[] tmp;

	public static void main(String[] args) { 
		boolean check = false;
		int size = 10000000; // 10 million
		
		if(args.length > 0) size = Integer.parseInt(args[0]);
		if(args.length > 1) check = Boolean.parseBoolean(args[1]);
		
		int inner = 5;
		int outter = 3;
		if(args.length > 2) inner = Integer.parseInt(args[2]);
		if(args.length > 3) outter = Integer.parseInt(args[3]);
		
		System.out.println("Size = "+size+" check = "+check);
		
		array = new int[size];
		tmp = new int[size];
		final int[] backup = new int[size];
		Random r = new Random(0);
		for(int i = 0; i < size; i++) {
			backup[i] = r.nextInt(999999);
		}
		usage();

		boolean harnessStarted = false;
		final long start = System.nanoTime();
		for(int i=0;i <outter; i++) {
			if(i+1 == outter) {
				harnessStarted = true;
				org.mmtk.plan.Plan.harnessBegin();
				org.jikesrvm.scheduler.RVMThread.perfEventStart();
			}
			for(int j=0; j<inner; j++) {
				System.out.println("========================== ITERATION ("+i+"."+j+") ==================================");
				System.arraycopy(backup, 0, array, 0, size); // restore the old array
				final long startTime = System.currentTimeMillis();
				cilksort(0, 0, size);
				final long time = System.currentTimeMillis() - startTime;
				final double secs = ((double)time) / 1000.0D;
				if(harnessStarted) {
					if(size == 10000000) {
						if(org.jikesrvm.scheduler.WS.wsTotalPush() != 34105) {
							System.out.println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
							System.exit(-1);
						}
					}
				}
				if(check) {
					int a = 0;
					int b;
					boolean ok = true;
					for(int k = 0; k < size; k++) {
						b = array[k];
						ok &= (a <= b);
						a = b;
					}
					if(ok) {
						System.out.println("CilkSort (" + size + "): passed. Time = " + secs);
					}
					else {
						System.out.println("CilkSort (" + size + "): failed. Time = " + secs);
					}
				}
				else {
					System.out.println("Time = " + secs);
				}
				org.jikesrvm.scheduler.WS.dumpWSStatistics();
			}
		}

		System.out.println("Test Kernel under harness passed successfully....");

		org.jikesrvm.scheduler.RVMThread.perfEventStop();
		org.mmtk.plan.Plan.harnessEnd();

		final double duration = (((double)(System.nanoTime() - start))/((double)(1.0E9))) * 1000;
		System.out.printf("===== Test PASSED in %d msec =====\n",(int)duration);
	}
}
