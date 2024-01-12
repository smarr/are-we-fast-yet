#ifndef BODY
#define BODY

#include <memory>

using namespace std;

namespace nbody {
    class Body {
        private:
            constexpr static double PI = 3.141592653589793;
            constexpr static double SOLAR_MASS = 4 * PI * PI;
            constexpr static double DAYS_PER_YER = 365.24;

            double _x;
            double _y;
            double _z;
            double _vx;
            double _vy;
            double _vz;
            double _mass;

        public:

            double getX();
            double getY();
            double getZ();

            double getVX();
            double getVY();
            double getVZ();

            double getMass();

            void setX(double x);
            void setY(double y);
            void setZ(double z);

            void setVX(double vx);
            void setVY(double vy);
            void setVZ(double vz);

            void offsetMomentum(double px, double py, double pz);
            Body(double x, double y, double z,
                double vx, double vy, double vz, double mass);

            static shared_ptr<Body> jupiter();
            static shared_ptr<Body> saturn();
            static shared_ptr<Body> uranus();
            static shared_ptr<Body> neptune();
            static shared_ptr<Body> sun();
    };
}

#endif //BODY