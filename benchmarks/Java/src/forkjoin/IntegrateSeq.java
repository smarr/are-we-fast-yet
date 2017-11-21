package forkjoin;

import java.util.concurrent.RecursiveTask;

import som.Benchmark;

// Fully sequential version, replacing all forks with compute.

/**
 * Source: From X10 distribution
 *
 * SM: Likely based on
 * https://github.com/x10-lang/x10/blob/c336f860467765e6c4e6dd4c1bf07ff3b568e7c8/x10.dist/samples/Integrate.x10
 *
 * Which means, the following likely applies:
 *
 * This file is licensed to You under the Eclipse Public License (EPL); You may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * (C) Copyright IBM Corporation 2006-2016.
 */
public final class IntegrateSeq extends Benchmark {

  private final static double EPSILON = 1.0e-9;

  @Override
  public boolean innerBenchmarkLoop(final int xMax) {
    return verifyResult(computeArea(0, xMax), xMax);
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }

  private static boolean verifyResult(final double area, final int xMax) {
    if (xMax == 10000) { return area == 2.50000005E15; }
    if (xMax == 5000)  { return area == 1.562500125E14; }
    if (xMax == 2500)  { return area == 9.765628125000004E12; }
    if (xMax == 1000)  { return area == 2.50000500000001E11; }

    System.out.println("No expected result for area=" + area);
    return false;
  }

  private static double computeArea(final double left, final double right) {
    return new ComputeArea(left, (left * left + 1.0) * left, right,
        (right * right + 1.0) * right, 0).compute();
  }

  private final static class ComputeArea extends RecursiveTask<Double> {
    private static final long serialVersionUID = 1993482498005149296L;

    private final double l;
    private final double fl;
    private final double r;
    private final double fr;
    private final double a;

    ComputeArea(final double l, final double fl, final double r,
        final double fr, final double a) {
      this.l = l;
      this.fl = fl;
      this.r = r;
      this.fr = fr;
      this.a = a;
    }

    @Override
    protected Double compute() {
      final double h = (r - l) / 2;
      final double hh = h / 2;
      final double c = l + h;
      final double fc = (c*c + 1.0) * c;
      final double al = (fl + fc) * hh;
      final double ar = (fr + fc) * hh;
      final double alr = al + ar;

      if (alr - a < EPSILON && a - alr < EPSILON) {
        return alr;
      }

      double expr1 = new ComputeArea(c, fc, r, fr, ar).compute();
      double expr2 = new ComputeArea(l, fl, c, fc, al).compute();

      return expr1 + expr2;
    }
  }
}
