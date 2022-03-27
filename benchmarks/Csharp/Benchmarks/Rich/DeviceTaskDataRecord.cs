namespace Harness.Benchmarks.Rich;

internal sealed class DeviceTaskDataRecord : RBObject
{
    internal DeviceTaskDataRecord()
    {
        Pending = NO_WORK;
    }

    public Packet Pending { get; set; }
}
