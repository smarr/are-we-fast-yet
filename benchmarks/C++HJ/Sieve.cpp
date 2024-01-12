#include <vector>
#include "Benchmark.cpp"
#include <algorithm>

using namespace std;

class Sieve : public Benchmark
{
    public:
   
    any benchmark() override {
        bool *flags = new bool[5000]; fill_n(flags, 5000, true);
        int result = sieve(flags, 5000);
        delete[] flags;
        return result;
    }

    static int sieve(bool *flags, int size) {
        int primeCount = 0;

        for (int i = 2; i <= size; i++) {
            if (flags[i - 1]) {
                primeCount++;
                int k = i + i;
                while (k <= size) {
                    flags[k - 1] = false;
                    k += i;
                }
            }
        }
        return primeCount;
    }
    
    bool verifyResult(any result) override {
        int result_cast = any_cast<int>(result);
        return 669 == result_cast;
    }
};