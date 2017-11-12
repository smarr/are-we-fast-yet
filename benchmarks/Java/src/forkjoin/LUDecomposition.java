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
 * Vivek Kumar: Ported to JavaTC work-asyncing.  
 */

public class LUDecomposition {
	private static int size = 1024;
	private static int BLOCK_SIZE = 16;
	private static int DEFAULT_SIZE = 1024;
	private static double[][] LU = null;
	private static double[][] LUSave = null;

	private static void blockLU(final int posBR, final int posBC) {
		double a;
		int i;
		int k;
		int n;
		for(k = 0; k < BLOCK_SIZE; k++) {
			for(i = k + 1; i < BLOCK_SIZE; i++) {
				LU[posBR + i][posBC + k] /= LU[posBR + k][posBC + k];
				a = LU[posBR + i][posBC + k];
				for(n = BLOCK_SIZE - 1; n >= (k + 1); n--) {
					LU[posBR + i][posBC + n] -= a * LU[posBR + k][posBC + n];
				}
			}
		}
	}

	private static void blockLowerSolve(final int posBR, final int posBC, final int posLR, final int posLC) {
		double a;
		int i;
		int k;
		int n;
		for(i = 1; i < BLOCK_SIZE; i++) {
			for(k = 0; k < i; k++) {
				a = LU[posLR + i][posLC + k];
				for(n = BLOCK_SIZE - 1; n >= 0; n--) {
					LU[posBR + i][posBC + n] -= a * LU[posBR + k][posBC + n];
				}
			}
		}
	}

	private static void blockUpperSolve(final int posBR, final int posBC, final int posUR, final int posUC) {
		double a;
		int i;
		int k;
		int n;
		for(i = 0; i < BLOCK_SIZE; i++) {
			for(k = 0; k < BLOCK_SIZE; k++) {
				LU[posBR + i][posBC + k] /= LU[posUR + k][posUC + k];
				a = LU[posBR + i][posBC + k];
				for(n = BLOCK_SIZE - 1; n >= (k + 1); n--) {
					LU[posBR + i][posBC + n] -= a * LU[posUR + k][posUC + n];
				}
			}
		}
	}

	private static void blockSchur(final int posBR, final int posBC, final int posAR, final int posAC, final int posCR, final int posCC) {
		int i;
		int k;
		int n;
		double a;
		for(i = 0; i < BLOCK_SIZE; i++) {
			for(k = 0; k < BLOCK_SIZE; k++) {
				a = LU[posAR + i][posAC + k];
				for(n = BLOCK_SIZE - 1; n >= 0; n--) {
					LU[posBR + i][posBC + n] -= a * LU[posCR + k][posCC + n];
				}
			}
		}
	}

	public static void schur(final int posMR, final int posMC, final int posVR, final int posVC, final int posWR, final int posWC, final int numOfBlocks) {
		if(numOfBlocks == 1) {
			blockSchur(posMR, posMC, posVR, posVC, posWR, posWC);
			return ;
		}
		final int halfNb = numOfBlocks / 2;
		final int posM00R = posMR;
		final int posM00C = posMC;
		final int posM01R = posMR;
		final int posM01C = posMC + (halfNb * BLOCK_SIZE);
		final int posM10R = posMR + (halfNb * BLOCK_SIZE);
		final int posM10C = posMC;
		final int posM11R = posMR + (halfNb * BLOCK_SIZE);
		final int posM11C = posMC + (halfNb * BLOCK_SIZE);
		final int posV00R = posVR;
		final int posV00C = posVC;
		final int posV01R = posVR;
		final int posV01C = posVC + (halfNb * BLOCK_SIZE);
		final int posV10R = posVR + (halfNb * BLOCK_SIZE);
		final int posV10C = posVC;
		final int posV11R = posVR + (halfNb * BLOCK_SIZE);
		final int posV11C = posVC + (halfNb * BLOCK_SIZE);
		final int posW00R = posWR;
		final int posW00C = posWC;
		final int posW01R = posWR;
		final int posW01C = posWC + (halfNb * BLOCK_SIZE);
		final int posW10R = posWR + (halfNb * BLOCK_SIZE);
		final int posW10C = posWC;
		final int posW11R = posWR + (halfNb * BLOCK_SIZE);
		final int posW11C = posWC + (halfNb * BLOCK_SIZE);
		finish {
			async {
				schur(posM00R, posM00C, posV00R, posV00C, posW00R, posW00C, halfNb);
				schur(posM01R, posM01C, posV00R, posV00C, posW01R, posW01C, halfNb);
				schur(posM10R, posM10C, posV10R, posV10C, posW00R, posW00C, halfNb);
				schur(posM11R, posM11C, posV10R, posV10C, posW01R, posW01C, halfNb);
			}
		}

		finish {
			async {
				schur(posM00R, posM00C, posV01R, posV01C, posW10R, posW10C, halfNb);
				schur(posM01R, posM01C, posV01R, posV01C, posW11R, posW11C, halfNb);
				schur(posM10R, posM10C, posV11R, posV11C, posW10R, posW10C, halfNb);
				schur(posM11R, posM11C, posV11R, posV11C, posW11R, posW11C, halfNb);
			}
		}
	}

