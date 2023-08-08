#include <memory>
#include "UnaryConstraint.h"

using namespace std;

namespace deltablue {
    UnaryConstraint::UnaryConstraint(shared_ptr<Variable> v, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner)
        : AbstractConstraint(strength) {
        _output = v;
        _satisfied = false;
        //addConstraint(planner);
    }

    bool UnaryConstraint::isSatisfied() {
        return _satisfied;
    }

    // Add myself to the constraint graph.
    void UnaryConstraint::addToGraph() {
        _output->addConstraint(shared_from_this());
        _satisfied = false;
    }

    // Remove myself from the constraint graph.
    void UnaryConstraint::removeFromGraph() {
        if (_output != nullptr) {
            _output->removeConstraint(shared_from_this());
        }
        _satisfied = false;
    }

    Direction UnaryConstraint::chooseMethod(int mark) {
        _satisfied = (_output->getMark() != mark) && _strength->stronger(_output->getWalkStrength());
        return NONE;
    }

    void UnaryConstraint::inputsDo(function<void(shared_ptr<Variable>)> fn) {
        // I have no input variables
    }

    bool UnaryConstraint::inputsHasOne(function<bool(shared_ptr<Variable>)> fn) {
        return false;
    }

    void UnaryConstraint::markUnsatisfied() {
        _satisfied = false;
    }

    shared_ptr<Variable> UnaryConstraint::getOutput() {
        return _output;
    }

    void UnaryConstraint::recalculate() {
        _output->setWalkStrength(_strength);
        _output->setStay(!isInput());
        if (_output->getStay()) {
            execute();
        }
    }
}