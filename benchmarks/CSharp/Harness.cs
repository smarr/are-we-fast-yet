namespace Benchmarks;

internal sealed class Harness
{
    private static Run ProcessArguments(string[] args)
    {
        Run run = new(args[0]);

        if (args.Length > 1)
        {
            run.SetIterations(int.Parse(args[1]));
            if (args.Length > 2)
            {
                run.SetInnerIterations(int.Parse(args[2]));
            }
        }

        return run;
    }

    private static void PrintUsage()
    {
        Console.WriteLine("Harness [benchmark] [num-iterations [inner-iter]]");
        Console.WriteLine();
        Console.WriteLine("  benchmark      - benchmark class name ");
        Console.WriteLine("  num-iterations - number of times to execute benchmark, default: 1");
        Console.WriteLine("  inner-iter     - number of times the benchmark is executed in an inner loop, ");
        Console.WriteLine("                   which is measured in total, default: 1");
    }

    public static void Main(string[] args)
    {
        if (args.Length < 1)
        {
            PrintUsage();
            Environment.Exit(1);
        }

        Run run = ProcessArguments(args);
        run.RunBenchmark();
        run.PrintTotal();
    }
}