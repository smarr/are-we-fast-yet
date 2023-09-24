#include "Planner.h"
#include <iostream>

using namespace std;

namespace deltablue {
    
    Planner::Planner() {
        _currentMark = 1;
    }

    void Planner::incrementalAdd(shared_ptr<AbstractConstraint> c) {
        int mark = newMark();
        shared_ptr<AbstractConstraint> overridden = c->satisfy(mark, shared_ptr<Planner>());

        while (overridden != nullptr) {
            overridden = overridden->satisfy(mark, shared_ptr<Planner>());
        }
    }

    void Planner::incrementalRemove(shared_ptr<AbstractConstraint> c) {
        shared_ptr<Variable> out = c->getOutput();
        c->markUnsatisfied();
        c->removeFromGraph();

        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> unsatisfied = removePropagateFrom(out);
        unsatisfied->forEach([&](shared_ptr<AbstractConstraint> u) -> void {
            incrementalAdd(u);
        });
    }

    shared_ptr<Plan> Planner::extractPlanFromConstraints(shared_ptr<Vector<shared_ptr<AbstractConstraint>>> constraints) {
        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> sources = make_shared<Vector<shared_ptr<AbstractConstraint>>>();
        constraints->forEach([&](shared_ptr<AbstractConstraint> c) -> void {
            if (c->isInput() && c->isSatisfied()) {
                sources->append(c);
            }
        });
        return makePlan(sources);
    }

    shared_ptr<Plan> Planner::makePlan(shared_ptr<Vector<shared_ptr<AbstractConstraint>>> sources) {
        int mark = newMark();
        shared_ptr<Plan> plan = make_shared<Plan>();
        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> todo = sources;

        while (!todo->isEmpty()) {
            shared_ptr<AbstractConstraint> c = todo->removeFirst();

            if (c->getOutput()->getMark() != mark && c->inputsKnown(mark)) {
                plan->append(c);
                c->getOutput()->setMark(mark);
                addConstraintsConsumingTo(c->getOutput(), todo);
            }
        }

        return plan;
    }

    void Planner::propagateFrom(shared_ptr<Variable> v) {
        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> todo = make_shared<Vector<shared_ptr<AbstractConstraint>>>();
        addConstraintsConsumingTo(v, todo);

        while (!todo->isEmpty()) {
            shared_ptr<AbstractConstraint> c = todo->removeFirst();
            c->execute();
            addConstraintsConsumingTo(c->getOutput(), todo);
        }
    }

    void Planner::addConstraintsConsumingTo(shared_ptr<Variable> v, shared_ptr<Vector<shared_ptr<AbstractConstraint>>> coll) {
        shared_ptr<AbstractConstraint> determiningC = v->getDeterminedBy();

        v->getConstraints()->forEach([&](shared_ptr<AbstractConstraint> c) -> void {
            if (c != determiningC && c->isSatisfied()) {
                coll->append(c);
            }
        });
    }

    bool Planner::addPropagate(shared_ptr<AbstractConstraint> c, int mark) {
        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> todo = Vector<shared_ptr<AbstractConstraint>>::with(c);

        while (!todo->isEmpty()) {
            shared_ptr<AbstractConstraint> d = todo->removeFirst();

            if (d->getOutput()->getMark() == mark) {
                incrementalRemove(c);
                return false;
            }

            d->recalculate();
            addConstraintsConsumingTo(d->getOutput(), todo);
        }

        return true;
    }

    void Planner::change(shared_ptr<Variable> var, int newValue) {
        shared_ptr<EditConstraint> editC = make_shared<EditConstraint>(var, Strength::PREFERRED, shared_from_this());
        editC->addConstraint(shared_from_this());

        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> editV = Vector<shared_ptr<AbstractConstraint>>::with(editC);
        shared_ptr<Plan> plan = extractPlanFromConstraints(editV);
        for (int i = 0; i < 10; i++) {
            var->setValue(newValue);
            plan->execute();
        }

        editC->destroyConstraint(shared_from_this());
    }

    void Planner::constraintsConsuming(shared_ptr<Variable> v, function<void(shared_ptr<AbstractConstraint>)> fn) {
        shared_ptr<AbstractConstraint> determiningC = v->getDeterminedBy();
        
        v->getConstraints()->forEach([&](shared_ptr<AbstractConstraint> c) -> void {
            if (c != determiningC && c->isSatisfied()) {
                fn(c);
            }
        });
    }

