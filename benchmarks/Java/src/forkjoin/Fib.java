package forkjoin;

import java.util.concurrent.RecursiveTask;

import som.Benchmark;

public final class Fib extends Benchmark {
  private static final int N = 40;

	private static final int[] RESULTS = {
		0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181,6765,10946,
		17711,28657, 46368,75025,121393,196418,317811,514229,832040,1346269,2178309,
		3524578,5702887,9227465, 14930352,24157817,39088169,63245986,102334155,
		165580141,267914296,433494437,701408733,1134903170
	};

	@Override
  public Object benchmark() {
	  return new Fibonacci(N).compute();
	}

	@Override
  public boolean verifyResult(final Object result) {
	  int r = (int) result;
	  return r == RESULTS[N];
	}

	private final static class Fibonacci extends RecursiveTask<Integer> {
    private static final long serialVersionUID = -2556124042120695959L;

    private final int n;

    public Fibonacci(final int n) {
      this.n = n;
    }

    @Override
    protected Integer compute() {
      if (n <= 2) {
        return 1;
      }

      Fibonacci taskA = new Fibonacci(n - 1);
      taskA.fork();
      Fibonacci taskB = new Fibonacci(n - 2);
      taskB.fork();

      return taskA.join() + taskB.join();
    }
	}
}
