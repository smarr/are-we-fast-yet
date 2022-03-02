using System;

public class Permute : Benchmark
{
    int count;
    int[] v;

    public static void Main(string[] args){
        Permute s = new Permute();
        Object result = s.benchmark();
        bool rr = s.verifyResult(result);
    }

    public override Object benchmark(){
        count = 0;
        v = new int[6];
        permute(6);
        return count;
    }

    public void permute(int n){
        count++;
        if(n !=0){
            int n1 = n -1;
            permute(n1);
            for(int i = n1; i >=0; i--){
                swap(n1,i);
                permute(n1);
                swap(n1, i);
            }
        }
    }

    public void swap(int i, int j){
        int tmp = v[i];
        v[i] = v[j];
        v[j] = tmp;
    }

    public override bool verifyResult(Object result){
        return (int) result == 8660;
    }
}