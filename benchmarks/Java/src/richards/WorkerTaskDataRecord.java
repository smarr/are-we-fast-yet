package richards;


final class WorkerTaskDataRecord extends RBObject {
  private int destination;
  private int count;

  WorkerTaskDataRecord() {
    destination = HANDLER_A;
    count = 0;
  }

  public int getCount() { return count; }
  public void setCount(final int aCount) { count = aCount; }

  public int getDestination() { return destination; }
  public void setDestination(final int aHandler) { destination = aHandler; }
}
