#ifndef AIRCRAFT
#define AIRCRAFT

#include <memory>
#include "CallSign.h"
#include "Vector3D.h"
using namespace std;

namespace CD {
class Aircraft {
 public:
  shared_ptr<CallSign> _callsign;
  shared_ptr<Vector3D> _position;

  Aircraft(shared_ptr<CallSign> callsign, shared_ptr<Vector3D> position);
};
};  // namespace CD

#endif  // AIRCRAFT