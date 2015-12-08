package deltablue;

import java.util.HashMap;

/*
 * Strengths are used to measure the relative importance of constraints. New
 * strengths may be inserted in the strength hierarchy without disrupting
 * current constraints. Strengths cannot be created outside this class, so
 * pointer comparison can be used for value comparison.
 */
public class Strength {

  private int    arithmeticValue;
  private String symbolicValue;

  private Strength(final String symbolicValue) {
    this.symbolicValue = symbolicValue;
    this.arithmeticValue = strengthTable.get(symbolicValue);
  }

  public boolean sameAs(final Strength s) {
    return arithmeticValue == s.getArithmeticValue();
  }

  public boolean stronger(final Strength s) {
    return arithmeticValue < s.getArithmeticValue();
  }

  public boolean weaker(final Strength s) {
    return arithmeticValue > s.getArithmeticValue();
  }

  public Strength strongest(final Strength s) {
    return s.stronger(this) ? s : this;
  }

  public Strength weakest(final Strength s) {
    return s.weaker(this) ? s : this;
  }

  public int getArithmeticValue() {
    return arithmeticValue;
  }

  public void print() {
    System.out.print("strength[" + Integer.toString(arithmeticValue) + "]");
  }

  public HashMap<String, Integer> strengthTable() {
    return strengthTable;
  }

  public static Strength of(final String aSymbol) {
    return strengthConstant.get(aSymbol);
  }

  public static void initialize() {
    strengthTable = new HashMap<>();
    strengthTable.put("absoluteStrongest", -10000);
    strengthTable.put("required",          -800);
    strengthTable.put("strongPreferred",   -600);
    strengthTable.put("preferred",         -400);
    strengthTable.put("strongDefault",     -200);
    strengthTable.put("default",            0);
    strengthTable.put("weakDefault",        500);
    strengthTable.put("absoluteWeakest",    10000);

    strengthConstant = new HashMap<>();
    strengthTable.keySet().forEach(key ->
      strengthConstant.put(key, new Strength(key))
    );

    absoluteStrongest = of("absoluteStrongest");
    absoluteWeakest   = of("absoluteWeakest");
    required          = of("required");
  }

  public static Strength absoluteStrongest() {
    return absoluteStrongest;
  }

  public static Strength absoluteWeakest() {
    return absoluteWeakest;
  }

  public static Strength required() {
    return required;
  }

  private static Strength absoluteStrongest;
  static Strength absoluteWeakest;
  static Strength required;

  private static HashMap<String, Integer>  strengthTable;
  private static HashMap<String, Strength> strengthConstant;
}
