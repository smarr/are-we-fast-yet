
public interface IBenchmark
{
    public bool InnerBenchmarkLoop(int innerIterations);
}

public abstract class Benchmark : IBenchmark
{
    public abstract object Execute();
    public abstract bool VerifyResult(object result);
    public virtual bool InnerBenchmarkLoop(int innerIterations)
    {
        for (int i = 0; i < innerIterations; i++)
        {
            if (!VerifyResult(Execute()))
            {
                return false;
            }
        }
        return true;
    }
}
