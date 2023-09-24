#include "EqualityConstraint.h"

namespace deltablue {
    EqualityConstraint::EqualityConstraint(shared_ptr<Variable> var1, shared_ptr<Variable> var2, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner)
            : BinaryConstraint(var1, var2, strength/*, planner*/) {
        //addConstraint(planner);
    }

    void EqualityConstraint::execute() {
        if (_direction == Direction::FORWARD) {
            _v2->setValue(_v1->getValue());
        } else {
                _v1->setValue(_v2->getValue());
        }
    }
}