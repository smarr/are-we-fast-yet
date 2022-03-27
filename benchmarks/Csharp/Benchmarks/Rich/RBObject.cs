namespace Harness.Benchmarks.Rich;

abstract class RBObject
{
    public virtual Packet Append(Packet packet, Packet queueHead)
    {
        packet.Link = NO_WORK;
        if (NO_WORK == queueHead)
            return packet;

        Packet mouse = queueHead;
        Packet link;
        while (NO_WORK != (link = mouse.Link))
        {
            mouse = link;
        }
        mouse.Link = packet;
        return queueHead;
    }

    public const int IDLER = 0;
    public const int WORKER = 1;
    public const int HANDLER_A = 2;
    public const int HANDLER_B = 3;
    public const int DEVICE_A = 4;
    public const int DEVICE_B = 5;
    public const int NUM_TYPES = 6;

    public const int DEVICE_PACKET_KIND = 0;
    public const int WORK_PACKET_KIND = 1;

    public const Packet NO_WORK = null;
    public const TaskControlBlock NO_TASK = null;
}
