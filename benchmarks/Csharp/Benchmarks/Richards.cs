using System.Diagnostics;

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


sealed class Scheduler : RBObject
{
    private TaskControlBlock taskList;
    private TaskControlBlock? currentTask;
    private int currentTaskIdentity;
    private readonly TaskControlBlock[] taskTable;

    private int queuePacketCount;
    private int holdCount;

    private int layout;

    private const bool TRACING = false;

    public Scheduler()
    {
        // init tracing
        layout = 0;

        // init scheduler
        queuePacketCount = 0;
        holdCount = 0;
        taskTable = new TaskControlBlock[NUM_TYPES];
        //Not needed: Arrays.Fill(taskTable, NO_TASK);
        taskList = NO_TASK;
    }


    internal void CreateDevice(int identity, int priority, Packet workPacket, TaskState state)
    {
        var data = new DeviceTaskDataRecord();

        CreateTask(identity, priority, workPacket, state, (Packet workArg, RBObject wordArg) =>
        {
            var dataRecord = (DeviceTaskDataRecord)wordArg;
            Packet functionWork = workArg;
            if (NO_WORK == functionWork)
            {
                if (NO_WORK == (functionWork = dataRecord.Pending))
                {
                    return MarkWaiting();
                }
                else
                {
                    dataRecord.Pending = NO_WORK;
                    return QueuePacket(functionWork);
                }
            }
            else
            {
                dataRecord.Pending = functionWork;

#pragma warning disable CS0162 // Unreachable code detected
                if (TRACING)
                    Trace(functionWork.Datum);
#pragma warning restore CS0162 // Unreachable code detected

                return HoldSelf();
            }
        },
        data);
    }

    internal void CreateHandler(int identity, int priority, Packet workPaket, TaskState state)
    {
        var data = new HandlerTaskDataRecord();
        CreateTask(identity, priority, workPaket, state, (Packet work, RBObject word) =>
        {
            var dataRecord = (HandlerTaskDataRecord)word;
            if (NO_WORK != work)
            {
                if (WORK_PACKET_KIND == work.Kind)
                {
                    dataRecord.WorkInAdd(work);
                }
                else
                {
                    dataRecord.DeviceInAdd(work);
                }
            }
            Packet workPacket = dataRecord.WorkIn;
            if (workPacket == NO_WORK)
            {
                return MarkWaiting();
            }
            else
            {
                var count = workPacket.Datum;
                if (count >= Packet.DATA_SIZE)
                {
                    dataRecord.WorkIn = workPacket.Link;
                    return QueuePacket(workPacket);
                }
                else
                {
                    Packet devicePacket = dataRecord.DeviceIn;
                    if (devicePacket == NO_WORK)
                    {
                        return MarkWaiting();
                    }
                    else
                    {
                        dataRecord.DeviceIn = devicePacket.Link;
                        devicePacket.Datum = workPacket.Data[count];
                        workPacket.Datum = count + 1;
                        return QueuePacket(devicePacket);
                    }
                }
            }
        },
        data);
    }

    internal void CreateIdler(int identity, int priority, Packet work, TaskState state)
    {
        var data = new IdleTaskDataRecord();
        CreateTask(identity, priority, work, state, (Packet workArg, RBObject wordArg) =>
        {
            var dataRecord = (IdleTaskDataRecord)wordArg;
            dataRecord.Count--;
            if (dataRecord.Count == 0)
            {
                return HoldSelf();
            }
            else
            {
                if ((dataRecord.Control & 1) == 0)
                {
                    dataRecord.Control /= 2;
                    return Release(DEVICE_A);
                }
                else
                {
                    dataRecord.Control = (dataRecord.Control / 2) ^ 53256;
                    return Release(DEVICE_B);
                }
            }
        },
        data);
    }

    internal static Packet CreatePacket(Packet link, int identity, int kind)
    {
        return new Packet(link, identity, kind);
    }

    internal void CreateTask(int identity, int priority, Packet work, TaskState state, ProcessFunction aBlock, RBObject data)
    {

        var t = new TaskControlBlock(taskList, identity, priority, work, state, aBlock, data);
        taskList = t;
        taskTable[identity] = t;
    }

