package forkjoin;

import java.util.concurrent.RecursiveAction;

import som.Benchmark;

// Fully sequential version, replacing all forks with compute.

public class NQueensSeq extends Benchmark {
	private static int[] solutions = {
		1,
		0,
		0,
		2,
		10, /* 5 */
		4,
		40,
		92,
		352,
		724, /* 10 */
		2680,
		14200,
		73712,
		365596,
		2279184, /* 15 */
		14772512
	};

	private int nSolutions;

  @Override
  public boolean innerBenchmarkLoop(final int size) {
    nSolutions = 0;
    compute(size);
    return verify(size);
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }

	private void compute(final int size)  {
		int[] a = new int[0];
		nqueens(size, a, 0).compute();
	}

	private NQueensTask nqueens(final int size, final int[] a, final int depth) {
		if (size == depth) {
		  synchronized (this) {
		    nSolutions += 1;
		  }
			return null;
		}

		return new NQueensTask(this, size, a, depth);
	}

	private NQueensTask kernel(final int size, final int[] a, final int depth,
	    final int i) {
		/* allocate a temporary array and copy <a> into it */
		int[] b = new int[depth + 1];

		System.arraycopy(a, 0, b, 0, depth);

		b[depth] = i;
		boolean status = ok(depth + 1, b);
		if (status) {
			return nqueens(size, b, depth + 1);
		} else {
		  return null;
		}
	}

	/*
	 * <A> contains array of <n> queen positions.  Returns 1
	 * if none of the queens conflict, and returns 0 otherwise.
	 */
	private boolean ok(final int n,  final int[] A) {
		for (int i =  0; i < n; i++) {
			final int p = A[i];

			for (int j = i + 1; j < n; j++) {
				final int q = A[j];
				if (q == p || q == p - (j - i) || q == p + (j - i)) {
          return false;
        }
			}
		}
		return true;
	}

	private boolean verify(final int size) {
		return nSolutions == solutions[size - 1];
	}

	private static class NQueensTask extends RecursiveAction {
    private static final long serialVersionUID = -4319096760849183916L;

    private final NQueensSeq nqueens;
	  private final int size;
	  private final int[] a;
	  private final int depth;

	  NQueensTask(final NQueensSeq nqueens, final int size, final int[] a,
	      final int depth) {
	    this.nqueens = nqueens;
	    this.size = size;
	    this.a = a;
	    this.depth = depth;
	  }

	  @Override
    protected void compute() {
	    /* try each possible position for queen <depth> */
	    for (int i = 0; i < size; i++) {
	      NQueensTask taskOrNull = nqueens.kernel(size, a, depth, i);
	      if (taskOrNull != null) {
          taskOrNull.compute();
        }
	    }
	  }
	}
}