	public static void lowerSolve(final int posMR, final int posMC, final int posLR, final int posLC, final int numOfBlocks) {
		if(numOfBlocks == 1) {
			blockLowerSolve(posMR, posMC, posLR, posLC);
			return ;
		}
		final int halfNb = numOfBlocks / 2;
		final int posM00R = posMR;
		final int posM00C = posMC;
		final int posM01R = posMR;
		final int posM01C = posMC + (halfNb * BLOCK_SIZE);
		final int posM10R = posMR + (halfNb * BLOCK_SIZE);
		final int posM10C = posMC;
		final int posM11R = posMR + (halfNb * BLOCK_SIZE);
		final int posM11C = posMC + (halfNb * BLOCK_SIZE);
		finish {
			async {
				auxLowerSolve(posM00R, posM00C, posM10R, posM10C, posLR, posLC, halfNb);
				auxLowerSolve(posM01R, posM01C, posM11R, posM11C, posLR, posLC, halfNb);
			}
		}
	}

	public static void auxLowerSolve(final int posMaR, int posMaC, final int posMbR, final int posMbC, final int posLR, final int posLC, final int numOfBlocks) {
		final int posL00R = posLR;
		final int posL00C = posLC;
		final int posL01R = posLR;
		final int posL01C = posLC + (numOfBlocks * BLOCK_SIZE);
		final int posL10R = posLR + (numOfBlocks * BLOCK_SIZE);
		final int posL10C = posLC;
		final int posL11R = posLR + (numOfBlocks * BLOCK_SIZE);
		final int posL11C = posLC + (numOfBlocks * BLOCK_SIZE);
		lowerSolve(posMaR, posMaC, posL00R, posL00C, numOfBlocks);
		schur(posMbR, posMbC, posL10R, posL10C, posMaR, posMaC, numOfBlocks);
		lowerSolve(posMbR, posMbC, posL11R, posL11C, numOfBlocks);
	}

	public static void upperSolve(final int posMR, final int posMC, final int posUR, final int posUC, final int numOfBlocks) {
		if(numOfBlocks == 1) {
			blockUpperSolve(posMR, posMC, posUR, posUC);
			return ;
		}
		final int halfNb = numOfBlocks / 2;
		final int posM00R = posMR;
		final int posM00C = posMC;
		final int posM01R = posMR;
		final int posM01C = posMC + (halfNb * BLOCK_SIZE);
		final int posM10R = posMR + (halfNb * BLOCK_SIZE);
		final int posM10C = posMC;
		final int posM11R = posMR + (halfNb * BLOCK_SIZE);
		final int posM11C = posMC + (halfNb * BLOCK_SIZE);
		finish {
			async {
				auxUpperSolve(posM00R, posM00C, posM01R, posM01C, posUR, posUC, halfNb);
				auxUpperSolve(posM10R, posM10C, posM11R, posM11C, posUR, posUC, halfNb);
			}
		}
	}

	public static void auxUpperSolve(final int posMaR, final int posMaC, final int posMbR, final int posMbC, final int posUR, final int posUC, final int numOfBlocks) {
		final int posU00R = posUR;
		final int posU00C = posUC;
		final int posU01R = posUR;
		final int posU01C = posUC + (numOfBlocks * BLOCK_SIZE);
		final int posU10R = posUR + (numOfBlocks * BLOCK_SIZE);
		final int posU10C = posUC;
		final int posU11R = posUR + (numOfBlocks * BLOCK_SIZE);
		final int posU11C = posUC + (numOfBlocks * BLOCK_SIZE);
		upperSolve(posMaR, posMaC, posU00R, posU00C, numOfBlocks);
		schur(posMbR, posMbC, posMaR, posMaC, posU01R, posU01C, numOfBlocks);
		upperSolve(posMbR, posMbC, posU11R, posU11C, numOfBlocks);
	}

