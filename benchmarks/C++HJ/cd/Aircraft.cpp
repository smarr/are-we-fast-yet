#include "Aircraft.h"

namespace CD {

    Aircraft::Aircraft(shared_ptr<CallSign> callsign, shared_ptr<Vector3D> position) {
        _callsign = callsign;
        _position = position;
    }
};