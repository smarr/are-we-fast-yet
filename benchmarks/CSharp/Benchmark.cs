public abstract class Benchmark {
    public abstract object Execute();
    public abstract bool VerifyResult(object result);

    public bool InnerBenchmarkLoop(int innerIterations){
        for(int i = 0; i < innerIterations; i++){
            if(!VerifyResult(Execute())){
                return false;
            }
        }
        return true;
    }
}