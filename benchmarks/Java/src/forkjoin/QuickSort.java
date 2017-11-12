import java.util.Random;

public class QuickSort {

	private static int partition(int[] data, int left, int right) {
		int i = left;
		int j = right;
		int tmp;
		int pivot = data[(left + right) / 2];

		while (i <= j) {
			while (data[i] < pivot) i++;
			while (data[j] > pivot) j--;
			if (i <= j) {
				tmp = data[i];
				data[i] = data[j];
				data[j] = tmp;
				i++;
				j--;
			}
		}

		return i;
	}

	private static void qsort(final int[] data, final int left, final int right) {
		final int index = partition(data, left, right);
		finish {
			if (left < index - 1) {
				async {
					qsort(data, left, index - 1);
				}
			}

			if (index < right) {
				qsort(data, index, right);
			}
		}  
	}

	public static void main(String[] args) {
		int N = 10000000; // 10 million
		boolean check = true;

		if (args.length > 0)
			N = Integer.parseInt(args[0]);
		if(args.length > 1)
			check = Boolean.parseBoolean(args[1]);

		System.out.println("Input: N = "+N+" check = "+check);
		
		final int[] data = new int[N];
		final int[] backup = new int[N];

		final Random r = new Random(0);
		for(int i=0; i<N; i++) {
			backup[i] = r.nextInt(9999);
		}
		
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
				System.arraycopy(backup, 0, data, 0, N); // restore the old array
				final long startTime = System.currentTimeMillis();
				qsort(data, 0, N-1);
				final long time = System.currentTimeMillis() - startTime;
				final double secs = ((double)time) / 1000.0;
				//check the result
				int a = 0;
				int b;
				if(harnessStarted) {
					if(N == 10000000) {
						if(org.jikesrvm.scheduler.WS.wsTotalPush() != 5822908) {
							System.out.println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
							System.exit(-1);
						}
					}
				}
				if(check) {
					boolean ok= true;
					for (int k=0; k<N; k++) {
						b = data[k];
						ok &= (a <= b);
						a = b;
					}
					if(ok){
						System.out.println("QuickSort ("+ N +"): passed. Time = "+ secs+" secs");   
					}
					else{
						System.out.printf("QuickSort ("+ N +"): failed. Time = "+secs +" secs"); 
					}
				}
				else {
					System.out.println("Time = "+ secs+" secs");
				}
				org.jikesrvm.scheduler.WS.dumpWSStatistics();
			}
		}

		org.jikesrvm.scheduler.RVMThread.perfEventStop();
		org.mmtk.plan.Plan.harnessEnd();

		final double duration = (((double)(System.nanoTime() - start))/((double)(1.0E9))) * 1000;
		System.out.printf("===== Test PASSED in %d msec =====\n",(int)duration);
	}
}
