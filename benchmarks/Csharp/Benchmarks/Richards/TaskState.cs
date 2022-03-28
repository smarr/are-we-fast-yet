namespace AreWeFastYet;

class TaskState : RBObject
{
    public virtual bool IsPacketPending { get; set; }
    public virtual bool IsTaskHolding { get; set; }
    public virtual bool IsTaskWaiting { get; set; }

    public virtual void SetPacketPending()
    {
        IsPacketPending = true;
        IsTaskWaiting = false;
        IsTaskHolding = false;
    }

    public virtual void SetRunning()
    {
        IsPacketPending = IsTaskWaiting = IsTaskHolding = false;
    }

    public virtual void SetWaiting()
    {
        IsPacketPending = IsTaskHolding = false;
        IsTaskWaiting = true;
    }

    public virtual void SetWaitingWithPacket()
    {
        IsTaskHolding = false;
        IsTaskWaiting = IsPacketPending = true;
    }

    public virtual bool IsRunning
        => !IsPacketPending && !IsTaskWaiting && !IsTaskHolding;

    public virtual bool IsTaskHoldingOrWaiting
        => IsTaskHolding || (!IsPacketPending && IsTaskWaiting);

    public virtual bool IsWaiting
        => !IsPacketPending && IsTaskWaiting && !IsTaskHolding;

    public virtual bool IsWaitingWithPacket
        => IsPacketPending && IsTaskWaiting && !IsTaskHolding;

    public static TaskState CreatePacketPending()
    {
        var t = new TaskState();
        t.SetPacketPending();
        return t;
    }

    public static TaskState CreateRunning()
    {
        var t = new TaskState();
        t.SetRunning();
        return t;
    }

    public static TaskState CreateWaiting()
    {
        var t = new TaskState();
        t.SetWaiting();
        return t;
    }

    public static TaskState CreateWaitingWithPacket()
    {
        var t = new TaskState();
        t.SetWaitingWithPacket();
        return t;
    }
}
