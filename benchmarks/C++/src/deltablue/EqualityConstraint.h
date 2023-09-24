#ifndef EQUALITYCONSTRAINT
#define EQUALITYCONSTRAINT

#include "BinaryConstraint.h"
#include "Strength.h"
#include "Direction.h"
#include <memory>

using namespace std;

namespace deltablue {
    class EqualityConstraint : public BinaryConstraint {
        public:
            EqualityConstraint(shared_ptr<Variable> var1, shared_ptr<Variable> var2, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner);

            void execute() override;
    };
}

#endif //EQUALITYCONSTRAINT