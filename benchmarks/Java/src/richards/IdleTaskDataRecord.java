package richards;


public class IdleTaskDataRecord extends RBObject {
  private int control;
  private int count;

  public int getControl() { return control; }
  public void setControl(final int aNumber) {
    control = aNumber;
  }

  public int getCount() { return count; }
  public void setCount(final int aCount) { count = aCount; }

  public IdleTaskDataRecord() {
    control = 1;
    count = 10000;
  }

  public static IdleTaskDataRecord create() { return new IdleTaskDataRecord(); }
}
