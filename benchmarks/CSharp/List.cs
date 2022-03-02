using System;
using System.Linq;
using System.Collections;

public class List : Benchmark
{
    public class Element {

        public Object Val;

        public Element? Next;

        public Element(Object v) {
            Val = v;
        }

        public int length(){
            if(Next == null){
                return 1;
            }
            else{
                return 1 + Next.length();
            }
        }
    }

    public static void Main(string[] args){
        List s = new List();
    Object? result = s.benchmark();
    Console.WriteLine(result);
    bool rr = s.verifyResult(result);
    Console.WriteLine(rr);
    }

    public override Object? benchmark(){
        Element? result = tail(makeList(15), makeList(10), makeList(6));
        return result?.length();
    }

    public Element? makeList(int length){
        if(length ==0){
            return null;
        }
        else{
            Element e = new Element(length);
            e.Next = makeList(length-1);
            return e;
        }
    }

    public bool isShorterThan(Element? x, Element? y){
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

    public Element? tail(Element? x, Element? y, Element? z){
        if(isShorterThan(y,x)){
            return tail(tail(x.Next, y, z), tail(y.Next, z, x), tail(z.Next, x, y));
        }
        else{
            return z;
        }
    }

    public override bool verifyResult(Object? result){
        return 10 == (int?) result;
    }
}
