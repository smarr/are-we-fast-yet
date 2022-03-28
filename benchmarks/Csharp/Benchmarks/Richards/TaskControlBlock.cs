namespace AreWeFastYet;

delegate TaskControlBlock ProcessFunction(Packet work, RBObject word);

sealed class TaskControlBlock : TaskState
{
    private Packet input;
    private readonly ProcessFunction function;
    private readonly RBObject handle;

    internal TaskControlBlock(TaskControlBlock aLink, int anIdentity, int aPriority, Packet anInitialWorkQueue, TaskState anInitialState, ProcessFunction aBlock, RBObject aPrivateData)
    {
        Link = aLink;
        Identity = anIdentity;
        Priority = aPriority;
        input = anInitialWorkQueue;
        IsPacketPending = anInitialState.IsPacketPending;
        IsTaskWaiting = anInitialState.IsTaskWaiting;
        IsTaskHolding = anInitialState.IsTaskHolding;
        function = aBlock;
        handle = aPrivateData;
    }

    public int Identity { get; }
    public TaskControlBlock Link { get; }
    public int Priority { get; }

    public TaskControlBlock AddInputAndCheckPriority(Packet packet, TaskControlBlock oldTask)
    {
        if (NO_WORK == input)
        {
            input = packet;
            IsPacketPending = true;
            if (Priority > oldTask.Priority)
                return this;
        }
        else
        {
            input = Append(packet, input);
        }
        return oldTask;
    }

    public TaskControlBlock RunTask()
    {
        Packet message = NO_WORK;
        if (IsWaitingWithPacket)
        {
            message = input;
            input = message.Link;
            if (NO_WORK == input)
                SetRunning();
            else            
                SetPacketPending();
        }
        return function(message, handle);
    }
}
