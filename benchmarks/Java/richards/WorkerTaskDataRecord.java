package som.richards;


public class WorkerTaskDataRecord extends RBObject {
  private int destination;
  private int count;

  public int getCount() { return count; }
  public void setCount(final int aCount) { count = aCount; }

  public int getDestination() { return destination; }
  public void setDestination(final int aHandler) { destination = aHandler; }

  private WorkerTaskDataRecord() {
    destination = RBObject.handlerA();
    count = 0;
  }

  public static WorkerTaskDataRecord create() {
    return new WorkerTaskDataRecord();
  }
}
