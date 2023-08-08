#ifndef AIRCRAFT
#define AIRCRAFT

#include "CallSign.h"
#include "Vector3D.h"
#include <memory>
using namespace std;

namespace CD {
    class Aircraft{
        public:
            shared_ptr<CallSign> _callsign;
            shared_ptr<Vector3D> _position;

        Aircraft(shared_ptr<CallSign> callsign, shared_ptr<Vector3D> position);
    };
};

#endif //AIRCRAFT