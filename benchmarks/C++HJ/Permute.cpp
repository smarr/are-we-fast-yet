#include <vector>
#include "Benchmark.cpp"

using namespace std;

class Permute : public Benchmark
{
    private:
        int _count;
        //int _v[6];
        int *_v;


        void swap(int i, int j) {
            int tmp = _v[i];
            _v[i] = _v[j];
            _v[j] = tmp;
        }

    public:
        any benchmark() override {
            _count = 0;
            _v = new int[6];
            permute(6);
            delete[] _v;
            return _count;
        }

        void permute(int n) {
            _count++;
            if (n != 0) {
                int n1 = n - 1;
                permute(n1);
                for (int i = n1; i >= 0; i--) {
                    swap(n1, i);
                    permute(n1);
                    swap(n1, i);
                }
            }
        }
    
        bool verifyResult(any result) override {
            int result_cast = any_cast<int>(result);
            return result_cast == 8660;
        }
};