namespace AreWeFastYet;

public sealed class Richards : Benchmark
{
    public override object Execute()
    {
        return new Scheduler().Start();
    }

    public override bool VerifyResult(object result)
    {
        return (bool)result;
    }
}
