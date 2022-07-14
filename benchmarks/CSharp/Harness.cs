namespace Benchmarks;

public static class Harness
{
    public static void Main(string[] args)
    {
        if (args.Length == 0)
        {
            PrintHelp();
            return;
        }

        var benchmarkName = args[0];
        var numberOfIterations = ArgumentOrDefault(1, 1);
        var numberOfInnerIterations = ArgumentOrDefault(2, 1);

        var benchmarkInstance = CreateBenchmarkInstance(benchmarkName);
        if (benchmarkInstance == null)
        {
            PrintHelp();
            Console.WriteLine();
            Console.WriteLine($"Error: Benchmark \"{benchmarkName}\" was not found.");
            Console.WriteLine("Known benchmarks:");
            foreach (var known in KnownBenchmarks.Keys.OrderBy(x => x))
                Console.WriteLine(known);
            return;
        }

        var run = new Run(benchmarkInstance.GetType().Name);
        run.SetIterations(numberOfIterations);
        run.SetInnerIterations(numberOfInnerIterations);
        run.RunBenchmark(benchmarkInstance);

        int ArgumentOrDefault(int index, int defaultValue)
        {
            return args.Length > index ? int.Parse(args[index]) : defaultValue;
        }
    }

    private static void PrintHelp()
    {
        Console.WriteLine("Harness [benchmark] [num-iterations [inner-iter]]");
        Console.WriteLine();
        Console.WriteLine("  benchmark      - benchmark class name ");
        Console.WriteLine("  num-iterations - number of times to execute benchmark, default: 1");
        Console.WriteLine("  inner-iter     - number of times the benchmark is executed in an inner loop, ");
        Console.WriteLine("                   which is measured in total, default: 1");
    }

    private static readonly Dictionary<string, Type> KnownBenchmarks = new(StringComparer.InvariantCultureIgnoreCase)
    {
        {nameof(Bounce), typeof(Bounce) },
        {nameof(List), typeof(List) },
        {nameof(NBody), typeof(NBody) },
        {nameof(Permute), typeof(Permute) },
        {nameof(Queens), typeof(Queens) },
        {nameof(Richards), typeof(Richards) },
        {nameof(Sieve), typeof(Sieve) },
        {nameof(Storage), typeof(Storage) },
        {nameof(Towers), typeof(Towers) },
    };

    private static Benchmark? CreateBenchmarkInstance(string name)
    {
        KnownBenchmarks.TryGetValue(name, out var benchmarkClass);
        if (benchmarkClass == null)
            return null;
        var benchmarkInstance = Activator.CreateInstance(benchmarkClass) as Benchmark;
        return benchmarkInstance;
    }
}