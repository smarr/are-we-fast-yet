/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

/*
 * Vivek Kumar: Ported to JavaTC work-asyncing.  
 */

public class Jacobi {
	final static double[] result_1024_10_2 = {
		0.03532437858581544D,0.017849813902944933D,0.01192949360929535D,
		0.008988350379449561D,0.007195258835605234D,0.005983223755848366D,
		0.005145446751888216D,0.004491799673319474D,0.0040017586597708155D,
		0.0035942981873005597D,0.0032725732667924223D,0.0029970514366798318D,
		0.0027653688264400733D,0.0025724189553933963D,0.002397642724590421D
	};
	static final int DEFAULT_GRANULARITY = 2;
	static final double EPSILON = 0.0001D;
	public static void main(String[] args) { 
		int n = 1024;
		int steps = 10;
		int granularity = DEFAULT_GRANULARITY;
		
		if(args.length > 0) n = Integer.parseInt(args[0]);
		if(args.length > 1) steps = Integer.parseInt(args[1]);
		if(args.length > 2) granularity = Integer.parseInt(args[2]);
		
		System.out.println("Matrix Size = " + n + " Steps = " + steps+" granularity = "+granularity);
				
		int dim = n + 2;
		int ncells = dim * dim;
		double[][] a = new double[dim][dim];
		double[][] b = new double[dim][dim];
		double smallVal = EPSILON;

		for(int i = 1; i < dim - 1; ++i) {
			for(int j = 1; j < dim - 1; ++j) {
				a[i][j] = smallVal;
			}
		}

		for(int k = 0; k < dim; ++k) {
			a[k][0] = 1.0D;
			a[k][n + 1] = 1.0D;
			a[0][k] = 1.0D;
			a[n + 1][k] = 1.0D;
			b[k][0] = 1.0D;
			b[k][n + 1] = 1.0D;
			b[0][k] = 1.0D;
			b[n + 1][k] = 1.0D;
		}

		int l_start=3;
		int inner = 5;
		int outter = 3;
		if(args.length > l_start) inner = Integer.parseInt(args[l_start]);
		if(args.length > (l_start+1)) outter = Integer.parseInt(args[l_start+1]);
		
		boolean harnesStarted = false;
		int iter = 0;
		final long start = System.nanoTime();
		for(int i=0;i <outter; i++) {
			if(i+1 == outter) {
				harnesStarted = true;
				org.mmtk.plan.Plan.harnessBegin();
				org.jikesrvm.scheduler.RVMThread.perfEventStart();
			}
			for(int j=0; j<inner; j++) {
				System.out.println("========================== ITERATION ("+i+"."+j+") ==================================");
				final long startTime = System.currentTimeMillis();
				double df = 0.0D;
				for(int x = 0; x < steps; ++x) {
					df = buildNode(a, b, 1, n, 1, n, granularity, x);
				}
				if(harnesStarted && iter<15) {
					if(n==1024 && steps == 10 && granularity == 2) {
						if(df != result_1024_10_2[iter]) {
							System.out.println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
							System.exit(-1);
						}
					}
				}
				iter++;
				final long time = System.currentTimeMillis() - startTime;
				final double secs = ((double)time) / 1000.0D;
				System.out.println("Jacobi: max diff after " + steps + " steps = " + df + " Time: " + secs);
				org.jikesrvm.scheduler.WS.dumpWSStatistics();
			}
		}
		
		System.out.println("Test Kernel under harness passed successfully....");

		org.jikesrvm.scheduler.RVMThread.perfEventStop();
		org.mmtk.plan.Plan.harnessEnd();

		final double duration = (((double)(System.nanoTime() - start))/((double)(1.0E9))) * 1000;
		System.out.printf("===== Test PASSED in %d msec =====\n",(int)duration);
	}

	public static double buildNode(double[][] a, double[][] b, int lr, int hr, int lc, int hc, int leafs, int steps) {
		int rows = (hr - lr + 1);
		int cols = (hc - lc + 1);
		int mr = (lr + hr) >>> 1;
		int mc = (lc + hc) >>> 1;
		int hrows = (mr - lr + 1);
		int hcols = (mc - lc + 1);
		double df1 = 0;
		double df2 = 0;
		double df3 = 0;
		double df4 = 0;
		if(rows * cols <= leafs) {
			++leafs;
			return processLeafNode(a, b, lr, hr, lc, hc, steps);
		}
		else if(hrows * hcols >= leafs) {
			finish {
				async {
					df1 = buildNode(a, b, lr, mr, lc, mc, leafs, steps);
					df2 = buildNode(a, b, lr, mr, mc + 1, hc, leafs, steps);
					df3 = buildNode(a, b, mr + 1, hr, lc, mc, leafs, steps);
					df4 = buildNode(a, b, mr + 1, hr, mc + 1, hc, leafs, steps);
				}
			}
			return ((((df1 > df2) ? df1 : df2) > df3 ? ((df1 > df2) ? df1 : df2) : df3) > df4) ? (((df1 > df2) ? df1 : df2) > df3 ? ((df1 > df2) ? df1 : df2) : df3) : df4;
		}
		else if(cols >= rows) {
			finish {
				async {
					df1 = buildNode(a, b, lr, hr, lc, mc, leafs, steps);
					df2 = buildNode(a, b, lr, hr, mc + 1, hc, leafs, steps);
				}
			}
			return ((df1 > df2) ? df1 : df2);
		}
		else {
			finish {
				async {
					df1 = buildNode(a, b, lr, mr, lc, hc, leafs, steps);
					df2 = buildNode(a, b, mr + 1, hr, lc, hc, leafs, steps);
				}
			}
			return ((df1 > df2) ? df1 : df2);
		}
	}

	public static double processLeafNode(double[][] A, double[][] B, int loRow, int hiRow, int loCol, int hiCol, int steps) {
		boolean AtoB = (steps & 1) == 0;
		double[][] a = AtoB ? A : B;
		double[][] b = AtoB ? B : A;
		double md = 0.0D;
		for(int i = loRow; i <= hiRow; ++i) {
			for(int j = loCol; j <= hiCol; ++j) {
				double v = 0.25D * (a[i - 1][j] + a[i][j - 1] + a[i + 1][j] + a[i][j + 1]);
				b[i][j] = v;
				double diff = v - a[i][j];
				if(diff < 0) 
					diff = -diff;
				if(diff > md) 
					md = diff;
			}
		}
		return md;
	}
}
