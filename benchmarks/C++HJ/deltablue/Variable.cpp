#include "AbstractConstraint.h"

using namespace std;

namespace deltablue {

    shared_ptr<Variable> Variable::value(int aValue) {
        shared_ptr<Variable> v = make_shared<Variable>();
        v->setValue(aValue);
        return v;
    }

    Variable::Variable() {
        _value = 0;
        _constraints = make_shared<Vector<shared_ptr<AbstractConstraint>>>(2);
        _determinedBy = nullptr;
        _walkStrength = Strength::absoluteWeakest();
        _stay = true;
        _mark = 0;
    }

    void Variable::addConstraint(shared_ptr<AbstractConstraint> c) {
        _constraints->append(c);
    }

    shared_ptr<Vector<shared_ptr<AbstractConstraint>>> Variable::getConstraints() {
        return _constraints;
    }

    shared_ptr<AbstractConstraint> Variable::getDeterminedBy() const {
        return _determinedBy;
    }

    void Variable::setDeterminedBy(shared_ptr<AbstractConstraint> c) {
        _determinedBy = c;
    }

    int Variable::getMark() const {
        return _mark;
    }

    void Variable::setMark(int markValue) {
        _mark = markValue;
    }

    void Variable::removeConstraint(shared_ptr<AbstractConstraint> c) {
        _constraints->remove(c);
        if (_determinedBy == c) {
          _determinedBy = nullptr;
        }
    }

    bool Variable::getStay() const {
        return _stay;
    }

    void Variable::setStay(bool v) {
        _stay = v;
    }

    int Variable::getValue() const {
        return _value;
    }

    void Variable::setValue(int value) {
        _value = value;
    }

    shared_ptr<Strength> Variable::getWalkStrength() {
        return _walkStrength;
    }

    void Variable::setWalkStrength(shared_ptr<Strength> strength) {
        _walkStrength = strength;
    }

}