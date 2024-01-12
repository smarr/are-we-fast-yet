#ifndef ABSTRACTCONSTRAINT
#define ABSTRACTCONSTRAINT

#include <vector>
#include <functional>
#include <memory>
#include "../som/Error.cpp"
#include "Variable.h"
#include "Direction.h"

using namespace std;

namespace deltablue {

    class Planner;

    class AbstractConstraint : public enable_shared_from_this<AbstractConstraint> {
        protected:
            shared_ptr<Strength> _strength;
            shared_ptr<Planner> _planer;
        public:


            AbstractConstraint(shared_ptr<Strength::Sym> strength);

            shared_ptr<Strength> getStrength();
            virtual bool isInput();
            void addConstraint(shared_ptr<Planner> planner);
            void destroyConstraint(shared_ptr<Planner> planner);
            bool inputsKnown(int mark);
            shared_ptr<AbstractConstraint> satisfy(int mark, shared_ptr<Planner> planner);

            virtual bool isSatisfied() {};
            virtual void addToGraph() {};
            virtual void removeFromGraph() {};
            virtual Direction chooseMethod(int mark) {};
            virtual void execute() {};
            virtual void inputsDo(function<void(shared_ptr<Variable>)> fn) {};
            virtual bool inputsHasOne(function<bool(shared_ptr<Variable> )> fn) {};
            virtual void markUnsatisfied() {};
            virtual shared_ptr<Variable> getOutput() {};
            virtual void recalculate() {};


    };
}

#endif //ABSTRACTCONSTRAINT