    int Planner::newMark() {
        _currentMark++;
        return _currentMark;
    }

    shared_ptr<Vector<shared_ptr<AbstractConstraint>>> Planner::removePropagateFrom(shared_ptr<Variable> out) {
        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> unsatisfied = make_shared<Vector<shared_ptr<AbstractConstraint>>>();

        out->setDeterminedBy(nullptr);
        out->setWalkStrength(Strength::absoluteWeakest());
        out->setStay(true);

        shared_ptr<Vector<shared_ptr<Variable>>> todo = Vector<shared_ptr<Variable>>::with(out);

        while (!todo->isEmpty()) {
            shared_ptr<Variable> v = todo->removeFirst();

            v->getConstraints()->forEach([&](shared_ptr<AbstractConstraint> c) -> void {
                if (!c->isSatisfied()) {
                    unsatisfied->append(c);
                }
            });

            constraintsConsuming(v, [&todo](shared_ptr<AbstractConstraint> c) -> void {
                c->recalculate();
                todo->append(c->getOutput());
            });
        }

        unsatisfied->sort([&](shared_ptr<AbstractConstraint> c1, shared_ptr<AbstractConstraint> c2) -> int {
            return c1->getStrength()->stronger(c2->getStrength()) ? -1 : 1;

        });

        return unsatisfied;
    }

    void Planner::chainTest(int n) {
        Strength::initializeConstants();
        shared_ptr<Planner> planner = make_shared<Planner>();
        shared_ptr<Variable>* vars = new shared_ptr<Variable>[n + 1];
        for (int i = 0; i < n + 1; i++) {
            vars[i] = make_shared<Variable>();
        }
        for (int i = 0; i < n; i++) {
            shared_ptr<Variable> v1 = vars[i];
            shared_ptr<Variable> v2 = vars[i + 1];
            (make_shared<EqualityConstraint>(v1, v2, Strength::REQUIRED, planner))->addConstraint(planner);
        }
        (make_shared<StayConstraint>(vars[n], Strength::STRONG_DEFAULT, planner))->addConstraint(planner);
        shared_ptr<AbstractConstraint> editC = make_shared<EditConstraint>(vars[0], Strength::PREFERRED, planner);
        editC->addConstraint(planner);
        shared_ptr<Vector<shared_ptr<AbstractConstraint>>> editV = Vector<shared_ptr<AbstractConstraint>>::with(editC);
        shared_ptr<Plan> plan = planner->extractPlanFromConstraints(editV);

        for (int i = 0; i < 100; i++) {
            vars[0]->setValue(i);
            plan->execute();
            if (vars[n]->getValue() != i) {
                throw Error("Chain test failed!");
            }
        }
        editC->destroyConstraint(planner);
    }

    void Planner::projectionTest(int n) {
        shared_ptr<Planner> planner = make_shared<Planner>();
        shared_ptr<Vector<shared_ptr<Variable>>> dests = make_shared<Vector<shared_ptr<Variable>>>();

        shared_ptr<Variable> scale = Variable::value(10);
        shared_ptr<Variable> offset = Variable::value(1000);

        shared_ptr<Variable> src = nullptr;
        shared_ptr<Variable> dst = nullptr;
        for (int i = 1; i <= n; i++) {
            src = Variable::value(i);
            dst = Variable::value(i);
            dests->append(dst);
            (make_shared<StayConstraint>(src, Strength::DEFAULT, planner))->addConstraint(planner);
            (make_shared<ScaleConstraint>(src, scale, offset, dst, Strength::REQUIRED, planner))->addConstraint(planner);
        }

        planner->change(src, 17);
        if (dst->getValue() != 1170) {
            throw Error("Projection test 1 failed!");
        }

        planner->change(dst, 1050);
        if (src->getValue() != 5) {
            throw Error("Projection test 2 failed!");
            
        }

        planner->change(scale, 5);
        for (int i = 0; i < n - 1; ++i) {
            if (dests->at(i)->getValue() != (i + 1) * 5 + 1000) {
                throw Error("Projection test 3 failed!");
            }
        }

        planner->change(offset, 2000);
        for (int i = 0; i < n - 1; ++i) {
            if (dests->at(i)->getValue() != (i + 1) * 5 + 2000) {
                throw Error("Projection test 4 failed!");
            }
        }
    }
}
