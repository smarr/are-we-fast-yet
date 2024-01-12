#ifndef STAYCONSTRAINT
#define STAYCONSTRAINT

#include "UnaryConstraint.h"
#include "Planner.h"

using namespace std;

namespace deltablue {
    
    class StayConstraint : public UnaryConstraint {
        public:

            StayConstraint(shared_ptr<Variable> v, shared_ptr<Strength::Sym> strength,
                shared_ptr<Planner> planner);

            void execute() override;
        
    };
}

#endif //STAYCONSTRAINT