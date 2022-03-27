#define TRACE
using System.Diagnostics;

namespace Harness.Benchmarks.Rich;

sealed class Scheduler : RBObject
{
    private TaskControlBlock taskList;
    private TaskControlBlock? currentTask;
    private int currentTaskIdentity;
    private readonly TaskControlBlock[] taskTable;

    private int queuePacketCount;
    private int holdCount;

    private int layout;

    // private const bool TRACING = false
    // Just define the TRACE symbol

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


    internal void createDevice(int identity, int priority, Packet workPacket, TaskState state)
    {
        DeviceTaskDataRecord data = new DeviceTaskDataRecord();

        createTask(identity, priority, workPacket, state, (Packet workArg, RBObject wordArg) =>
        {
            DeviceTaskDataRecord dataRecord = (DeviceTaskDataRecord)wordArg;
            Packet functionWork = workArg;
            if (NO_WORK == functionWork)
            {
                if (NO_WORK == (functionWork = dataRecord.Pending))
                {
                    return markWaiting();
                }
                else
                {
                    dataRecord.Pending = NO_WORK;
                    return queuePacket(functionWork);
                }
            }
            else
            {
                dataRecord.Pending = functionWork;
                trace(functionWork.Datum);
                return holdSelf();
            }
        }, data);
    }

    internal void createHandler(int identity, int priority, Packet workPaket, TaskState state)
    {
        HandlerTaskDataRecord data = new HandlerTaskDataRecord();
        createTask(identity, priority, workPaket, state, (Packet work, RBObject word) =>
    {
        HandlerTaskDataRecord dataRecord = (HandlerTaskDataRecord)word;
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
            return markWaiting();
        }
        else
        {
            var count = workPacket.Datum;
            if (count >= Packet.DATA_SIZE)
            {
                dataRecord.WorkIn = workPacket.Link;
                return queuePacket(workPacket);
            }
            else
            {
                Packet devicePacket = dataRecord.DeviceIn;
                if (devicePacket == NO_WORK)
                {
                    return markWaiting();
                }
                else
                {
                    dataRecord.DeviceIn = devicePacket.Link;
                    devicePacket.Datum = workPacket.Data[count];
                    workPacket.Datum = count + 1;
                    return queuePacket(devicePacket);
                }
            }
        }
    }, data);
    }

    internal void createIdler(int identity, int priority, Packet work, TaskState state)
    {
        IdleTaskDataRecord data = new IdleTaskDataRecord();
        createTask(identity, priority, work, state, (Packet workArg, RBObject wordArg) =>
        {
            var dataRecord = (IdleTaskDataRecord)wordArg;
            dataRecord.Count--;
            if (dataRecord.Count == 0)
            {
                return holdSelf();
            }
            else
            {
                if ((dataRecord.Control & 1) == 0)
                {
                    dataRecord.Control /= 2;
                    return release(DEVICE_A);
                }
                else
                {
                    dataRecord.Control = (dataRecord.Control / 2) ^ 53256;
                    return release(DEVICE_B);
                }
            }
        }, data);
    }

    internal Packet createPacket(Packet link, int identity, int kind)
    {
        return new Packet(link, identity, kind);
    }

    internal void createTask(int identity, int priority, Packet work, TaskState state, ProcessFunction aBlock, RBObject data)
    {

        TaskControlBlock t = new TaskControlBlock(taskList, identity, priority, work, state, aBlock, data);
        taskList = t;
        taskTable[identity] = t;
    }

    internal void createWorker(int identity, int priority, Packet workPaket, TaskState state)
    {
        var dataRecord = new WorkerTaskDataRecord();
        createTask(identity, priority, workPaket, state, (Packet work, RBObject word) =>
            {
                var data = (WorkerTaskDataRecord)word;
                if (NO_WORK == work)
                {
                    return markWaiting();
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
                    return queuePacket(work);
                }
            },
        dataRecord);
    }

    public bool start()
    {
        Packet workQ;

        createIdler(IDLER, 0, NO_WORK, TaskState.createRunning());
        workQ = createPacket(NO_WORK, WORKER, WORK_PACKET_KIND);
        workQ = createPacket(workQ, WORKER, WORK_PACKET_KIND);

        createWorker(WORKER, 1000, workQ, TaskState.createWaitingWithPacket());
        workQ = createPacket(NO_WORK, DEVICE_A, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ, DEVICE_A, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ, DEVICE_A, DEVICE_PACKET_KIND);

        createHandler(HANDLER_A, 2000, workQ, TaskState.createWaitingWithPacket());
        workQ = createPacket(NO_WORK, DEVICE_B, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);

        createHandler(HANDLER_B, 3000, workQ, TaskState.createWaitingWithPacket());
        createDevice(DEVICE_A, 4000, NO_WORK, TaskState.createWaiting());
        createDevice(DEVICE_B, 5000, NO_WORK, TaskState.createWaiting());

        schedule();

        return queuePacketCount == 23246 && holdCount == 9297;
    }

    internal TaskControlBlock findTask(int identity)
    {
        TaskControlBlock t = taskTable[identity];
        if (NO_TASK == t)
        {
            throw new Exception("findTask failed");
        }
        return t;
    }

    internal TaskControlBlock holdSelf()
    {
        holdCount = holdCount + 1;
        currentTask!.IsTaskHolding = true;
        return currentTask.Link;
    }

    internal TaskControlBlock queuePacket(Packet packet)
    {
        Debug.Assert(currentTask != null);
        TaskControlBlock t = findTask(packet.Identity);
        if (t == NO_TASK)
        {
            return NO_TASK;
        }

        queuePacketCount++;

        packet.Link = NO_WORK;
        packet.Identity = currentTaskIdentity;
        return t.addInputAndCheckPriority(packet, currentTask);
    }

    internal TaskControlBlock release(int identity)
    {
        Debug.Assert(currentTask != null);
        TaskControlBlock t = findTask(identity);
        if (NO_TASK == t)
        {
            return NO_TASK;
        }
        t.IsTaskHolding = false;
        if (t.Priority > currentTask.Priority)
        {
            return t;
        }
        else
        {
            return currentTask;
        }
    }

    [Conditional("TRACE")]
    internal void trace(int id)
    {
        layout--;
        if (0 >= layout)
        {
            Console.WriteLine();
            layout = 50;
        }
        Console.Write(id);

        //for (var i = 0; i < taskTable.Length; i++)
        //{
        //    TaskControlBlock? item = taskTable[i];
        //    if (item == null)
        //        Console.WriteLine($"idx {i}, no task");
        //    else
        //        Console.WriteLine($"idx {i}, {item}");
        //}
    }

    internal TaskControlBlock markWaiting()
    {
        Debug.Assert(currentTask != null);
        currentTask.IsTaskWaiting = true;
        return currentTask;
    }

    internal void schedule()
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
                trace(currentTaskIdentity);

                currentTask = currentTask.runTask();
            }
        }
    }

}
