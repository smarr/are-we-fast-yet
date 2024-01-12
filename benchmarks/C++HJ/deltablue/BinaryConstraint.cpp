#include "BinaryConstraint.h"

namespace deltablue {

            BinaryConstraint::BinaryConstraint(shared_ptr<Variable> var1, shared_ptr<Variable> var2,
                shared_ptr<Strength::Sym> strength/*, shared_ptr<Planner> planner*/) : AbstractConstraint(strength) { /// don't use planner ??? Probleme with "this"
                _v1 = var1;
                _v2 = var2;
                _direction = NONE;
            }

            bool BinaryConstraint::isSatisfied() {
                return _direction != NONE;
            }

            void BinaryConstraint::addToGraph() {
                _v1->addConstraint(shared_from_this());
                _v2->addConstraint(shared_from_this());
                _direction = NONE;
            }

            void BinaryConstraint::removeFromGraph() {
                _v1->removeConstraint(shared_from_this());
                _v2->removeConstraint(shared_from_this());
                _direction = NONE;
            }

            Direction BinaryConstraint::chooseMethod(int mark) {
                if (_v1->getMark() == mark) {
                    if (_v2->getMark() != mark && _strength->stronger(_v2->getWalkStrength())) {
                        _direction = FORWARD;
                        return _direction;
                    } else {
                        _direction = NONE;
                        return _direction;
                    }
                }

                if (_v2->getMark() == mark) {
                    if (_v1->getMark() != mark && _strength->stronger(_v1->getWalkStrength())) {
                        _direction = BACKWARD;
                        return _direction;
                    } else {
                        _direction = NONE;
                        return _direction;
                    }
                }

                if (_v1->getWalkStrength()->weaker(_v2->getWalkStrength())) {
                    if (_strength->stronger(_v1->getWalkStrength())) {
                        _direction = BACKWARD;
                        return _direction;
                    } else {
                        _direction = NONE;
                        return _direction;
                    }
                } else {
                    if (_strength->stronger(_v2->getWalkStrength())) {
                        _direction = FORWARD;
                        return _direction;
                    } else {
                        _direction = NONE;
                        return _direction;
                    }
                }
            }

            void BinaryConstraint::inputsDo(function<void(shared_ptr<Variable>)> fn) {
                if (_direction == FORWARD) {
                    fn(_v1);
                } else {
                    fn(_v2);
                }
            }

            bool BinaryConstraint::inputsHasOne(function<bool(shared_ptr<Variable>)> fn) {
                if (_direction == FORWARD) {
                    return fn(_v1);
                } else {
                    return fn(_v2);
                }
            }

            void BinaryConstraint::markUnsatisfied() {
                _direction = NONE;
            }

            shared_ptr<Variable> BinaryConstraint::getOutput() {
                return _direction == FORWARD ? _v2 : _v1;
            }

            void BinaryConstraint::recalculate() {
                shared_ptr<Variable> in;
                shared_ptr<Variable> out;

                if (_direction == FORWARD) {
                    in = _v1; 
                    out = _v2;
                } else {
                    in = _v2; 
                    out = _v1;
                }

                out->setWalkStrength(_strength->weakest(in->getWalkStrength()));
                out->setStay(in->getStay());
                if (out->getStay()) {
                    execute();
                }
            }
}