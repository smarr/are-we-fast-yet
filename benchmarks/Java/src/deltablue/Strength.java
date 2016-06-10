/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue;

import som.Dictionary.CustomHash;
import som.IdentityDictionary;

/*
 * Strengths are used to measure the relative importance of constraints. New
 * strengths may be inserted in the strength hierarchy without disrupting
 * current constraints. Strengths cannot be created outside this class, so
 * pointer comparison can be used for value comparison.
 */
public final class Strength {

  public static class Sym implements CustomHash {

    private final int hash;

    Sym(final int hash) {
      this.hash = hash;
    }

    @Override
    public int customHash() {
      return hash;
    }
  }

  public static final Sym ABSOLUTE_STRONGEST = new Sym(0);
  public static final Sym REQUIRED           = new Sym(1);
  public static final Sym STRONG_PREFERRED   = new Sym(2);
  public static final Sym PREFERRED          = new Sym(3);
  public static final Sym STRONG_DEFAULT     = new Sym(4);
  public static final Sym DEFAULT            = new Sym(5);
  public static final Sym WEAK_DEFAULT       = new Sym(6);
  public static final Sym ABSOLUTE_WEAKEST   = new Sym(7);

  private final int arithmeticValue;
  @SuppressWarnings("unused")
  private final Sym   symbolicValue;

  private Strength(final Sym symbolicValue) {
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

  public static Strength of(final Sym strength) {
    return strengthConstant.at(strength);
  }

  private static IdentityDictionary<Sym, Integer> createStrengthTable() {
    IdentityDictionary<Sym, Integer> strengthTable = new IdentityDictionary<>();
    strengthTable.atPut(ABSOLUTE_STRONGEST, -10000);
    strengthTable.atPut(REQUIRED,           -800);
    strengthTable.atPut(STRONG_PREFERRED,   -600);
    strengthTable.atPut(PREFERRED,          -400);
    strengthTable.atPut(STRONG_DEFAULT,     -200);
    strengthTable.atPut(DEFAULT,             0);
    strengthTable.atPut(WEAK_DEFAULT,        500);
    strengthTable.atPut(ABSOLUTE_WEAKEST,    10000);
    return strengthTable;
  }

  private static IdentityDictionary<Sym, Strength> createStrengthConstants() {
    IdentityDictionary<Sym, Strength> strengthConstant = new IdentityDictionary<>();
    strengthTable.getKeys().forEach(key ->
      strengthConstant.atPut(key, new Strength(key))
    );
    return strengthConstant;
  }

  static {
    strengthTable     = createStrengthTable();
    strengthConstant  = createStrengthConstants();

    absoluteWeakest   = of(ABSOLUTE_WEAKEST);
    required          = of(REQUIRED);
  }


  public static Strength absoluteWeakest() {
    return absoluteWeakest;
  }

  public static Strength required() {
    return required;
  }

  private static final Strength absoluteWeakest;
  private static final Strength required;

  private static final IdentityDictionary<Sym, Integer>  strengthTable;
  private static final IdentityDictionary<Sym, Strength> strengthConstant;
}
