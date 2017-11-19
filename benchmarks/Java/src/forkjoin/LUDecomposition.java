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
 *
 */

/*
 * Vivek Kumar: Ported to JavaTC work-asyncing.
 */

public final class LUDecomposition extends Benchmark {

  private final static int BLOCK_SIZE = 16;
  private double[][]       LU         = null;
  private double[][]       LUSave     = null;

  @Override
  public boolean innerBenchmarkLoop(final int size) {
    final int numOfBlocks = size / BLOCK_SIZE;
    allocate(size);

    initialize(size);
    calcLU(0, 0, numOfBlocks);

    return verifyResult(size);
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }

  private void blockLU(final int posBR, final int posBC) {
    for (int k = 0; k < BLOCK_SIZE; k++) {
      for (int i = k + 1; i < BLOCK_SIZE; i++) {
        LU[posBR + i][posBC + k] /= LU[posBR + k][posBC + k];
        double a = LU[posBR + i][posBC + k];
        for (int n = BLOCK_SIZE - 1; n >= (k + 1); n--) {
          LU[posBR + i][posBC + n] -= a * LU[posBR + k][posBC + n];
        }
      }
    }
  }

  private void blockLowerSolve(final int posBR, final int posBC,
      final int posLR, final int posLC) {
    for (int i = 1; i < BLOCK_SIZE; i++) {
      for (int k = 0; k < i; k++) {
        double a = LU[posLR + i][posLC + k];
        for (int n = BLOCK_SIZE - 1; n >= 0; n--) {
          LU[posBR + i][posBC + n] -= a * LU[posBR + k][posBC + n];
        }
      }
    }
  }

  private void blockUpperSolve(final int posBR, final int posBC,
      final int posUR, final int posUC) {
    for (int i = 0; i < BLOCK_SIZE; i++) {
      for (int k = 0; k < BLOCK_SIZE; k++) {
        LU[posBR + i][posBC + k] /= LU[posUR + k][posUC + k];
        double a = LU[posBR + i][posBC + k];
        for (int n = BLOCK_SIZE - 1; n >= (k + 1); n--) {
          LU[posBR + i][posBC + n] -= a * LU[posUR + k][posUC + n];
        }
      }
    }
  }

  private void blockSchur(final int posBR, final int posBC, final int posAR,
      final int posAC, final int posCR, final int posCC) {
    for (int i = 0; i < BLOCK_SIZE; i++) {
      for (int k = 0; k < BLOCK_SIZE; k++) {
        double a = LU[posAR + i][posAC + k];
        for (int n = BLOCK_SIZE - 1; n >= 0; n--) {
          LU[posBR + i][posBC + n] -= a * LU[posCR + k][posCC + n];
        }
      }
    }
  }

  private static class Schur extends RecursiveAction {
    private static final long serialVersionUID = -5406539330478469821L;

    private final int posMR;
    private final int posMC;
    private final int posVR;
    private final int posVC;
    private final int posWR;
    private final int posWC;
    private final int numOfBlocks;
    private final LUDecomposition lu;

    Schur(final int posMR, final int posMC, final int posVR, final int posVC,
        final int posWR, final int posWC, final int numOfBlocks,
        final LUDecomposition lu) {
      this.posMR = posMR;
      this.posMC = posMC;
      this.posVR = posVR;
      this.posVC = posVC;
      this.posWR = posWR;
      this.posWC = posWC;
      this.numOfBlocks = numOfBlocks;
      this.lu = lu;
    }

