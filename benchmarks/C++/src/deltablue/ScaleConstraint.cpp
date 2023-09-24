#include "ScaleConstraint.h"

namespace deltablue {

    ScaleConstraint::ScaleConstraint(shared_ptr<Variable> src, shared_ptr<Variable> scale, shared_ptr<Variable> offset,
            shared_ptr<Variable> dest, shared_ptr<Strength::Sym> strength, shared_ptr<Planner> planner)
        : BinaryConstraint(src, dest, strength/*, planner*/) {
        _strength = Strength::of(strength);
        _v1 = src;
        _v2 = dest;
        _direction = NONE;
        _scale = scale;
        _offset = offset;

        //addConstraint(planner);
    }

    void ScaleConstraint::addToGraph() {
        _v1->addConstraint(shared_from_this());
        _v2->addConstraint(shared_from_this());
        _scale->addConstraint(shared_from_this());
        _offset->addConstraint(shared_from_this());
        _direction = NONE;
    }

    void ScaleConstraint::removeFromGraph() {
        if (_v1 != nullptr) 
            _v1->removeConstraint(shared_from_this());
        if (_v2 != nullptr) 
            _v2->removeConstraint(shared_from_this());
        if (_scale != nullptr) 
            _scale->removeConstraint(shared_from_this());
        if (_offset != nullptr) 
            _offset->removeConstraint(shared_from_this());
        _direction = NONE;
    }

    void ScaleConstraint::execute() {
        if (_direction == FORWARD) {
        _v2->setValue(_v1->getValue() * _scale->getValue() + _offset->getValue());
        } else {
        _v1->setValue((_v2->getValue() - _offset->getValue()) / _scale->getValue());
        }
    }

    void ScaleConstraint::inputsDo(function<void(shared_ptr<Variable>)> fn) {
        if (_direction == FORWARD) {
            fn(_v1);
            fn(_scale);
            fn(_offset);
        } else {
            fn(_v2);
            fn(_scale);
            fn(_offset);
        }
    }

    void ScaleConstraint::recalculate() {
        shared_ptr<Variable> in;
        shared_ptr<Variable> out;

        if (_direction == FORWARD) {
            in = _v1;
            out = _v2;
        } else {
            out = _v1;
            in = _v2;
        }

        out->setWalkStrength(_strength->weakest(in->getWalkStrength()));
        out->setStay(in->getStay() && _scale->getStay() && _offset->getStay());
        if (out->getStay()) {
            execute(); // stay optimization
        }
    }
}