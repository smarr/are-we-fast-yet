#include "BinaryConstraint.h"
#include "Strength.h"
#include "Direction.h"
#include <memory>

using namespace std;

namespace deltablue {
    class EqualityConstraint : public BinaryConstraint {
        public:
            EqualityConstraint(shared_ptr<Variable> var1, shared_ptr<Variable> var2, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner)
                : BinaryConstraint(var1, var2, strength/*, planner*/) {
                //addConstraint(planner);
            }

            void execute() override {
                if (_direction == Direction::FORWARD) {
                    _v2->setValue(_v1->getValue());
                } else {
                    _v1->setValue(_v2->getValue());
                }
            }
    };
}