    @Override
    protected void compute() {
      if (numOfBlocks == 1) {
        lu.blockSchur(posMR, posMC, posVR, posVC, posWR, posWC);
        return;
      }

      final int halfNb = numOfBlocks / 2;

      final int posM01C = posMC + (halfNb * BLOCK_SIZE);
      final int posM10R = posMR + (halfNb * BLOCK_SIZE);
      final int posM11R = posMR + (halfNb * BLOCK_SIZE);
      final int posM11C = posMC + (halfNb * BLOCK_SIZE);
      final int posV01C = posVC + (halfNb * BLOCK_SIZE);
      final int posV10R = posVR + (halfNb * BLOCK_SIZE);
      final int posV11R = posVR + (halfNb * BLOCK_SIZE);
      final int posV11C = posVC + (halfNb * BLOCK_SIZE);
      final int posW01C = posWC + (halfNb * BLOCK_SIZE);
      final int posW10R = posWR + (halfNb * BLOCK_SIZE);
      final int posW11R = posWR + (halfNb * BLOCK_SIZE);
      final int posW11C = posWC + (halfNb * BLOCK_SIZE);

      Schur a = new Schur(posMR, posMC, posVR, posVC, posWR, posWC, halfNb, lu);
      a.fork();
      Schur b = new Schur(posMR, posM01C, posVR, posVC, posWR, posW01C, halfNb, lu);
      b.fork();
      Schur c = new Schur(posM10R, posMC, posV10R, posVC, posWR, posWC, halfNb, lu);
      c.fork();
      Schur d = new Schur(posM11R, posM11C, posV10R, posVC, posWR, posW01C, halfNb, lu);
      d.fork();

      a.join();
      b.join();
      c.join();
      d.join();

      a = new Schur(posMR, posMC, posVR, posV01C, posW10R, posWC, halfNb, lu);
      a.fork();
      b = new Schur(posMR, posM01C, posVR, posV01C, posW11R, posW11C, halfNb, lu);
      b.fork();
      c = new Schur(posM10R, posMC, posV11R, posV11C, posW10R, posWC, halfNb, lu);
      c.fork();
      d = new Schur(posM11R, posM11C, posV11R, posV11C, posW11R, posW11C, halfNb, lu);
      d.fork();

      a.join();
      b.join();
      c.join();
      d.join();
    }
  }

  private static class LowerSolve extends RecursiveAction {
    private static final long serialVersionUID = 9193323728379724492L;

    private final int posMR;
    private final int posMC;
    private final int posLR;
    private final int posLC;
    private final int numOfBlocks;
    private final LUDecomposition lu;

    LowerSolve(final int posMR, final int posMC, final int posLR,
        final int posLC, final int numOfBlocks, final LUDecomposition lu) {
      this.posMR = posMR;
      this.posMC = posMC;
      this.posLR = posLR;
      this.posLC = posLC;
      this.numOfBlocks = numOfBlocks;
      this.lu = lu;
    }

    @Override
    protected void compute() {
      if (numOfBlocks == 1) {
        lu.blockLowerSolve(posMR, posMC, posLR, posLC);
        return;
      }

      final int halfNb = numOfBlocks / 2;
      final int posM01C = posMC + (halfNb * BLOCK_SIZE);
      final int posM10R = posMR + (halfNb * BLOCK_SIZE);
      final int posM11R = posMR + (halfNb * BLOCK_SIZE);
      final int posM11C = posMC + (halfNb * BLOCK_SIZE);

      AuxLowerSolve a = new AuxLowerSolve(posMR, posMC, posM10R, posMC, posLR,
          posLC, halfNb, lu);
      a.fork();
      AuxLowerSolve b = new AuxLowerSolve(posMR, posM01C, posM11R, posM11C,
          posLR, posLC, halfNb, lu);
      b.fork();

      a.join();
      b.join();
    }
  }

  private static class AuxLowerSolve extends RecursiveAction {
    private static final long serialVersionUID = -2377680468729789947L;

    private final int posMaR;
    private final int posMaC;
    private final int posMbR;
    private final int posMbC;
    private final int posLR;
    private final int posLC;
    private final int numOfBlocks;
    private final LUDecomposition lu;

    AuxLowerSolve(final int posMaR, final int posMaC, final int posMbR,
        final int posMbC, final int posLR, final int posLC,
        final int numOfBlocks, final LUDecomposition lu) {
      this.posMaR = posMaR;
      this.posMaC = posMaC;
      this.posMbR = posMbR;
      this.posMbC = posMbC;
      this.posLR = posLR;
      this.posLC = posLC;
      this.numOfBlocks = numOfBlocks;
      this.lu = lu;
    }

    @Override
    @SuppressWarnings("unused")
    protected void compute() {
      final int posL01C = posLC + (numOfBlocks * BLOCK_SIZE);
      final int posL10R = posLR + (numOfBlocks * BLOCK_SIZE);
      final int posL11R = posLR + (numOfBlocks * BLOCK_SIZE);
      final int posL11C = posLC + (numOfBlocks * BLOCK_SIZE);
      new LowerSolve(posMaR, posMaC, posLR, posLC, numOfBlocks, lu).compute();
      new Schur(posMbR, posMbC, posL10R, posLC, posMaR, posMaC, numOfBlocks, lu).compute();
      new LowerSolve(posMbR, posMbC, posL11R, posL11C, numOfBlocks, lu).compute();
    }
  }

