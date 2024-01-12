#ifndef BINARYCONSTRAINT
#define BINARYCONSTRAINT

#include "AbstractConstraint.h"

using namespace std;

namespace deltablue {
    class BinaryConstraint: public AbstractConstraint {
        protected:
            shared_ptr<Variable> _v1;
            shared_ptr<Variable> _v2;
            Direction _direction;

        public:
            BinaryConstraint(shared_ptr<Variable> var1, shared_ptr<Variable> var2, shared_ptr<Strength::Sym> strength/*, shared_ptr<Planner> planner*/);
            bool isSatisfied() override;
            void addToGraph() override;
            void removeFromGraph() override;
            Direction chooseMethod(int mark) override;
            void inputsDo(function<void(shared_ptr<Variable>)> fn) override;
            bool inputsHasOne(function<bool(shared_ptr<Variable>)> fn) override;
            void markUnsatisfied() override;
            shared_ptr<Variable> getOutput() override;
            void recalculate() override ;
    };
}
#endif //BINARYCONSTRAINT