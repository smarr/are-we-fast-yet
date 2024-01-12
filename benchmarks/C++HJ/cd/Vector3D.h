#ifndef VECTOR3D
#define VECTOR3D

#include <memory>
#include <cmath>
using namespace std;

namespace CD {

    class Vector3D : public enable_shared_from_this<Vector3D> {

        public:
            double _x;
            double _y;
            double _z;

            Vector3D(double x, double y, double z);

            shared_ptr<Vector3D> plus(shared_ptr<Vector3D> other) const;
            shared_ptr<Vector3D> minus(shared_ptr<Vector3D> other) const;
            double dot(shared_ptr<Vector3D> other);
            double squaredMagnitude();
            double magnitude();
            shared_ptr<Vector3D> times(double amount) const;
    };
}

#endif //VECTOR3D