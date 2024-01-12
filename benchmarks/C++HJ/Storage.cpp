#include <vector>
#include "Benchmark.cpp"
#include "som/Random.cpp"

using namespace std;

class Storage : public Benchmark
{
    private:
        int _count;

        vector<void> buildTreeDepth(int depth, Random random) {
            _count++;

            if (depth == 1) {
                vector<void> node = vector<void>(random.next() % 10 + 1);
                return node;
            } else {
                vector<void> arr = vector<void>(4);;

                for(int i = 0; i < 4; i++) {
                    arr[i] = buildTreeDepth(depth - 1, random);
                }
                return arr;
            }
        }

    public:
        any benchmark() override {
            Random random = Random();
            _count = 0;
            buildTreeDepth(7, random);
            return _count;
        }

        bool verifyResult(any result) override {
            bool result_cast = any_cast<int>(result);
            return 5461 == result_cast;
        }
};

