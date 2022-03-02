using System;
using System.Collections;

public class Storage : Benchmark
{
    int count;

    public static void Main(string[] args){
        Storage s = new Storage();
        Object result = s.benchmark();
        bool rr = s.verifyResult(result);
    }

    public override Object benchmark(){
        Random random = new Random();
        count =0;
        buildTreeDepth(7, random);
        return count;
    }

    public Object buildTreeDepth(int depth, Random random){
        count++;
        if(depth ==1){
            return new Object[random.Next() % 10 + 1];
        }
        else{
            Object[] arr = new Object[4];
            for (int i = 0; i < 4; i++)
        {
            arr[i] = this.buildTreeDepth(depth - 1, random);
        }
            return arr;
        }
    }

    public override bool verifyResult(Object result){
        return 5461 == (int) result;
    }
}