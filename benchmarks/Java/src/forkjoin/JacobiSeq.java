package forkjoin;

import java.util.concurrent.RecursiveTask;

import som.Benchmark;

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

// Fully sequential version, replacing all forks with compute.

/*
 * Vivek Kumar: Ported to JavaTC work-asyncing.
 */
public final class JacobiSeq extends Benchmark {

  private static final int      DEFAULT_GRANULARITY = 2;
  private static final int      STEPS               = 10;
  private static final double   EPSILON             = 0.0001d;

  @Override
  public boolean innerBenchmarkLoop(final int n) {
    int dim = n + 2;
    double[][] a = new double[dim][dim];
    double[][] b = new double[dim][dim];

    for (int i = 1; i < dim - 1; ++i) {
      for (int j = 1; j < dim - 1; ++j) {
        a[i][j] = EPSILON;
      }
    }

    for (int k = 0; k < dim; ++k) {
      a[k][0] = 1.0d;
      a[k][n + 1] = 1.0d;
      a[0][k] = 1.0d;
      a[n + 1][k] = 1.0d;
      b[k][0] = 1.0d;
      b[k][n + 1] = 1.0d;
      b[0][k] = 1.0d;
      b[n + 1][k] = 1.0d;
    }

    double df = 0.0d;
    for (int x = 0; x < STEPS; ++x) {
      df = new BuildNode(a, b, 1, n, 1, n, DEFAULT_GRANULARITY, x).compute();
    }

    return verifyResult(df, n);
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }

  private static boolean verifyResult(final double df, final int n) {
    // original benchmark does repeated iterations on the same data
    // we don't do that to have more predictable behavior
    return df == 0.03532437858581544d;
  }

  private static class BuildNode extends RecursiveTask<Double> {
    private static final long serialVersionUID = -8076979977697518646L;

    private final double[][] a;
    private final double[][] b;
    private final int        lr;
    private final int        hr;
    private final int        lc;
    private final int        hc;
    private final int        leafs;
    private final int        steps;

    BuildNode(final double[][] a, final double[][] b, final int lr,
        final int hr, final int lc, final int hc, final int leafs,
        final int steps) {
      this.a = a;
      this.b = b;
      this.lr = lr;
      this.hr = hr;
      this.lc = lc;
      this.hc = hc;
      this.leafs = leafs;
      this.steps = steps;
    }

    @Override
    protected Double compute() {
      int rows = hr - lr + 1;
      int cols = hc - lc + 1;
      int mr = (lr + hr) >>> 1;
      int mc = (lc + hc) >>> 1;
      int hrows = mr - lr + 1;
      int hcols = mc - lc + 1;

      if (rows * cols <= leafs) {
        return processLeafNode(a, b, lr, hr, lc, hc, steps);
      } else if (hrows * hcols >= leafs) {
        double df1 = new BuildNode(a, b, lr, mr, lc, mc, leafs, steps).compute();
        double df2 = new BuildNode(a, b, lr, mr, mc + 1, hc, leafs, steps).compute();
        double df3 = new BuildNode(a, b, mr + 1, hr, lc, mc, leafs, steps).compute();
        double df4 = new BuildNode(a, b, mr + 1, hr, mc + 1, hc, leafs, steps).compute();

        double max12 = df1 > df2 ? df1 : df2;
        double max123 = max12 > df3 ? max12 : df3;
        return max123 > df4 ? max123 : df4;
      } else if (cols >= rows) {
        double df1 = new BuildNode(a, b, lr, hr, lc, mc, leafs, steps).compute();
        double df2 = new BuildNode(a, b, lr, hr, mc + 1, hc, leafs, steps).compute();

        return df1 > df2 ? df1 : df2;
      } else {
        double df1 = new BuildNode(a, b, lr, mr, lc, hc, leafs, steps).compute();
        double df2 = new BuildNode(a, b, mr + 1, hr, lc, hc, leafs, steps).compute();

        return df1 > df2 ? df1 : df2;
      }
    }
  }

  private static double processLeafNode(final double[][] A, final double[][] B,
      final int loRow, final int hiRow, final int loCol, final int hiCol,
      final int steps) {
    boolean AtoB = (steps & 1) == 0;
    double[][] a = AtoB ? A : B;
    double[][] b = AtoB ? B : A;

    double md = 0.0d;

    for (int i = loRow; i <= hiRow; ++i) {
      for (int j = loCol; j <= hiCol; ++j) {
        double v = 0.25d
            * (a[i - 1][j] + a[i][j - 1] +
               a[i + 1][j] + a[i][j + 1]);
        b[i][j] = v;

        double diff = v - a[i][j];
        if (diff < 0) {
          diff = -diff;
        }

        if (diff > md) {
          md = diff;
        }
      }
    }
    return md;
  }
}
