namespace Benchmarks;

public class List : Benchmark
{
    public class Element {

        public object Val { get; set; }

        public Element? Next { get; set; }

        public Element(object v) {
            Val = v;
        }

        public int Length(){
            if (Next == null) {
                return 1;
            } else {
                return 1 + Next.Length();
            }
        }
    }

    public override object Execute() {
        Element? result = Tail(MakeList(15), MakeList(10), MakeList(6));
        return result!.Length();
    }

    public Element? MakeList(int length){
        if(length ==0){
            return null;
        }
        else{
            Element e = new Element(length);
            e.Next = MakeList(length-1);
            return e;
        }
    }

    public bool IsShorterThan(Element? x, Element? y){
        Element? xTail = x;
        Element? yTail = y;

        while(yTail != null){
            if(xTail == null){
                return true;
            }
            xTail = xTail.Next;
            yTail = yTail.Next;
        }
        return false;
    }

    public Element? Tail(Element? x, Element? y, Element? z){
        if(IsShorterThan(y,x)){
            return Tail(Tail(x?.Next, y, z), Tail(y?.Next, z, x), Tail(z?.Next, x, y));
        }
        else{
            return z;
        }
    }

    public override bool VerifyResult(object result){
        return 10 == (int?) result;
    }
}