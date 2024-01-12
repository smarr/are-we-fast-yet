#ifndef VECTOR2D
#define VECTOR2D

#include <memory>
using namespace std;

namespace CD {

    class Vector2D {
        private: 

            static int compareNumbers(double a, double b);

        public:
            double _x;
            double _y;

            Vector2D(double x, double y);
            shared_ptr<Vector2D> plus(shared_ptr<Vector2D> other) const;
            shared_ptr<Vector2D> minus(shared_ptr<Vector2D> other) const;
            int compareTo(shared_ptr<Vector2D> other);
    };
}

#endif //VECTOR2D