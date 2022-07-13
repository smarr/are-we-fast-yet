using System.Diagnostics;

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

    public void RunBenchmark(Benchmark benchmarkInstance)
    {
        Console.WriteLine("Starting " + benchmarkInstance.ToString() + " benchmark ...");
        DoRuns(benchmarkInstance);        
        ReportBenchmark();
        Console.WriteLine();        
    }

    private void DoRuns(Benchmark bench)
    {
        for (var i = 0; i < Iterations; i++)
        {
            Measure(bench);
        }
    }
    private void Measure(Benchmark bench)
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