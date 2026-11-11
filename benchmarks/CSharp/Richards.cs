namespace Benchmarks;

internal sealed class Richards : Benchmark
{
    public override object Execute() => new Scheduler().Start();

    public override bool VerifyResult(object result) => (bool)result;
}

internal sealed class DeviceTaskDataRecord : RbObject
{
    internal DeviceTaskDataRecord()
    {
        Pending = NoWork;
    }

    public Packet? Pending { get; set; }
}

internal sealed class HandlerTaskDataRecord : RbObject
{
    public HandlerTaskDataRecord()
    {
        WorkIn = DeviceIn = NoWork;
    }

    public Packet? DeviceIn { get; set; }

    public void DeviceInAdd(Packet packet) => DeviceIn = Append(packet, DeviceIn);

    public Packet? WorkIn { get; set; }

    public void WorkInAdd(Packet packet) => WorkIn = Append(packet, WorkIn);
}

internal sealed class IdleTaskDataRecord : RbObject
{
    public int Control { get; set; }

    public int Count { get; set; }

    internal IdleTaskDataRecord()
    {
        Control = 1;
        Count = 10000;
    }
}

internal sealed class Packet : RbObject
{
    public const int DataSize = 4;

    public Packet(Packet? link, int identity, int kind)
    {
        Link = link;
        Identity = identity;
        Kind = kind;
        Datum = 0;
        Data = new int[DataSize];
    }

    public int[] Data { get; }

    public int Datum { get; set; }

    public int Identity { get; set; }

    public int Kind { get; }

    public Packet? Link { get; set; }

    public override string ToString() => $"Packet id: {Identity} kind: {Kind}";
}

internal abstract class RbObject
{
    public Packet Append(Packet packet, Packet? queueHead)
    {
        packet.Link = NoWork;
        if (NoWork == queueHead)
        {
            return packet;
        }

        Packet mouse = queueHead;
        Packet? link;
        while (NoWork != (link = mouse.Link))
        {
            mouse = link;
        }

        mouse.Link = packet;
        return queueHead;
    }

    public const int Idler = 0;
    public const int Worker = 1;
    public const int HandlerA = 2;
    public const int HandlerB = 3;
    public const int DeviceA = 4;
    public const int DeviceB = 5;
    public const int NumTypes = 6;

    public const int DevicePacketKind = 0;
    public const int WorkPacketKind = 1;

    public const Packet? NoWork = null;

    public const TaskControlBlock? NoTask = null;
}

internal sealed class Scheduler : RbObject
{
    private TaskControlBlock? _taskList;
    private TaskControlBlock? _currentTask;
    private int _currentTaskIdentity;
    private readonly TaskControlBlock?[] _taskTable;

    private int _queuePacketCount;
    private int _holdCount;

    private int _layout;

    private const bool Tracing = false;

    public Scheduler()
    {
        // init tracing
        _layout = 0;

        // init scheduler
        _queuePacketCount = 0;
        _holdCount = 0;
        _taskTable = new TaskControlBlock[NumTypes];
        Array.Fill(_taskTable, NoTask);
        _taskList = NoTask;
    }


    internal void CreateDevice(int identity, int priority, Packet? workPacket, TaskState state)
    {
        DeviceTaskDataRecord data = new();

        CreateTask(identity, priority, workPacket, state, (workArg, wordArg) =>
        {
            DeviceTaskDataRecord? dataRecord = (DeviceTaskDataRecord?)wordArg;
            Packet? functionWork = workArg;
            if (NoWork == functionWork)
            {
                if (NoWork == (functionWork = dataRecord!.Pending))
                {
                    return MarkWaiting();
                }

                dataRecord.Pending = NoWork;
                return QueuePacket(functionWork);
            }

            dataRecord!.Pending = functionWork;

#pragma warning disable CS0162
            if (Tracing)
            {
                Trace(functionWork.Datum);
            }
#pragma warning restore CS0162

            return HoldSelf();
        },
            data);
    }

