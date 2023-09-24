#ifndef SCALECONSTRAINT
#define SCALECONSTRAINT


#include "BinaryConstraint.h"
#include "Variable.h"
#include <memory>

using namespace std;

namespace deltablue {
    class ScaleConstraint :public BinaryConstraint {
        private:
            shared_ptr<Variable> _scale;
            shared_ptr<Variable> _offset;

        public:
            ScaleConstraint(shared_ptr<Variable> src, shared_ptr<Variable> scale, shared_ptr<Variable> offset,
                            shared_ptr<Variable> dest, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner);

            void addToGraph() override;

            void removeFromGraph() override;
            void execute() override;

            void inputsDo(function<void(shared_ptr<Variable>)> fn) override;
            void recalculate() override;
    };
}

#endif //SCALECONSTRAINT