  private static class UpperSolve extends RecursiveAction {
    private static final long serialVersionUID = 2854982837031704951L;

    private final int posMR;
    private final int posMC;
    private final int posUR;
    private final int posUC;
    private final int numOfBlocks;

    private final LUDecomposition lu;

    UpperSolve(final int posMR, final int posMC, final int posUR,
        final int posUC, final int numOfBlocks, final LUDecomposition lu) {
      this.posMR = posMR;
      this.posMC = posMC;
      this.posUR = posUR;
      this.posUC = posUC;
      this.numOfBlocks = numOfBlocks;
      this.lu = lu;
    }

    @Override
    protected void compute() {
      if (numOfBlocks == 1) {
        lu.blockUpperSolve(posMR, posMC, posUR, posUC);
        return;
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

      AuxUpperSolve a = new AuxUpperSolve(posM00R, posM00C, posM01R, posM01C,
          posUR, posUC, halfNb, lu);
      a.fork();
      AuxUpperSolve b = new AuxUpperSolve(posM10R, posM10C, posM11R, posM11C,
          posUR, posUC, halfNb, lu);
      b.fork();

      a.join();
      b.join();
    }
  }

  private static class AuxUpperSolve extends RecursiveAction {
    private static final long serialVersionUID = -7206748103817248871L;

    private final int posMaR;
    private final int posMaC;
    private final int posMbR;
    private final int posMbC;
    private final int posUR;
    private final int posUC;
    private final int numOfBlocks;
    private final LUDecomposition lu;

    AuxUpperSolve(final int posMaR, final int posMaC, final int posMbR,
        final int posMbC, final int posUR, final int posUC,
        final int numOfBlocks, final LUDecomposition lu) {
      this.posMaR = posMaR;
      this.posMaC = posMaC;
      this.posMbR = posMbR;
      this.posMbC = posMbC;
      this.posUR = posUR;
      this.posUC = posUC;
      this.numOfBlocks = numOfBlocks;
      this.lu = lu;
    }

    @Override
    @SuppressWarnings("unused")
    protected void compute() {
      final int posU00R = posUR;
      final int posU00C = posUC;
      final int posU01R = posUR;
      final int posU01C = posUC + (numOfBlocks * BLOCK_SIZE);
      final int posU10R = posUR + (numOfBlocks * BLOCK_SIZE);
      final int posU10C = posUC;
      final int posU11R = posUR + (numOfBlocks * BLOCK_SIZE);
      final int posU11C = posUC + (numOfBlocks * BLOCK_SIZE);
      new UpperSolve(posMaR, posMaC, posU00R, posU00C, numOfBlocks, lu).compute();
      new Schur(posMbR, posMbC, posMaR, posMaC, posU01R, posU01C, numOfBlocks, lu).compute();
      new UpperSolve(posMbR, posMbC, posU11R, posU11C, numOfBlocks, lu).compute();
    }
  }

  private void calcLU(final int posR, final int posC, final int numOfBlocks) {
    if (numOfBlocks == 1) {
      blockLU(posR, posC);
      return;
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

    LowerSolve a = new LowerSolve(pos01R, pos01C, pos00R, pos00C, halfNb, this);
    a.fork();
    UpperSolve b = new UpperSolve(pos10R, pos10C, pos00R, pos00C, halfNb, this);
    b.fork();

    a.join();
    b.join();

    new Schur(pos11R, pos11C, pos10R, pos10C, pos01R, pos01C, halfNb, this).compute();
    calcLU(pos11R, pos11C, halfNb);
  }

  private void allocate(final int n) {
    Random r = new Random();

    LU = new double[n][n];
    LUSave = new double[n][n];

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        LUSave[i][j] = r.next() / 65535.0d;
      }
    }

    for (int k = 0; k < n; ++k) {
      LUSave[k][k] *= 10.0d;
    }
  }

  private void initialize(final int n) {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        LU[i][j] = LUSave[i][j];
      }
    }
  }

  private boolean verifyResult(final int size) {
    double maxDiff = 0.0d;

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        double v = 0.0d;
        int k;
        for (k = 0; k < i && k <= j; k++) {
          v += LU[i][k] * LU[k][j];
        }

        if (k == i && k <= j) {
          v += LU[k][j];
        }

        double diff = Math.abs(LUSave[i][j] - v);
        if (diff > maxDiff) {
          maxDiff = diff;
        }
      }
    }

    return maxDiff <= 0.00001d;
  }
}
