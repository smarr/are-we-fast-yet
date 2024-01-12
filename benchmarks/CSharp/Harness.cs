using System.Diagnostics;

public class Run
{
  public readonly string Name;
  public readonly Type BenchmarkSuite;
  public int NumIterations;
  public int InnerIterations;
  public long Total;

  public Run(String name)
  {
    this.Name = name;
    this.BenchmarkSuite = SuiteFromName(name);
    NumIterations = 1;
    InnerIterations = 1;
  }

  public Type SuiteFromName(String name)
  {
    Type? suite = Type.GetType(name);
    if (suite == null) {
        throw new Exception("Suite " + name + " not found");
    }
    return suite;
  }


  public void RunBenchmark()
  {
    Console.WriteLine("Starting " + Name + " benchmark ...");

    Benchmark benchmark = (Benchmark) Activator.CreateInstance(BenchmarkSuite);

    DoRuns(benchmark);

    ReportBenchmark();

    Console.WriteLine("");
  }

  public void measure(Benchmark bench)
  {
    Stopwatch timer = Stopwatch.StartNew();
    if (!bench.InnerBenchmarkLoop(InnerIterations))
    {
      throw new InvalidOperationException("Benchmark failed with incorrect result");
    }
    timer.Stop();
    long runTime = timer.ElapsedMilliseconds;

    PrintResult(runTime);

    Total += runTime;
  }

  public void DoRuns(Benchmark bench)
  {
    for (int i = 0; i < NumIterations; i++)
    {
      measure(bench);
    }
  }

  public void ReportBenchmark()
  {
    Console.WriteLine(Name + ": iterations=" + NumIterations +
        " average: " + (Total / NumIterations) + "ms total: " + Total + "ms/n");
  }

  public void PrintResult(long runTime)
  {
    Console.WriteLine(Name + ": iterations=1 runtime: " + runTime + "ms");
  }

  public void PrintTotal()
  {
    Console.WriteLine("Total Runtime: " + Total + "ms");
  }
}

public class Harness
{
  public static Run ProcessArguments(String[] args)
  {
    Run run = new Run(args[0]);

    if (args.Length > 1)
    {
      run.NumIterations = Int32.Parse(args[1]);
      if (args.Length > 2)
      {
        run.InnerIterations = Int32.Parse(args[2]);
      }
    }
    return run;
  }

  public static void PrintUsage()
  {
    Console.WriteLine("Harness [benchmark] [num-iterations inner-iter]]");
    Console.WriteLine(" ");
    Console.WriteLine(" benchmark       -benchmark class name");
    Console.WriteLine(" num-iterations  -number of times to execute benchmark, default: 1");
    Console.WriteLine(" inner-iter      -number of times the benchmark is executed in an inner loop, ");
    Console.WriteLine("                  which is measured in total, default: 1");
  }

  public static int Main(String[] args)
  {
    if (args.Length < 2)
    {
      PrintUsage();
      return 1;
    }

    Run run = ProcessArguments(args);
    run.RunBenchmark();
    run.PrintTotal();
    return 0;
  }
}