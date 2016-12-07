package vacation;


public class ReservationInfo {

  int id;
  int type;
  int price;

  public ReservationInfo(final int type, final int id, final int price) {
    this.type = type;
    this.id = id;
    this.price = price;
  }

  /**
   * @return -1 if A < B, 0 if A = B, 1 if A > B
   */
  public static int compare(final ReservationInfo a, final ReservationInfo b) {
    int typeDiff = a.type - b.type;
    return typeDiff != 0 ? typeDiff : a.id - b.id;
  }
}
