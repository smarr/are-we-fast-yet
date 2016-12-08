package vacation;

public class Defines {

  public static final int  ACTION_MAKE_RESERVATION    = 0;
  public static final int  ACTION_DELETE_CUSTOMER     = 1;
  public static final int  ACTION_UPDATE_TABLES       = 2;
  public static final int  PARAM_DEFAULT_CLIENTS      = 10; // number of clients
  public static final int  PARAM_DEFAULT_NUMBER       = 10; // number of user queries/transaction
  public static final int  PARAM_DEFAULT_QUERIES      = 90; // percentage of relations queried
  public static final int  PARAM_DEFAULT_RELATIONS    = 1 << 16; // number of possible relations
  public static final int  PARAM_DEFAULT_TRANSACTIONS = 1 << 16; // number of transactions
  public static final int  PARAM_DEFAULT_USER         = 80;      // percentage of user transactions
  public static final int  RESERVATION_CAR            = 0;
  public static final int  RESERVATION_FLIGHT         = 1;
  public static final int  RESERVATION_ROOM           = 2;
  public static final int  NUM_RESERVATION_TYPE       = 3;
}
