namespace AreWeFastYet;

sealed class Packet : RBObject
{
    public const int DATA_SIZE = 4;

    public Packet(Packet link, int identity, int kind)
    {
        Link = link;
        Identity = identity;
        Kind = kind;
        Datum = 0;
        Data = new int[DATA_SIZE];
    }

    public int[] Data { get; }

    public int Datum { get; set; }

    public int Identity { get; set; }

    public int Kind { get; }

    public Packet Link { get; set; }

    public override string ToString()
    {
        return "Packet id: " + Identity + " kind: " + Kind;
    }
}
