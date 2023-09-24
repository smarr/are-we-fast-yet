#ifndef PLAN
#define PLAN

#include "AbstractConstraint.h"
#include "../som/Vector.cpp"

using namespace std;

namespace deltablue {

    class Plan : public Vector<shared_ptr<AbstractConstraint>> {
        public:
            Plan();

            void execute();
    };
}

#endif //PLAN