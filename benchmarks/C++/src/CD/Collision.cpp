#include "Collision.h"

using namespace std;

namespace CD {
Collision::Collision(shared_ptr<CallSign> aircraftA,
                     shared_ptr<CallSign> aircraftB,
                     shared_ptr<Vector3D> position) {
  _aircraftA = aircraftA;
  _aircraftB = aircraftB;
  _position = position;
}
}  // namespace CD