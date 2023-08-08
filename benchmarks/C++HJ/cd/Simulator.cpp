#include <cmath>
#include "../som/Vector.cpp"
#include "Aircraft.h"
using namespace std;

namespace CD {
    class Simulator {
        private :
            shared_ptr<Vector<shared_ptr<CallSign>>> _aircraft;

            public:

            Simulator(int numAircraft) {
                _aircraft = make_shared<Vector<shared_ptr<CallSign>>>();

                for (int i = 0; i < numAircraft; i++) {
                    _aircraft->append(make_shared<CallSign>(i));
                }
            }

            shared_ptr<Vector<shared_ptr<Aircraft>>> simulate(double time) {
                shared_ptr<Vector<shared_ptr<Aircraft>>> frame = make_shared<Vector<shared_ptr<Aircraft>>>();

                for (unsigned long int i = 0; i < _aircraft->size(); i+=2) {
                    frame->append(make_shared<Aircraft>(_aircraft->atPtr(i), make_shared<Vector3D>(time, cos(time) * 2 + i * 3, 10)));
                    frame->append(make_shared<Aircraft>(_aircraft->atPtr(i + 1), make_shared<Vector3D>(time, sin(time) * 2 + i * 3, 10)));
                }
                return frame;
            }
    };
};
