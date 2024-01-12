#include "Vector3D.h"
#include <iostream>

namespace CD {
        
    Vector3D::Vector3D(double x, double y, double z) {
        _x = x;
        _y = y;
        _z = z;
    }

    shared_ptr<Vector3D> Vector3D::plus(shared_ptr<Vector3D> other) const {
        return make_shared<Vector3D>(_x + other->_x, _y + other->_y, _z + other->_z);
    }

    shared_ptr<Vector3D> Vector3D::minus(shared_ptr<Vector3D> other) const {
        return make_shared<Vector3D>(_x - other->_x, _y - other->_y, _z - other->_z);
    }

    double Vector3D::dot(shared_ptr<Vector3D> other) {
        return _x * other->_x + _y * other->_y + _z * other->_z;
    }

    double Vector3D::squaredMagnitude() {
        return dot(shared_from_this());
    }


    double Vector3D::magnitude() {
        return sqrt(squaredMagnitude());
    }

    shared_ptr<Vector3D> Vector3D::times(double amount) const {
        return make_shared<Vector3D>(_x * amount, _y * amount, _z * amount);
    }
};