    internal void CreateHandler(int identity, int priority, Packet? workPaket, TaskState state)
    {
        HandlerTaskDataRecord data = new();
        CreateTask(identity, priority, workPaket, state, (work, word) =>
        {
            HandlerTaskDataRecord? dataRecord = (HandlerTaskDataRecord?)word;
            if (NoWork != work)
            {
                if (WorkPacketKind == work.Kind)
                {
                    dataRecord!.WorkInAdd(work);
                }
                else
                {
                    dataRecord!.DeviceInAdd(work);
                }
            }

            Packet? workPacket = dataRecord!.WorkIn;
            if (workPacket == NoWork)
            {
                return MarkWaiting();
            }

            int count = workPacket.Datum;
            if (count >= Packet.DataSize)
            {
                dataRecord.WorkIn = workPacket.Link;
                return QueuePacket(workPacket);
            }

            Packet? devicePacket = dataRecord.DeviceIn;
            if (devicePacket == NoWork)
            {
                return MarkWaiting();
            }

            dataRecord.DeviceIn = devicePacket.Link;
            devicePacket.Datum = workPacket.Data[count];
            workPacket.Datum = count + 1;
            return QueuePacket(devicePacket);
        },
            data);
    }

    internal void CreateIdler(int identity, int priority, Packet? work, TaskState state)
    {
        IdleTaskDataRecord data = new();
        CreateTask(identity, priority, work, state, (_, wordArg) =>
        {
            IdleTaskDataRecord? dataRecord = (IdleTaskDataRecord?)wordArg;
            dataRecord!.Count--;
            if (dataRecord.Count == 0)
            {
                return HoldSelf();
            }

            if ((dataRecord.Control & 1) == 0)
            {
                dataRecord.Control /= 2;
                return Release(DeviceA);
            }

            dataRecord.Control = (dataRecord.Control / 2) ^ 53256;
            return Release(DeviceB);
        },
            data);
    }

    internal Packet CreatePacket(Packet? link, int identity, int kind)
    {
        return new Packet(link, identity, kind);
    }

    internal void CreateTask(int identity, int priority, Packet? work, TaskState state, Func<Packet?, RbObject?, TaskControlBlock?> aBlock,
        RbObject data)
    {
        TaskControlBlock t = new(_taskList!, identity, priority, work, state, aBlock, data);
        _taskList = t;
        _taskTable[identity] = t;
    }

    internal void CreateWorker(int identity, int priority, Packet? workPaket, TaskState state)
    {
        WorkerTaskDataRecord dataRecord = new();
        CreateTask(identity, priority, workPaket, state, (work, word) =>
        {
            WorkerTaskDataRecord? data = (WorkerTaskDataRecord?)word;
            if (NoWork == work)
            {
                return MarkWaiting();
            }

            data!.Destination = HandlerA == data.Destination ? HandlerB : HandlerA;
            work.Identity = data.Destination;
            work.Datum = 0;
            for (int i = 0; i < Packet.DataSize; i++)
            {
                data.Count++;
                if (data.Count > 26)
                {
                    data.Count = 1;
                }

                work.Data[i] = 65 + data.Count - 1;
            }

            return QueuePacket(work);
        },
            dataRecord);
    }

    public bool Start()
    {
        CreateIdler(Idler, 0, NoWork, TaskState.CreateRunning());
        Packet workQ = CreatePacket(NoWork, Worker, WorkPacketKind);
        workQ = CreatePacket(workQ, Worker, WorkPacketKind);

        CreateWorker(Worker, 1000, workQ, TaskState.CreateWaitingWithPacket());
        workQ = CreatePacket(NoWork, DeviceA, DevicePacketKind);
        workQ = CreatePacket(workQ, DeviceA, DevicePacketKind);
        workQ = CreatePacket(workQ, DeviceA, DevicePacketKind);

        CreateHandler(HandlerA, 2000, workQ, TaskState.CreateWaitingWithPacket());
        workQ = CreatePacket(NoWork, DeviceB, DevicePacketKind);
        workQ = CreatePacket(workQ, DeviceB, DevicePacketKind);
        workQ = CreatePacket(workQ, DeviceB, DevicePacketKind);

        CreateHandler(HandlerB, 3000, workQ, TaskState.CreateWaitingWithPacket());
        CreateDevice(DeviceA, 4000, NoWork, TaskState.CreateWaiting());
        CreateDevice(DeviceB, 5000, NoWork, TaskState.CreateWaiting());

        Schedule();

        return _queuePacketCount == 23246 && _holdCount == 9297;
    }

    internal TaskControlBlock FindTask(int identity)
    {
        TaskControlBlock? t = _taskTable[identity];
        return NoTask == t ? throw new InvalidOperationException("findTask failed") : t;
    }

    internal TaskControlBlock HoldSelf()
    {
        _holdCount++;
        _currentTask!.IsTaskHolding = true;
        return _currentTask.Link;
    }

    internal TaskControlBlock? QueuePacket(Packet packet)
    {
        TaskControlBlock t = FindTask(packet.Identity);
        if (t == NoTask)
        {
            return NoTask;
        }

        _queuePacketCount++;

        packet.Link = NoWork;
        packet.Identity = _currentTaskIdentity;
        return t.AddInputAndCheckPriority(packet, _currentTask);
    }

