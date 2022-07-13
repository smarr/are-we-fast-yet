using System.Diagnostics;

namespace Benchmarks;

sealed class Run
{
    private int iterations;
    private int innerIterations;
    private long total;
    private readonly string name;

    public Run(string name)
    {
        this.name = name;
        iterations = 1;
        innerIterations = 1;
    }

    public void RunBenchmark(Benchmark benchmarkInstance)
    {
        Console.WriteLine("Starting " + benchmarkInstance + " benchmark ...");
        DoRuns(benchmarkInstance);
        ReportBenchmark();
        Console.WriteLine();
    }

    private void DoRuns(Benchmark bench)
    {
        for (var i = 0; i < iterations; i++)
        {
            Measure(bench);
        }
    }
    private void Measure(Benchmark bench)
    {
        var sw = new Stopwatch();
        sw.Start();
        if (!bench.InnerBenchmarkLoop(innerIterations))
        {
            throw new Exception("Benchmark failed with incorrect result");
        }
        var runTime = (long)(sw.Elapsed.TotalMilliseconds * 1000);
        PrintResult(runTime);
        total += runTime;
    }

    private void PrintResult(long runTime)
    {
        Console.WriteLine($"{name}: iterations=1 runtime: {runTime}us");
    }

    private void ReportBenchmark()
    {
        var avgTimeUs = total / iterations;
        Console.WriteLine($"{name}: iterations={iterations} average: {avgTimeUs}us total: {total}us\n");
    }

    public void SetIterations(int value) {
        iterations = value;
    }

    public void SetInnerIterations(int value) {
        this.innerIterations = value;
    }
}