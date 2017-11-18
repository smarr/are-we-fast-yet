package forkjoin;

/**
 * Source: From X10 distribution SM: Likely based on
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
public class Integrate {

  public final static double epsilon = 1.0e-9;

  public final static double computeArea(final double left,
      final double right) {
    return recEval(left, (left * left + 1.0) * left, right,
        (right * right + 1.0) * right, 0);
  }

  private final static double recEval(final double l, final double fl,
      final double r, final double fr, final double a) {
		final double h = (r - l) / 2;
		final double hh = h / 2;
		final double c = l + h;
		final double fc = (c*c + 1.0) * c;
		final double al = (fl + fc) * hh;
		final double ar = (fr + fc) * hh;
		final double alr = al + ar;
		if (alr - a < epsilon && a - alr < epsilon) {
      return alr;
    }

		double expr1 = 0.0;
		double expr2 = 0.0;

		finish {
			async {
				expr1 = recEval(c, fc, r, fr, ar);
				expr2 = recEval(l, fl, c, fc, al);
			}
		}
		return expr1 + expr2;
	}

  public static void main(final String[] args) {
    int xMax = 5000;

    if (args.length > 0) {
      xMax = Integer.parseInt(args[0]);
    }

    System.out.println(
        "Integrate: Calculating the area under the curve (Y^2 + 1) x Y from Y=0 to Y="
            + xMax);
    boolean harnessStarted = false;

    int l_start = 1;
    int inner = 5;
    int outter = 3;
    if (args.length > l_start) {
      inner = Integer.parseInt(args[l_start]);
    }
    if (args.length > (l_start + 1)) {
      outter = Integer.parseInt(args[l_start + 1]);
    }

    final long start = System.nanoTime();
    for (int i = 0; i < outter; i++) {
      if (i + 1 == outter) {
        harnessStarted = true;
        org.mmtk.plan.Plan.harnessBegin();
        org.jikesrvm.scheduler.RVMThread.perfEventStart();
      }
      for (int j = 0; j < inner; j++) {
        System.out.println("========================== ITERATION (" + i + "."
            + j + ") ==================================");

        final long startTime = System.currentTimeMillis();
        final double area = computeArea(0, xMax);
        final long time = System.currentTimeMillis() - startTime;
        final double secs = (time) / 1000.0;
        if (harnessStarted) {
          if (xMax == 10000) {
            if (area != 2.50000005E15) {
              System.out
                  .println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
              System.exit(-1);
            }

          } else if (xMax == 5000) {
            if (area != 1.562500125E14) {
              System.out
                  .println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
              System.exit(-1);
            }

          }
        }
        System.out.println("Time = " + secs + " secs");
        org.jikesrvm.scheduler.WS.dumpWSStatistics();
      }
    }

    System.out.println("Test Kernel under harness passed successfully....");

    org.jikesrvm.scheduler.RVMThread.perfEventStop();
    org.mmtk.plan.Plan.harnessEnd();

    final double duration = ((System.nanoTime() - start) / ((1.0E9))) * 1000;
    System.out.printf("===== Test PASSED in %d msec =====\n", (int) duration);
  }
}
