#ifndef UNARYCONSTRAINT
#define UNARYCONSTRAINT

#include <memory>
#include "AbstractConstraint.h"

using namespace std;

namespace deltablue {
    class UnaryConstraint : public AbstractConstraint {
        protected:
            shared_ptr<Variable> _output; // possible output variable
            bool _satisfied;   // true if I am currently satisfied

        public:
            UnaryConstraint(shared_ptr<Variable> v, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner);

            bool isSatisfied() override;
            void addToGraph() override;
            void removeFromGraph() override;
            Direction chooseMethod(int mark) override;
            virtual void execute() = 0;
            void inputsDo(function<void(shared_ptr<Variable>)> fn) override;
            bool inputsHasOne(function<bool(shared_ptr<Variable>)> fn) override;
            void markUnsatisfied() override;
            shared_ptr<Variable> getOutput() override;
            void recalculate() override;

    };
}

#endif //UNARYCONSTRAINT