	public static void calcLU(final int posR, final int posC, final int numOfBlocks) {
		if(numOfBlocks == 1) {
			blockLU(posR, posC);
			return ;
		}
		final int halfNb = numOfBlocks / 2;
		final int pos00R = posR;
		final int pos00C = posC;
		final int pos01R = posR;
		final int pos01C = posC + (halfNb * BLOCK_SIZE);
		final int pos10R = posR + (halfNb * BLOCK_SIZE);
		final int pos10C = posC;
		final int pos11R = posR + (halfNb * BLOCK_SIZE);
		final int pos11C = posC + (halfNb * BLOCK_SIZE);
		calcLU(pos00R, pos00C, halfNb);
		finish {
			async {
				lowerSolve(pos01R, pos01C, pos00R, pos00C, halfNb);
				upperSolve(pos10R, pos10C, pos00R, pos00C, halfNb);
			}
		}
		schur(pos11R, pos11C, pos10R, pos10C, pos01R, pos01C, halfNb);
		calcLU(pos11R, pos11C, halfNb);
	}

	public static void main(String[] args) {
		size = 1024;
		boolean check = false;
		
		if(args.length > 0) size = Integer.parseInt(args[0]);
		if(args.length > 1) check = Boolean.parseBoolean(args[1]);
		
		System.out.println("Size = "+size+"X"+size+" check = "+check);
		final int numOfBlocks = size / BLOCK_SIZE;
		allocate(size);

		boolean harnessStarted = false;
		
		int l_start=2;
		int inner = 5;
		int outter = 3;
		if(args.length > l_start) inner = Integer.parseInt(args[l_start]);
		if(args.length > (l_start+1)) outter = Integer.parseInt(args[l_start+1]);
		
		final long start = System.nanoTime();
		for(int i=0;i <outter; i++) {
			if(i+1 == outter) {
				harnessStarted = true;
				org.mmtk.plan.Plan.harnessBegin();
				org.jikesrvm.scheduler.RVMThread.perfEventStart();
			}
			for(int j=0; j<inner; j++) {
				System.out.println("========================== ITERATION ("+i+"."+j+") ==================================");

				initialize(size);
				final long startTime = System.currentTimeMillis();
				calcLU(0, 0, numOfBlocks);
				final long time = System.currentTimeMillis() - startTime;
				final double secs = ((double)time) / 1000.0D;
				System.out.println("LUDecomposition (" + size + "x" + size + ") Done. Time = " + secs+" secs");
				if(harnessStarted) {
					if(size == 1024 && BLOCK_SIZE == 16) {
						if(org.jikesrvm.scheduler.WS.wsTotalPush() != 72231) {
							System.out.println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
							System.exit(-1);
						}
					}
				}
				org.jikesrvm.scheduler.WS.dumpWSStatistics();
				if(check) verifyResult();
			}
		}

		System.out.println("Test Kernel under harness passed successfully....");
		
		org.jikesrvm.scheduler.RVMThread.perfEventStop();
		org.mmtk.plan.Plan.harnessEnd();

		final double duration = (((double)(System.nanoTime() - start))/((double)(1.0E9))) * 1000;
		System.out.printf("===== Test PASSED in %d msec =====\n",(int)duration);
	}

	public static void allocate(int n) {
		LU = new double[n][n];
		LUSave = new double[n][n];
		for(int i = 0; i < n; i++) 
			for(int j = 0; j < n; j++) 
				LUSave[i][j] = Math.random();
		for(int k = 0; k < n; ++k) 
			LUSave[k][k] *= 10.0D;
	}

	public static void initialize(int n) {
		for(int i = 0; i < n; i++) 
			for(int j = 0; j < n; j++) 
				LU[i][j] = LUSave[i][j];
	}

	public static boolean verifyResult() {
		double diff;
		double maxDiff;
		double v;
		int i;
		int j;
		int k;
		maxDiff = 0.0D;
		for(i = 0; i < size; i++) {
			for(j = 0; j < size; j++) {
				v = 0.0D;
				for(k = 0; k < i && k <= j; k++) {
					v += LU[i][k] * LU[k][j];
				}
				if(k == i && k <= j) {
					v += LU[k][j];
				}
				diff = Math.abs(LUSave[i][j] - v);
				if(diff > maxDiff) 
					maxDiff = diff;
			}
		}
		if(maxDiff <= 0.00001D) {
			return true;
		}
		else {
			System.out.println("Bad Result: maxDiff is " + maxDiff);
			return false;
		}
	}
}
