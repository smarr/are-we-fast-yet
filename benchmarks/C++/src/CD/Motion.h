#ifndef MOTION
#define MOTION

#include <memory>
#include "CallSign.h"
#include "Constants.h"
#include "Vector3D.h"

using namespace std;

namespace CD {
class Motion {
 private:
  shared_ptr<Vector3D> delta() const;

 public:
  shared_ptr<CallSign> _callsign;
  shared_ptr<Vector3D> _posOne;
  shared_ptr<Vector3D> _posTwo;

  Motion(shared_ptr<CallSign> callsign,
         shared_ptr<Vector3D> posOne,
         shared_ptr<Vector3D> posTwo);

  shared_ptr<Vector3D> findIntersection(shared_ptr<Motion> other);
};
}  // namespace CD

#endif  // MOTION