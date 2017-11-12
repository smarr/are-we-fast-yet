public class Fib {
	private static int[] results = {
		0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181,6765,10946,17711,28657,
		46368,75025,121393,196418,317811,514229,832040,1346269,2178309,3524578,5702887,9227465, 
		14930352,24157817,39088169,63245986,102334155,165580141,267914296,433494437,701408733,1134903170
	};
	
	public static void main(String args[]) {
		int n = 40;

		if (args.length > 0)
			n = Integer.parseInt(args[0]);

		System.out.println("N = "+n);
		
		boolean harnessStarted = false;

		int l_start=1;
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
				final long startTime = System.currentTimeMillis();
				int r = fib(n);
				final long time = System.currentTimeMillis() - startTime;
				final double secs = ((double)time) / 1000.0;
				if(harnessStarted) {
					if(r != results[n]) {
						System.out.println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
						System.exit(-1);
					}
				}
				System.out.println(" Time: " + secs);
				org.jikesrvm.scheduler.WS.dumpWSStatistics();
			}
		}

		org.jikesrvm.scheduler.RVMThread.perfEventStop();
		org.mmtk.plan.Plan.harnessEnd();

		final double duration = (((double)(System.nanoTime() - start))/((double)(1.0E9))) * 1000;
		System.out.printf("===== Test PASSED in %d msec =====\n",(int)duration);
	}

	private static int fib(int n) {
		int a = 0;
		int b = 0;
		if (n <= 2) return 1;
		finish {
			async {
				a = fib(n-1);
				b = fib(n-2);
			}
		}
		return a + b;
	}
}
