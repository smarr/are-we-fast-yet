namespace Benchmarks;

public class Random
{
  private int seed = 74755;

  public int Next()
  {
    seed = ((seed * 1309) + 13849) & 65535;
    return seed;
  }
}