    internal TaskControlBlock? Release(int identity)
    {
        TaskControlBlock t = FindTask(identity);
        if (NoTask == t)
        {
            return NoTask;
        }

        t.IsTaskHolding = false;
        return t.Priority > _currentTask!.Priority ? t : _currentTask;
    }

    internal void Trace(int id)
    {
        _layout--;
        if (_layout <= 0)
        {
            Console.WriteLine();
            _layout = 50;
        }

        Console.Write(id);
    }

    internal TaskControlBlock MarkWaiting()
    {
        _currentTask!.IsTaskWaiting = true;
        return _currentTask;
    }

    internal void Schedule()
    {
        _currentTask = _taskList;
        while (NoTask != _currentTask)
        {
            if (_currentTask.IsTaskHoldingOrWaiting)
            {
                _currentTask = _currentTask.Link;
            }
            else
            {
                _currentTaskIdentity = _currentTask.Identity;


#pragma warning disable CS0162
                // ReSharper disable HeuristicUnreachableCode
                if (Tracing)
                {
                    Trace(_currentTaskIdentity);
                }
                // ReSharper restore HeuristicUnreachableCode
#pragma warning restore CS0162

                _currentTask = _currentTask.RunTask();
            }
        }
    }
}

internal sealed class TaskControlBlock : TaskState
{
    private Packet? _input;
    private readonly Func<Packet?, RbObject?, TaskControlBlock?> _function;
    private readonly RbObject _handle;

    internal TaskControlBlock(TaskControlBlock aLink, int anIdentity, int aPriority, Packet? anInitialWorkQueue,
        TaskState anInitialState, Func<Packet?, RbObject?, TaskControlBlock?> aBlock, RbObject aPrivateData)
    {
        Link = aLink;
        Identity = anIdentity;
        Priority = aPriority;
        _input = anInitialWorkQueue;
        IsPacketPending = anInitialState.IsPacketPending;
        IsTaskWaiting = anInitialState.IsTaskWaiting;
        IsTaskHolding = anInitialState.IsTaskHolding;
        _function = aBlock;
        _handle = aPrivateData;
    }

    public int Identity { get; }
    public TaskControlBlock Link { get; }
    public int Priority { get; }

    public TaskControlBlock? AddInputAndCheckPriority(Packet packet, TaskControlBlock? oldTask)
    {
        if (NoWork == _input)
        {
            _input = packet;
            IsPacketPending = true;
            if (Priority > oldTask!.Priority)
            {
                return this;
            }
        }
        else
        {
            _input = Append(packet, _input);
        }

        return oldTask;
    }

    public TaskControlBlock? RunTask()
    {
        Packet? message;
        if (IsWaitingWithPacket)
        {
            message = _input;
            _input = message!.Link;
            if (NoWork == _input)
            {
                SetRunning();
            }
            else
            {
                SetPacketPending();
            }
        }
        else
        {
            message = NoWork;
        }

        return _function(message, _handle);
    }
}

internal class TaskState : RbObject
{
    public bool IsPacketPending { get; set; }
    public bool IsTaskHolding { get; set; }
    public bool IsTaskWaiting { get; set; }

    public void SetPacketPending()
    {
        IsPacketPending = true;
        IsTaskWaiting = false;
        IsTaskHolding = false;
    }

    public void SetRunning()
    {
        IsPacketPending = IsTaskWaiting = IsTaskHolding = false;
    }

    public void SetWaiting()
    {
        IsPacketPending = IsTaskHolding = false;
        IsTaskWaiting = true;
    }

    public void SetWaitingWithPacket()
    {
        IsTaskHolding = false;
        IsTaskWaiting = IsPacketPending = true;
    }

    public bool IsTaskHoldingOrWaiting
        => IsTaskHolding || (!IsPacketPending && IsTaskWaiting);

    public bool IsWaitingWithPacket
        => IsPacketPending && IsTaskWaiting && !IsTaskHolding;

    public static TaskState CreateRunning()
    {
        TaskState t = new();
        t.SetRunning();
        return t;
    }

    public static TaskState CreateWaiting()
    {
        TaskState t = new();
        t.SetWaiting();
        return t;
    }

    public static TaskState CreateWaitingWithPacket()
    {
        TaskState t = new();
        t.SetWaitingWithPacket();
        return t;
    }
}

internal sealed class WorkerTaskDataRecord : RbObject
{
    public int Count { get; set; }

    public int Destination { get; set; } = HandlerA;
}