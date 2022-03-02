using System;

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