﻿using System.Diagnostics;

if (args.Length == 0)
{
    PrintHelp();
    return;
}

var benchmarkName = args[0];
var NumberOfIterations = ArgumentOrDefault(1, 1);
var NumberOfInnerIterations = ArgumentOrDefault(2, 1);

var benchmarkInstance = CreateBenchmarkInstance(benchmarkName!);
if (benchmarkInstance == null)
{
    PrintHelp();
    Console.WriteLine();
    Console.WriteLine($"Error: Benchmark \"{ benchmarkName }\" was not found.");
    return;
}
var run = new Run(benchmarkInstance.GetType().Name) { 
    Iterations = NumberOfIterations,
    InnerIterations = NumberOfInnerIterations
};
run.RunBenchmark(benchmarkInstance);

void PrintHelp()
{
    Console.WriteLine("Harness [benchmark] [num-iterations [inner-iter]]");
    Console.WriteLine();
    Console.WriteLine("  benchmark      - benchmark class name ");
    Console.WriteLine("  num-iterations - number of times to execute benchmark, default: 1");
    Console.WriteLine("  inner-iter     - number of times the benchmark is executed in an inner loop, ");
    Console.WriteLine("                   which is measured in total, default: 1");
}

int ArgumentOrDefault(int index, int defaultValue) => args.Length > index ? int.Parse(args[index]) : defaultValue;

IBenchmark? CreateBenchmarkInstance(string name)
{
    var benchmarkClass = Type.GetType("AreWeFastYet." + name, false, true);
    if (benchmarkClass == null)
        return null;
    var benchmarkInstance = Activator.CreateInstance(benchmarkClass) as IBenchmark;
    return benchmarkInstance;
}

class Run
{
    public int Iterations { get; set; } = 1;
    public int InnerIterations { get; set; } = 1;
    /// <summary>
    /// Total runtime in microseconds
    /// </summary>
    public long TotalRuntime { get; private set; }
    public string Name { get; }

    public Run(string name)
    {
        Name = name;
    }

    public void RunBenchmark(IBenchmark benchmarkInstance)
    {
        Console.WriteLine("Starting " + benchmarkInstance.ToString() + " benchmark ...");
        DoRuns(benchmarkInstance);        
        ReportBenchmark();
        Console.WriteLine();        
    }

    private void DoRuns(IBenchmark bench)
    {
        for (var i = 0; i < Iterations; i++)
        {
            Measure(bench);
        }
    }
    private void Measure(IBenchmark bench)
    {
        var sw = new Stopwatch();
        sw.Start();
        if (!bench.InnerBenchmarkLoop(InnerIterations))
        {
            throw new Exception("Benchmark failed with incorrect result");
        }
        var runTime = (long)(sw.Elapsed.TotalMilliseconds * 1000);
        PrintResult(runTime);
        TotalRuntime += runTime;
    }

    private void PrintResult(long runTime)
    {
        Console.WriteLine($"{Name} iteration runtime: {runTime}µs");
    }

    private void ReportBenchmark()
    {
        var avgTimeUs = TotalRuntime / Iterations;
        Console.WriteLine($"{Name}: iterations={Iterations} average: {avgTimeUs}µs total: {TotalRuntime}µs\n");
    }
}