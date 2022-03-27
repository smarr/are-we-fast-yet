namespace Harness.Benchmarks.Rich;

sealed class HandlerTaskDataRecord : RBObject
{
    public HandlerTaskDataRecord()
    {
        WorkIn = DeviceIn = NO_WORK;
    }

    public Packet DeviceIn { get; set; }

    public void DeviceInAdd(Packet packet)
    {
        DeviceIn = Append(packet, DeviceIn);
    }

    public Packet WorkIn { get; set; }

    public void WorkInAdd(Packet packet)
    {
        WorkIn = Append(packet, WorkIn);
    }
}
