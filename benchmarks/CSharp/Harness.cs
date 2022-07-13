using System;
using System.Diagnostics;
using System.Runtime;
using System.Reflection;

public class Run{
    public String name;
    public Type benchmarkSuite;
    public int NumIterations;
    public int InnerIterations;
    public long total;
    Stopwatch timer;

    public Run(String name){
        this.name = name;
        this.benchmarkSuite = getSuiteFromName(name);
        NumIterations = 1;
        InnerIterations = 1;
    }

    public Type getSuiteFromName(String name){
        Type suite = Type.GetType(name);
        return suite;
    }


    public void runBenchmark(){
    Console.WriteLine("Starting " + name + " benchmark ...");

    Benchmark benchmark = (Benchmark) Activator.CreateInstance(benchmarkSuite);

    doRuns(benchmark);

    reportBenchmark();

    Console.WriteLine("");
    }

    public void measure(Benchmark bench){
        timer = Stopwatch.StartNew();
        if (!bench.innerBenchmarkLoop(InnerIterations)) {
            throw new InvalidOperationException("Benchmark failed with incorrect result");
        }
        timer.Stop();
        long runTime = timer.ElapsedMilliseconds;

        printResult(runTime);

        total +=runTime;
    }

    public void doRuns(Benchmark bench){
        for(int i = 0; i < NumIterations; i++){
            measure(bench);
        }
    }

    public void reportBenchmark(){
        Console.WriteLine(name + ": iterations=" + NumIterations +
            " average: " + (total / NumIterations) + "ms total: " + total + "ms/n");
    }

    public void printResult(long runTime){
        Console.WriteLine(name + ": iterations=1 runtime: " + runTime + "ms");
    }

    public void printTotal(){
        Console.WriteLine("Total Runtime: " + total + "ms");
    }
}

public abstract class Benchmark {
    public abstract Object benchmark();
    public abstract bool verifyResult(Object result);

    public bool innerBenchmarkLoop(int innerIterations){
        for(int i = 0; i < innerIterations; i++){
            if(!verifyResult(benchmark())){
                return false;
            }
        }
        return true;
    }
}

public class Harness {
    public static Run processArguments(String[] args){
        Run run = new Run(args[0]);

        if(args.Length > 1){
            run.NumIterations = Int32.Parse(args[1]) ;
            if(args.Length > 2){
                run.InnerIterations = Int32.Parse(args[2]);
            }
        }
        return run;
    }
    
    public static void printUsage() {
        Console.WriteLine("Harness [benchmark] [num-iterations inner-iter]]");
        Console.WriteLine(" ");
        Console.WriteLine(" benchmark       -benchmark class name");
        Console.WriteLine(" num-iterations  -number of times to exectute benchmark, default: 1");
        Console.WriteLine(" inner-iter      -number of times the benchmark is exectuted in an inner loop, ");
        Console.WriteLine("                  which is measured in total, default: 1");
    }

    public static int Main(String[] args){
        if(args.Length < 2){
            printUsage();
            return 1;
        }

        Run run = processArguments(args);
        run.runBenchmark();
        run.printTotal();
        return 0;
    }
}