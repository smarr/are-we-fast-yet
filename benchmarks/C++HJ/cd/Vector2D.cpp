#include "Vector2D.h"

namespace CD {
    int Vector2D::compareNumbers(double a, double b) {
        if (a == b) {
            return 0;
        }
        if (a < b) {
            return -1;
        }
        if (a > b) {
            return 1;
        }

        // We say that NaN is smaller than non-NaN.
        if (a == a) {
            return 1;
        }
        return -1;
    }

            

    Vector2D::Vector2D(double x, double y) {
        _x = x;
        _y = y;
    }

    shared_ptr<Vector2D> Vector2D::plus(shared_ptr<Vector2D> other) const {
        return make_shared<Vector2D>(_x + other->_x, _y + other->_y);
    }

    shared_ptr<Vector2D> Vector2D::minus(shared_ptr<Vector2D> other) const {
        return make_shared<Vector2D>(_x - other->_x, _y - other->_y);
    }

    int Vector2D::compareTo(shared_ptr<Vector2D> other) {
        int result = compareNumbers(_x, other->_x);

        if (result != 0) {
            return result;
        }

        return compareNumbers(_y, other->_y);
    }
}