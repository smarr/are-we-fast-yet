namespace Harness.Benchmarks.Rich;

sealed class WorkerTaskDataRecord : RBObject
{
    public WorkerTaskDataRecord()
    {
        Destination = HANDLER_A;
        Count = 0;
    }

    public int Count { get; set; }

    public int Destination { get; set; }
}
