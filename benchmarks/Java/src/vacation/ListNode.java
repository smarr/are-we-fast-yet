package vacation;

public class ListNode {
  private final ReservationInfo data;
  private ListNode next;

  public ListNode(final ReservationInfo data) {
    this.data = data;
  }

  ReservationInfo getData() {
    return data;
  }

  ListNode getNext() {
    return next;
  }

  void setNext(ListNode next) {
    this.next = next;
  }
}
