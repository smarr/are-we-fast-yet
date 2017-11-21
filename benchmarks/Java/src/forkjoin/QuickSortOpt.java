package forkjoin;

import java.util.concurrent.RecursiveAction;

import som.Benchmark;
import som.Random;

// Parallelized, and one of the tasks is done locally.

public class QuickSortOpt extends Benchmark {

  private int[] randomNumbers;

  private void initRandomNumbers(final int size) {
    randomNumbers = new int[size];
    final Random r = new Random();
    for (int i = 0; i < size; i++) {
      randomNumbers[i] = r.next();
    }
  }

  @Override
  public boolean innerBenchmarkLoop(final int problemSize) {
    int size = problemSize * 1000;

    if (randomNumbers == null) {
      initRandomNumbers(size);
    }

    final int[] data = new int[size];

    // init data with random numbers
    System.arraycopy(randomNumbers, 0, data, 0, size);

    new QSort(data, 0, size - 1).compute();

    int a = 0;
    for (int k = 0; k < size; k++) {
      int b = data[k];

      if (a > b) {
        return false;
      }
      a = b;
    }

    return true;
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }

	private static int partition(final int[] data, final int left, final int right) {
		int i = left;
		int j = right;
		int tmp;
		int pivot = data[(left + right) / 2];

		while (i <= j) {
			while (data[i] < pivot) {
        i++;
      }

			while (data[j] > pivot) {
        j--;
      }

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

	private static class QSort extends RecursiveAction {
    private static final long serialVersionUID = -5614790014632850696L;

    private final int[] data;
	  private final int left;
	  private final int right;

	  QSort(final int[] data, final int left, final int right) {
	    this.data = data;
	    this.left = left;
	    this.right = right;
	  }

	  @Override
    protected void compute() {
	    final int index = partition(data, left, right);

	    QSort a;
	    if (left < index - 1) {
	      a = new QSort(data, left, index - 1);

	      if (index < right) {
	        a.fork();
	      } else {
	        a.compute();
	      }
	    } else {
	      a = null;
	    }

	    if (index < right) {
	      new QSort(data, index, right).compute();
      } else {
        if (a != null) {
          a.join();
        }
      }
	  }
	}
}
