import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import leetm.LeeRouter;
import leetm.LeeThread;

public class Lee extends Benchmark {

  private String[] data;
  private int      gridSize;

  @Override
  public boolean innerBenchmarkLoop(final int problemSize, final int numThreads) {
    if (data == null) { loadData(problemSize); }

    int totalLaidTracks = 0;
    LeeRouter lr = new LeeRouter(data, gridSize);

    LeeThread[] threads = new LeeThread[numThreads];

    try {
      for (int i = 0; i < numThreads; i++) {
        threads[i] = new LeeThread(lr);
      }

      for (LeeThread thread : threads) {
        thread.start();
      }

      for (LeeThread thread : threads) {
        thread.join();
        totalLaidTracks += thread.myLaidTracks;
      }
    } catch (Exception e) {
      e.printStackTrace(System.out);
      System.exit(0);
    }

    return lr.sanityCheck(totalLaidTracks, problemSize);
  }

  private void loadData(final int problemSize) {
    String fileName;
    switch (problemSize) {
      case 1:
        fileName = "mainboard.txt";
        gridSize = 600;
        break;

      case 2:
        fileName = "memboard.txt";
        gridSize = 600;
        break;

      case 3:
        fileName = "sparselong.txt";
        gridSize = 600;
        break;

      case 4:
        fileName = "sparseshort.txt";
        gridSize = 600;
        break;

      case 5:
        fileName = "test.txt";
        gridSize = 10;
        break;

      default:
        throw new IllegalArgumentException(
            "Lee does not support arbitrary number of inner iterations. " +
            "This parameter is used to determine the input file. " +
            "Valid values are 1 to 4.");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        LeeRouter.class.getResourceAsStream(fileName)));

    ArrayList<String> list = new ArrayList<>();

    while (true) {
      String line;
      try {
        line = reader.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (line == null) {
        break;
      } else {
        list.add(line);
      }
    }

    data = list.toArray(new String[0]);
  }

  @Override
  public Object benchmark() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new UnsupportedOperationException();
  }
}
