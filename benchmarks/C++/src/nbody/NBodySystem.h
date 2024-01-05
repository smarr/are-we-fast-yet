#ifndef NBODYSYSTEM
#define NBODYSYSTEM

#include "Body.h"
#include <cmath>

using namespace std;

namespace nbody {
    class NBodySystem {
        private:
            shared_ptr<Body>* _bodies;
            int _bodiesSize;

        public: 
            NBodySystem();

            shared_ptr<Body>*  createBodies();
            void advance(double dt);
            double energy();
    };
}

#endif //NBODYSYSTEM