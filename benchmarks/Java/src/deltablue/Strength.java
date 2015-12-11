package deltablue;

import som.Dictionary;

/*
 * Strengths are used to measure the relative importance of constraints. New
 * strengths may be inserted in the strength hierarchy without disrupting
 * current constraints. Strengths cannot be created outside this class, so
 * pointer comparison can be used for value comparison.
 */
public class Strength {

  public enum S { ABSOLUTE_STRONGEST, REQUIRED, STRONG_PREFERRED, PREFERRED,
    STRONG_DEFAULT, DEFAULT, WEAK_DEFAULT, ABSOLUTE_WEAKEST };

  private final int arithmeticValue;
  @SuppressWarnings("unused")
  private final S   symbolicValue;

  private Strength(final S symbolicValue) {
    this.symbolicValue = symbolicValue;
    this.arithmeticValue = strengthTable.at(symbolicValue);
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

  public static Strength of(final S strength) {
    return strengthConstant.at(strength);
  }

  private static Dictionary<S, Integer> createStrengthTable() {
    Dictionary<S, Integer> strengthTable = new Dictionary<>();
    strengthTable.atPut(S.ABSOLUTE_STRONGEST, -10000);
    strengthTable.atPut(S.REQUIRED,           -800);
    strengthTable.atPut(S.STRONG_PREFERRED,   -600);
    strengthTable.atPut(S.PREFERRED,          -400);
    strengthTable.atPut(S.STRONG_DEFAULT,     -200);
    strengthTable.atPut(S.DEFAULT,             0);
    strengthTable.atPut(S.WEAK_DEFAULT,        500);
    strengthTable.atPut(S.ABSOLUTE_WEAKEST,    10000);
    return strengthTable;
  }

  private static Dictionary<S, Strength> createStrengthConstants() {
    Dictionary<S, Strength> strengthConstant = new Dictionary<>();
    strengthTable.getKeys().forEach(key ->
      strengthConstant.atPut(key, new Strength(key))
    );
    return strengthConstant;
  }

  static {
    strengthTable     = createStrengthTable();
    strengthConstant  = createStrengthConstants();

    absoluteWeakest   = of(S.ABSOLUTE_WEAKEST);
    required          = of(S.REQUIRED);
  }


  public static Strength absoluteWeakest() {
    return absoluteWeakest;
  }

  public static Strength required() {
    return required;
  }

  private static final Strength absoluteWeakest;
  private static final Strength required;

  private static final Dictionary<S, Integer>  strengthTable;
  private static final Dictionary<S, Strength> strengthConstant;
}
