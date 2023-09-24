#include "StayConstraint.h"

using namespace std;

namespace deltablue {

    StayConstraint::StayConstraint(shared_ptr<Variable> v, shared_ptr<Strength::Sym> strength,
        shared_ptr<Planner> planner) : UnaryConstraint(v, strength, planner) {}

    void StayConstraint::execute() {}
}