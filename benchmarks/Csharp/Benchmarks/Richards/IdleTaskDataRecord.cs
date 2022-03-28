namespace AreWeFastYet;

internal sealed class IdleTaskDataRecord : RBObject
{
    public int Control { get; set; }

    public int Count { get; set; }

    internal IdleTaskDataRecord()
    {
        Control = 1;
        Count = 10000;
    }
}