    internal void CreateWorker(int identity, int priority, Packet workPaket, TaskState state)
    {
        var dataRecord = new WorkerTaskDataRecord();
        CreateTask(identity, priority, workPaket, state, (Packet work, RBObject word) =>
        {
            var data = (WorkerTaskDataRecord)word;
            if (NO_WORK == work)
            {
                return MarkWaiting();
            }
            else
            {
                data.Destination = (HANDLER_A == data.Destination) ? HANDLER_B : HANDLER_A;
                work.Identity = data.Destination;
                work.Datum = 0;
                for (var i = 0; i < Packet.DATA_SIZE; i++)
                {
                    data.Count++;
                    if (data.Count > 26)
                    {
                        data.Count = 1;
                    }
                    work.Data[i] = 65 + data.Count - 1;
                }
                return QueuePacket(work);
            }
        },
        dataRecord);
    }

    public bool Start()
    {
        Packet workQ;

        CreateIdler(IDLER, 0, NO_WORK, TaskState.CreateRunning());
        workQ = CreatePacket(NO_WORK, WORKER, WORK_PACKET_KIND);
        workQ = CreatePacket(workQ, WORKER, WORK_PACKET_KIND);

        CreateWorker(WORKER, 1000, workQ, TaskState.CreateWaitingWithPacket());
        workQ = CreatePacket(NO_WORK, DEVICE_A, DEVICE_PACKET_KIND);
        workQ = CreatePacket(workQ, DEVICE_A, DEVICE_PACKET_KIND);
        workQ = CreatePacket(workQ, DEVICE_A, DEVICE_PACKET_KIND);

        CreateHandler(HANDLER_A, 2000, workQ, TaskState.CreateWaitingWithPacket());
        workQ = CreatePacket(NO_WORK, DEVICE_B, DEVICE_PACKET_KIND);
        workQ = CreatePacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);
        workQ = CreatePacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);

        CreateHandler(HANDLER_B, 3000, workQ, TaskState.CreateWaitingWithPacket());
        CreateDevice(DEVICE_A, 4000, NO_WORK, TaskState.CreateWaiting());
        CreateDevice(DEVICE_B, 5000, NO_WORK, TaskState.CreateWaiting());

        Schedule();

        return queuePacketCount == 23246 && holdCount == 9297;
    }

    internal TaskControlBlock FindTask(int identity)
    {
        TaskControlBlock t = taskTable[identity];
        return NO_TASK == t ? throw new Exception("findTask failed") : t;
    }

    internal TaskControlBlock HoldSelf()
    {
        holdCount++;
        currentTask!.IsTaskHolding = true;
        return currentTask.Link;
    }

    internal TaskControlBlock QueuePacket(Packet packet)
    {
        Debug.Assert(currentTask != null);
        TaskControlBlock t = FindTask(packet.Identity);
        if (t == NO_TASK)
        {
            return NO_TASK;
        }

        queuePacketCount++;

        packet.Link = NO_WORK;
        packet.Identity = currentTaskIdentity;
        return t.AddInputAndCheckPriority(packet, currentTask);
    }

    internal TaskControlBlock Release(int identity)
    {
        Debug.Assert(currentTask != null);
        TaskControlBlock t = FindTask(identity);
        if (NO_TASK == t)
        {
            return NO_TASK;
        }
        t.IsTaskHolding = false;
        return t.Priority > currentTask.Priority ? t : currentTask;
    }

    internal void Trace(int id)
    {
        layout--;
        if (layout <= 0)
        {
            Console.WriteLine();
            layout = 50;
        }
        Console.Write(id);
    }

    internal TaskControlBlock MarkWaiting()
    {
        Debug.Assert(currentTask != null);
        currentTask.IsTaskWaiting = true;
        return currentTask;
    }

    internal void Schedule()
    {
        currentTask = taskList;
        while (NO_TASK != currentTask)
        {
            if (currentTask.IsTaskHoldingOrWaiting)
            {
                currentTask = currentTask.Link;
            }
            else
            {
                currentTaskIdentity = currentTask.Identity;

#pragma warning disable CS0162 // Unreachable code detected
                if (TRACING)
                    Trace(currentTaskIdentity);
#pragma warning restore CS0162 // Unreachable code detected

                currentTask = currentTask.RunTask();
            }
        }
    }

}

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
