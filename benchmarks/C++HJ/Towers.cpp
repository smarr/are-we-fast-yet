#include <utility>
#include <vector>
#include "Benchmark.cpp"
#include "som/Error.cpp"

using namespace std;

class Towers : public Benchmark
{
    private:

        class TowersDisk {

            private: 
                int _size;
                shared_ptr<TowersDisk> _next{};

            public:

                TowersDisk() = default;

                TowersDisk(int size) {
                    _size = size;
                }

                int getSize() const {
                    return _size;
                }

                shared_ptr<TowersDisk> getNext() {
                    return _next;
                }

                void setNext(shared_ptr<TowersDisk> value) {
                    _next = move(value);
                }
        };

        vector<shared_ptr<TowersDisk>> _piles;
        int _movesDone;

        void pushDisk(shared_ptr<TowersDisk> disk, int pile) {
            shared_ptr<TowersDisk> top = _piles[pile];

            if (!(top == nullptr) && (disk->getSize() >= top->getSize())) {
                throw Error("Cannot put a big disk on a smaller one");
            }

            disk->setNext(top);
            _piles[pile] = disk;
        }

        shared_ptr<TowersDisk> popDiskFrom(int pile) {
            shared_ptr<TowersDisk> top = _piles[pile];

            if (top == nullptr) {
                throw Error("Attempting to remove a disk from an empty pile");
            }

            _piles[pile] = top->getNext();
            top->setNext(nullptr);
            return top;
        }

        void moveTopDisk(int fromPile, int toPile) {
            pushDisk(popDiskFrom(fromPile), toPile);
            _movesDone++;
        }

        void buildTowerAt(int pile, int disks) {
            for (int i = disks; i >= 0; i--) {
                pushDisk(make_shared<TowersDisk>(i), pile);
            }
        }

        void moveDisks(int disks, int fromPile, int toPile) {
            if(disks == 1) {
                moveTopDisk(fromPile, toPile);
            } else {
                int otherPile = (3 - fromPile) - toPile;
                moveDisks(disks - 1, fromPile, otherPile);
                moveTopDisk(fromPile, toPile);
                moveDisks(disks - 1, otherPile, toPile);
            }
        }

    public:
        any benchmark() override {
            _piles = vector<shared_ptr<TowersDisk>>(3);
            buildTowerAt(0, 13);
            _movesDone = 0;
            moveDisks(13, 0 ,1);

            return _movesDone;
        }

        bool verifyResult(any result) override {
            int result_cast = any_cast<int>(result);
            return 8191 == result_cast;
        }
};