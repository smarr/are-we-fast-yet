﻿namespace Harness.Benchmarks;

public sealed class Richards : IBenchmark
{
    public bool Benchmark(int innerIterations)
    {
        return new Rich.Scheduler().Start();
    }
}