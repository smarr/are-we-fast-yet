package vacation;

public class Defines {

  public static final int  ACTION_MAKE_RESERVATION    = 0;
  public static final int  ACTION_DELETE_CUSTOMER     = 1;
  public static final int  ACTION_UPDATE_TABLES       = 2;
  public static final long NUM_ACTION                 = 2;
  public static final long PARAM_CLIENTS              = 'c';
  public static final long PARAM_NUMBER               = 'n';
  public static final long PARAM_QUERIES              = 'q';
  public static final long PARAM_RELATIONS            = 'r';
  public static final long PARAM_TRANSACTIONS         = 't';
  public static final long PARAM_USER                 = 'u';
  public static final int  PARAM_DEFAULT_CLIENTS      = (10);
  public static final int  PARAM_DEFAULT_NUMBER       = (10);
  public static final int  PARAM_DEFAULT_QUERIES      = (90);
  public static final int  PARAM_DEFAULT_RELATIONS    = (1 << 16);
  public static final int  PARAM_DEFAULT_TRANSACTIONS = (1 << 26);
  public static final int  PARAM_DEFAULT_USER         = (80);
  public static final int  RESERVATION_CAR            = 0;
  public static final int  RESERVATION_FLIGHT         = 1;
  public static final int  RESERVATION_ROOM           = 2;
  public static final int  NUM_RESERVATION_TYPE       = 3;
  public static final long OPERATION_MAKE_RESERVATION = 0L;
  public static final long OPERATION_DELETE_CUSTOMER  = 1L;
  public static final long OPERATION_UPDATE_TABLE     = 2L;
  public static final long NUM_OPERATION              = 3;

  public static final int  RED                        = 0;
  public static final int  BLACK                      = 1;

}
