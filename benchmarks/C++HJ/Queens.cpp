#include <vector>
#include "Benchmark.cpp"
#include <algorithm>

using namespace std;

class Queens : public Benchmark
{
    private:
        bool *_freeMaxs;
        bool *_freeRows;
        bool *_freeMins;
        int *_queenRows;

        bool queens() {
            _freeRows = new bool[8];fill_n(_freeRows, 8, true);
            _freeMaxs = new bool[16];fill_n(_freeMaxs, 16, true);
            _freeMins = new bool[16];fill_n(_freeMins, 16, true);
            _queenRows = new int[8];fill_n(_queenRows, 8, -1);
            bool result = placeQueen(0);
            delete[] _freeMaxs;
            delete[] _freeRows;
            delete[] _freeMins;
            delete[] _queenRows;
            return result;
        }

        bool placeQueen(int c) {
            for (int r = 0; r < 8; r++) {
                if (getRowColumn(r, c)) {
                    _queenRows[r] = c;
                    setRowColumn(r, c, false);

                    if (c == 7) {
                        return true;
                    }

                    if (placeQueen(c + 1)) {
                        return true;
                    }
                    setRowColumn(r, c, true);
                }
            }
            return false;
        }

        bool getRowColumn(int r, int c) {
            return _freeRows[r] && _freeMaxs[c + r] && _freeMins[c - r + 7];
        }

        void setRowColumn(int r, int c, bool v) {
            _freeRows[r] = v;
            _freeMaxs[c + r] = v;
            _freeMins[c - r + 7] = v;
        }


    public:
        any benchmark() override {
            bool result = true;
            for (int i = 0; i < 10; i++) {
                result = result && queens();
            }
            return result;
        }
    
        bool verifyResult(any result) override {
            bool result_cast = any_cast<bool>(result);
            return result_cast;
        }
};