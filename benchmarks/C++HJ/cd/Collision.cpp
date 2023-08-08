#include "CallSign.h"
#include "Vector3D.h"
#include <memory>
using namespace std;

namespace CD {
    class Collision {
        public:
            shared_ptr<CallSign> _aircraftA;
            shared_ptr<CallSign> _aircraftB;
            shared_ptr<Vector3D> _position;

            Collision(shared_ptr<CallSign> aircraftA, shared_ptr<CallSign> aircraftB, shared_ptr<Vector3D> position) {
                _aircraftA = aircraftA;
                _aircraftB = aircraftB;
                _position = position;
            }
    };
};