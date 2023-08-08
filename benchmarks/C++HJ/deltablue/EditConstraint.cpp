#include "EditConstraint.h"

namespace deltablue {
    EditConstraint::EditConstraint(shared_ptr<Variable> v, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner)
        : UnaryConstraint(v, strength, planner) {
    }

    bool EditConstraint::isInput() {
        return true;
    }

    void EditConstraint::execute() {
        // Edit constraints do nothing.
    }
}