package vacation;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Barrier {

  private static CyclicBarrier barrier;

  public static void enterBarrier() {
    try {
      barrier.await();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BrokenBarrierException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void setBarrier(final int x) {
    barrier = new CyclicBarrier(x);
  }

  public static void assertIsClear() {
    int numberWaiting = barrier.getNumberWaiting();
    if (numberWaiting != 0) {
      // Checkstyle: stop
      System.out.println(String.format("Bad barrier: %d waiting", numberWaiting));
    } else {
      System.out.println("Barrier is clear.");
      // Checkstyle: resume
    }
  }
}
