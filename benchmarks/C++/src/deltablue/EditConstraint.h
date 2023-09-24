#ifndef EDITCONSTRAINT
#define EDITCONSTRAINT

#include <memory>
#include "Planner.h"
#include "UnaryConstraint.h"

using namespace std;

namespace deltablue {

    class EditConstraint: public UnaryConstraint {
        public:

            EditConstraint(shared_ptr<Variable> v, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner);

            bool isInput() override;
            void execute() override;
    };
}

#endif //EDITCONSTRAINT