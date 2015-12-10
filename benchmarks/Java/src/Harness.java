public final class Harness {

  @SuppressWarnings("unchecked")
  private static Class<? extends Benchmark> getSuiteFromName(final String name) {
    try {
      return (Class<? extends Benchmark>) Class.forName(name);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static void processArguments(final String[] args, final Run run) {
    run.setName(args[0]);
    run.setBenchmarkSuite(getSuiteFromName(args[0]));

    if (args.length > 1) {
      run.setNumIterations(Integer.valueOf(args[1]));
      if (args.length > 2) {
        run.setInnerIterations(Integer.valueOf(args[2]));
      }
    }
  }

  private static void printUsage() {
    System.out.println("Harness [benchmark] [num-iterations [inner-iter]]");
    System.out.println();
    System.out.println("  benchmark      - benchmark class name ");
    System.out.println("  num-iterations - number of times to execute benchmark, default: 1");
    System.out.println("  inner-iter     - number of times the benchmark is executed in an inner loop, ");
    System.out.println("                   which is measured in total, default: 1");
  }

  public static void main(final String[] args) {
  	if (args.length < 2) {
  	  printUsage();
  	  System.exit(1);
  	}

  	Run run = new Run();
  	processArguments(args, run);
  	run.runBenchmark();
  	run.printTotal();
  }
}
