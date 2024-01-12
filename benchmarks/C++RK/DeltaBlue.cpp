/* Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */

#include "DeltaBlue.h"
#include "som/Dictionary.h"
using namespace som;

// NOTE:
// 12000 runs with no memory management (just new, no delete): 19us
class Sym {

    int hash;
public:
    Sym(int hash) {
        this->hash = hash;
    }

    class Hash
    {
    public:
        int operator()(Sym* s) {
            return s->hash;
        }
    };
};

class Strength {
public:


    static Sym* ABSOLUTE_STRONGEST;
    static Sym* REQUIRED;
    static Sym* STRONG_PREFERRED;
    static Sym* PREFERRED;
    static Sym* STRONG_DEFAULT;
    static Sym* DEFAULT;
    static Sym* WEAK_DEFAULT;
    static Sym* ABSOLUTE_WEAKEST;

    Strength(Sym* symbolicValue) {
        this->symbolicValue = symbolicValue;
        this->arithmeticValue = *strengthTable.at(symbolicValue);
    }
    bool sameAs(Strength* s) {
        return arithmeticValue == s->getArithmeticValue();
    }

    bool stronger(Strength* s) {
        return arithmeticValue < s->getArithmeticValue();
    }

    bool weaker(Strength* s) {
        return arithmeticValue > s->getArithmeticValue();
    }

    Strength* strongest(Strength* s) {
        return s->stronger(this) ? s : this;
    }

    Strength* weakest(Strength* s) {
        return s->weaker(this) ? s : this;
    }

    int getArithmeticValue() {
        return arithmeticValue;
    }

    static Strength* of(Sym* sym) {
        return *strengthConstant.at(sym);
    }

    static Strength* absoluteWeakest() {
        return absoluteWeakest_;
    }

    static Strength* required() {
        return required_;
    }

    static void init()
    {
        if( ABSOLUTE_STRONGEST )
            return;
        ABSOLUTE_STRONGEST = new Sym(0);
        REQUIRED           = new Sym(1);
        STRONG_PREFERRED   = new Sym(2);
        PREFERRED          = new Sym(3);
        STRONG_DEFAULT     = new Sym(4);
        DEFAULT            = new Sym(5);
        WEAK_DEFAULT       = new Sym(6);
        ABSOLUTE_WEAKEST   = new Sym(7);
        createStrengthTable(strengthTable);
        createStrengthConstants(strengthConstant);
        absoluteWeakest_   = Strength::of(ABSOLUTE_WEAKEST);
        required_          = Strength::of(REQUIRED);
    }

    static void deinit()
    {
        Vector<Strength*> vals;
        strengthConstant.getValues(vals);
        for( int i = 0; i < vals.size(); i++ )
            delete vals.at(i);

        delete ABSOLUTE_STRONGEST;
        delete REQUIRED;
        delete STRONG_PREFERRED;
        delete PREFERRED;
        delete STRONG_DEFAULT;
        delete DEFAULT;
        delete WEAK_DEFAULT;
        delete ABSOLUTE_WEAKEST;
        ABSOLUTE_STRONGEST = 0;
        REQUIRED           = 0;
        STRONG_PREFERRED   = 0;
        PREFERRED          = 0;
        STRONG_DEFAULT     = 0;
        DEFAULT            = 0;
        WEAK_DEFAULT       = 0;
        ABSOLUTE_WEAKEST   = 0;
        strengthTable.removeAll();

        strengthConstant.removeAll();
    }

private:
    int arithmeticValue;
    Sym*   symbolicValue;

    static void createStrengthTable(IdentityDictionary<Sym*, int, Sym::Hash>& strengthTable) {
        strengthTable.atPut(ABSOLUTE_STRONGEST, -10000);
        strengthTable.atPut(REQUIRED,           -800);
        strengthTable.atPut(STRONG_PREFERRED,   -600);
        strengthTable.atPut(PREFERRED,          -400);
        strengthTable.atPut(STRONG_DEFAULT,     -200);
        strengthTable.atPut(DEFAULT,             0);
        strengthTable.atPut(WEAK_DEFAULT,        500);
        strengthTable.atPut(ABSOLUTE_WEAKEST,    10000);
    }

    static void createStrengthConstants(IdentityDictionary<Sym*, Strength*, Sym::Hash>& strengthConstant) {
        Vector<Sym*> keys;
        strengthTable.getKeys(keys);
        for( int i = 0; i < keys.size(); i++ )
        {
            Sym* key = keys.at(i);
            strengthConstant.atPut(key, new Strength(key));
        }
    }

    static Strength* absoluteWeakest_;
    static Strength* required_;
    static IdentityDictionary<Sym*, int, Sym::Hash>  strengthTable;
    static IdentityDictionary<Sym*, Strength*, Sym::Hash> strengthConstant;
};

Strength* Strength::absoluteWeakest_ = 0;
Strength* Strength::required_ = 0;
IdentityDictionary<Sym*, int, Sym::Hash>  Strength::strengthTable;
IdentityDictionary<Sym*, Strength*, Sym::Hash> Strength::strengthConstant;

Sym* Strength::ABSOLUTE_STRONGEST = 0;
Sym* Strength::REQUIRED           = 0;
Sym* Strength::STRONG_PREFERRED   = 0;
Sym* Strength::PREFERRED          = 0;
Sym* Strength::STRONG_DEFAULT     = 0;
Sym* Strength::DEFAULT            = 0;
Sym* Strength::WEAK_DEFAULT       = 0;
Sym* Strength::ABSOLUTE_WEAKEST   = 0;

class Planner;
class Variable;

class AbstractConstraint {

protected:
    Strength* strength;

    // Activate this constraint and attempt to satisfy it.
    void addConstraint(Planner* planner);

public:
    AbstractConstraint(Sym* strength) {
        this->strength = Strength::of(strength);
    }
    virtual ~AbstractConstraint() {}

    Strength* getStrength() const {
        return strength;
    }

    // Normal constraints are not input constraints. An input constraint
    // is one that depends on external state, such as the mouse, the
    // keyboard, a clock, or some arbitrary piece of imperative code.
    virtual bool isInput() const {
        return false;
    }

    // Answer true if this constraint is satisfied in the current solution.
    virtual bool isSatisfied() const = 0;


    // Add myself to the constraint graph.
    virtual void addToGraph() = 0;

    // Deactivate this constraint, remove it from the constraint graph,
    // possibly causing other constraints to be satisfied, and destroy
    // it.
    void destroyConstraint(Planner* planner);

    // Remove myself from the constraint graph.
    virtual void removeFromGraph() = 0;

    // Decide if I can be satisfied and record that decision. The output
    // of the chosen method must not have the given mark and must have
    // a walkabout strength less than that of this constraint.
    virtual int chooseMethod(int mark) = 0; // Direction

    // Enforce this constraint. Assume that it is satisfied.
    virtual void execute() = 0;


    virtual void inputsDo(ForEachInterface<Variable*>& fn) = 0;
    virtual bool inputsHasOne(const TestInterface<Variable*>& fn) const = 0;

    // Assume that I am satisfied. Answer true if all my current inputs
    // are known. A variable is known if either a) it is 'stay' (i.e. it
    // is a constant at plan execution time), b) it has the given mark
    // (indicating that it has been computed by a constraint appearing
    // earlier in the plan), or c) it is not determined by any
    // constraint.
    bool inputsKnown(int mark);

    // Record the fact that I am unsatisfied.
    virtual void markUnsatisfied() = 0;

    // Answer my current output variable. Raise an error if I am not
    // currently satisfied.
    virtual Variable* getOutput() const = 0;

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    virtual void recalculate() = 0;

    // Attempt to find a way to enforce this constraint. If successful,
    // record the solution, perhaps modifying the current dataflow
    // graph. Answer the constraint that this constraint overrides, if
    // there is one, or nil, if there isn't.
    // Assume: I am not already satisfied.
    //
    AbstractConstraint* satisfy(int mark, Planner* planner);
};

class Variable {

    int value_;       // my value; changed by constraints
    Vector<AbstractConstraint*> constraints; // normal constraints that reference me
    AbstractConstraint* determinedBy; // the constraint that currently determines
    // my value (or null if there isn't one)
    int mark;        // used by the planner to mark constraints
    Strength* walkStrength; // my walkabout strength
    bool  stay;        // true if I am a planning-time constant

public:
    static Variable* value(int aValue) {
        Variable* v = new Variable();
        v->setValue(aValue);
        return v;
    }

    Variable() {
        value_ = 0;
        constraints;
        determinedBy = 0;
        walkStrength = Strength::absoluteWeakest();
        stay = true;
        mark = 0;
    }

    // Add the given constraint to the set of all constraints that refer to me.
    void addConstraint(AbstractConstraint* c) {
        constraints.append(c);
    }

    const Vector<AbstractConstraint*>& getConstraints() const {
        return constraints;
    }

    AbstractConstraint* getDeterminedBy() const {
        return determinedBy;
    }

    void setDeterminedBy(AbstractConstraint* c) {
        determinedBy = c;
    }

    int getMark() const {
        return mark;
    }

    void setMark(int markValue) {
        mark = markValue;
    }

    // Remove all traces of c from this variable.
    void removeConstraint(AbstractConstraint* c) {
        constraints.remove(c);
        if (determinedBy == c) {
            determinedBy = 0;
        }
    }

    bool getStay() const {
        return stay;
    }

    void setStay(bool v) {
        stay = v;
    }

    int getValue() const {
        return value_;
    }

    void setValue(int value) {
        value_ = value;
    }

    Strength* getWalkStrength() {
        return walkStrength;
    }

    void setWalkStrength(Strength* strength) {
        walkStrength = strength;
    }
};

class Plan : public Vector<AbstractConstraint*> {
public:
    Plan() {
        expand(15);
    }

    void execute() {
        for( int i = 0; i < size(); i++ )
            at(i)->execute();
    }
};

class Planner {
    int currentMark;

public:
    Planner() {
        currentMark = 1;
    }

    // Attempt to satisfy the given constraint and, if successful,
    // incrementally update the dataflow graph. Details: If satifying
    // the constraint is successful, it may override a weaker constraint
    // on its output. The algorithm attempts to resatisfy that
    // constraint using some other method. This process is repeated
    // until either a) it reaches a variable that was not previously
    // determined by any constraint or b) it reaches a constraint that
    // is too weak to be satisfied using any of its methods. The
    // variables of constraints that have been processed are marked with
    // a unique mark value so that we know where we've been. This allows
    // the algorithm to avoid getting into an infinite loop even if the
    // constraint graph has an inadvertent cycle.
    //
    void incrementalAdd(AbstractConstraint* c) {
        int mark = newMark();
        AbstractConstraint* overridden = c->satisfy(mark, this);

        while (overridden != 0) {
            overridden = overridden->satisfy(mark, this);
        }
    }

    // Entry point for retracting a constraint. Remove the given
    // constraint and incrementally update the dataflow graph.
    // Details: Retracting the given constraint may allow some currently
    // unsatisfiable downstream constraint to be satisfied. We therefore collect
    // a list of unsatisfied downstream constraints and attempt to
    // satisfy each one in turn. This list is traversed by constraint
    // strength, strongest first, as a heuristic for avoiding
    // unnecessarily adding and then overriding weak constraints.
    // Assume: c is satisfied.
    //
    void incrementalRemove(AbstractConstraint* c) {
        Variable* out = c->getOutput();
        c->markUnsatisfied();
        c->removeFromGraph();

        Vector<AbstractConstraint*> unsatisfied;
        removePropagateFrom(out,unsatisfied);
        for( int i = 0; i < unsatisfied.size(); i++ )
            incrementalAdd(unsatisfied.at(i));
    }

    // Extract a plan for resatisfaction starting from the outputs of
    // the given constraints, usually a set of input constraints.
    //
protected:
    Plan* extractPlanFromConstraints(const Vector<AbstractConstraint*>& constraints) {
        Vector<AbstractConstraint*> sources;

        for( int i = 0; i < constraints.size(); i++ )
        {
            AbstractConstraint* c = constraints.at(i);
            if (c->isInput() && c->isSatisfied()) {
                sources.append(c);
            }
        }

        return makePlan(sources);
    }

    // Extract a plan for resatisfaction starting from the given source
    // constraints, usually a set of input constraints. This method
    // assumes that stay optimization is desired; the plan will contain
    // only constraints whose output variables are not stay. Constraints
    // that do no computation, such as stay and edit constraints, are
    // not included in the plan.
    // Details: The outputs of a constraint are marked when it is added
    // to the plan under construction. A constraint may be appended to
    // the plan when all its input variables are known. A variable is
    // known if either a) the variable is marked (indicating that has
    // been computed by a constraint appearing earlier in the plan), b)
    // the variable is 'stay' (i.e. it is a constant at plan execution
    // time), or c) the variable is not determined by any
    // constraint. The last provision is for past states of history
    // variables, which are not stay but which are also not computed by
    // any constraint.
    // Assume: sources are all satisfied.
    //
    Plan* makePlan(const Vector<AbstractConstraint*>& sources) {
        int mark = newMark();
        Plan* plan = new Plan();
        Vector<AbstractConstraint*> todo = sources;

        while (!todo.isEmpty()) {
            AbstractConstraint* c = todo.removeFirst();

            if (c->getOutput()->getMark() != mark && c->inputsKnown(mark)) {
                // not in plan already and eligible for inclusion
                plan->append(c);
                c->getOutput()->setMark(mark);
                addConstraintsConsumingTo(c->getOutput(), todo);
            }
        }
        return plan;
    }

public:
    // The given variable has changed. Propagate values downstream.
    void propagateFrom(Variable* v) {
        Vector<AbstractConstraint*> todo;
        addConstraintsConsumingTo(v, todo);

        while (!todo.isEmpty()) {
            AbstractConstraint* c = todo.removeFirst();
            c->execute();
            addConstraintsConsumingTo(c->getOutput(), todo);
        }
    }

    void addConstraintsConsumingTo(Variable* v, Vector<AbstractConstraint*>& coll) {
        AbstractConstraint* determiningC = v->getDeterminedBy();

        for( int i = 0; i < v->getConstraints().size(); i++ )
        {
            AbstractConstraint* c = v->getConstraints().at(i);
            if (c != determiningC && c->isSatisfied()) {
                coll.append(c);
            }
        }
    }

    // Recompute the walkabout strengths and stay flags of all variables
    // downstream of the given constraint and recompute the actual
    // values of all variables whose stay flag is true. If a cycle is
    // detected, remove the given constraint and answer
    // false. Otherwise, answer true.
    // Details: Cycles are detected when a marked variable is
    // encountered downstream of the given constraint. The sender is
    // assumed to have marked the inputs of the given constraint with
    // the given mark. Thus, encountering a marked node downstream of
    // the output constraint means that there is a path from the
    // constraint's output to one of its inputs.
    //
public:
    bool addPropagate(AbstractConstraint* c, int mark) {
        Vector<AbstractConstraint*> todo;
        todo.append(c);

        while (!todo.isEmpty()) {
            AbstractConstraint* d = todo.removeFirst();

            if (d->getOutput()->getMark() == mark) {
                incrementalRemove(c);
                return false;
            }
            d->recalculate();
            addConstraintsConsumingTo(d->getOutput(), todo);
        }
        return true;
    }

private:
    void change(Variable* var, int newValue);

    void constraintsConsuming(Variable* v, ForEachInterface<AbstractConstraint*>& fn) {
        AbstractConstraint* determiningC = v->getDeterminedBy();
        for( int i = 0; i < v->getConstraints().size(); i++ )
        {
            AbstractConstraint* c = v->getConstraints().at(i);
            if (c != determiningC && c->isSatisfied()) {
                fn.apply(c);
            }
        }
    }

    // Select a previously unused mark value.
    int newMark() {
        currentMark++;
        return currentMark;
    }

    // Update the walkabout strengths and stay flags of all variables
    // downstream of the given constraint. Answer a collection of
    // unsatisfied constraints sorted in order of decreasing strength.
protected:
    void removePropagateFrom(Variable* out, Vector<AbstractConstraint*>& unsatisfied) {

        out->setDeterminedBy(0);
        out->setWalkStrength(Strength::absoluteWeakest());
        out->setStay(true);

        Vector<Variable*> todo;
        todo.append(out);

        while (!todo.isEmpty()) {
            Variable* v = todo.removeFirst();

            for(int i = 0; i < v->getConstraints().size(); i++ )
            {
                AbstractConstraint* c = v->getConstraints().at(i);
                if (!c->isSatisfied()) { unsatisfied.append(c); }
            }

            class Iter : public ForEachInterface<AbstractConstraint*>
            {
                Vector<Variable*>& todo;
            public:
                Iter(Vector<Variable*>& todo):todo(todo){}
                void apply( AbstractConstraint* const & c)
                {
                    c->recalculate();
                    todo.append(c->getOutput());
                }
            }it(todo);
            constraintsConsuming(v, it);
        }

        class Iter : public Comparator<AbstractConstraint*>
        {
        public:
            int compare(AbstractConstraint* const & c1, AbstractConstraint* const & c2) const
            {
                return c1->getStrength()->stronger(c2->getStrength()) ? -1 : 1;
            }
        } it;
        unsatisfied.sort(it);
    }

public:
    // This is the standard DeltaBlue benchmark. A long chain of
    // equality constraints is constructed with a stay constraint on
    // one end. An edit constraint is then added to the opposite end
    // and the time is measured for adding and removing this
    // constraint, and extracting and executing a constraint
    // satisfaction plan. There are two cases. In case 1, the added
    // constraint is stronger than the stay constraint and values must
    // propagate down the entire length of the chain. In case 2, the
    // added constraint is weaker than the stay constraint so it cannot
    // be accomodated. The cost in this case is, of course, very
    // low. Typical situations lie somewhere between these two
    // extremes.
    //
    static void chainTest(int n);

    // This test constructs a two sets of variables related to each
    // other by a simple linear transformation (scale and offset). The
    // time is measured to change a variable on either side of the
    // mapping and to change the scale and offset factors.
    //
    static void projectionTest(int n);
};

class UnaryConstraint : public AbstractConstraint {
protected:
    Variable* output; // possible output variable
    bool  satisfied; // true if I am currently satisfied

public:
    UnaryConstraint(Variable* v, Sym* strength, Planner* planner):AbstractConstraint(strength) {
        output = v;
        addConstraint(planner);
    }

    // Answer true if this constraint is satisfied in the current solution.
    bool isSatisfied() const {
        return satisfied;
    }

    // Add myself to the constraint graph.
    void addToGraph() {
        output->addConstraint(this);
        satisfied = false;
    }

    // Remove myself from the constraint graph.
    void removeFromGraph() {
        if (output != 0) {
            output->removeConstraint(this);
        }
        satisfied = false;
    }

    void execute() { } // do nothing.
protected:
    // Decide if I can be satisfied and record that decision.
    int chooseMethod(int mark) {
        satisfied = output->getMark() != mark
                && strength->stronger(output->getWalkStrength());
        return 0;
    }

public:
    void inputsDo(ForEachInterface<Variable*>& fn) {
        // I have no input variables
    }

    bool inputsHasOne(const TestInterface<Variable*>& fn) const {
        return false;
    };

    // Record the fact that I am unsatisfied.
    void markUnsatisfied() {
        satisfied = false;
    }

    // Answer my current output variable.
    Variable* getOutput() const {
        return output;
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied."
    void recalculate() {
        output->setWalkStrength(strength);
        output->setStay(!isInput());
        if (output->getStay()) {
            execute(); // stay optimization
        }
    }
};

class EditConstraint : public UnaryConstraint {
public:
    EditConstraint(Variable* v, Sym* strength, Planner* planner):UnaryConstraint(v, strength, planner){
    }

    // I indicate that a variable is to be changed by imperative code.
    bool isInput() const {
        return true;
    }

    void execute() { } // Edit constraints do nothing.
};

class StayConstraint : public UnaryConstraint {
public:
    // Install a stay constraint with the given strength on the given variable.
    StayConstraint(Variable* v, Sym* strength,
                   Planner* planner):UnaryConstraint(v, strength, planner) {
    }

    void execute() { } // Stay constraints do nothing.
};

class BinaryConstraint : public AbstractConstraint {

protected:
    Variable* v1;
    Variable* v2;          // possible output variables
    enum Direction { FORWARD = 1, BACKWARD = 2 };
    int direction;  // Direction

public:
    BinaryConstraint(Variable* var1, Variable* var2,
                     Sym* strength, Planner* planner):AbstractConstraint(strength) {
        v1 = var1;
        v2 = var2;
        direction = 0;
    }

    // Answer true if this constraint is satisfied in the current solution.
    bool isSatisfied() const {
        return direction != 0;
    }

    // Add myself to the constraint graph.
    void addToGraph() {
        v1->addConstraint(this);
        v2->addConstraint(this);
        direction = 0;
    }

    // Remove myself from the constraint graph.
    void removeFromGraph() {
        if (v1 != 0) {
            v1->removeConstraint(this);
        }
        if (v2 != 0) {
            v2->removeConstraint(this);
        }
        direction = 0;
    }

    void execute() { } // do nothing.

    // Decide if I can be satisfied and which way I should flow based on
    // the relative strength of the variables I relate, and record that
    // decision.
    //
protected:
    int chooseMethod(int mark) {
        if (v1->getMark() == mark) {
            if (v2->getMark() != mark && strength->stronger(v2->getWalkStrength())) {
                direction = FORWARD;
                return direction;
            } else {
                direction = 0;
                return direction;
            }
        }

        if (v2->getMark() == mark) {
            if (v1->getMark() != mark && strength->stronger(v1->getWalkStrength())) {
                direction = BACKWARD;
                return direction;
            } else {
                direction = 0;
                return direction;
            }
        }

        // If we get here, neither variable is marked, so we have a choice.
        if (v1->getWalkStrength()->weaker(v2->getWalkStrength())) {
            if (strength->stronger(v1->getWalkStrength())) {
                direction = BACKWARD;
                return direction;
            } else {
                direction = 0;
                return direction;
            }
        } else {
            if (strength->stronger(v2->getWalkStrength())) {
                direction = FORWARD;
                return direction;
            } else {
                direction = 0;
                return direction;
            }
        }
    }

    void inputsDo(ForEachInterface<Variable*>& fn) {
        if (direction == FORWARD) {
            fn.apply(v1);
        } else {
            fn.apply(v2);
        }
    }

    bool inputsHasOne(const TestInterface<Variable*>& fn) const {
        if (direction == FORWARD) {
            return fn.test(v1);
        } else {
            return fn.test(v2);
        }
    }

    // Record the fact that I am unsatisfied.
    void markUnsatisfied() {
        direction = 0;
    }


    // Answer my current output variable.
    Variable* getOutput() const {
        return direction == FORWARD ? v2 : v1;
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    //
    void recalculate() {
        Variable* in;
        Variable* out;

        if (direction == FORWARD) {
            in = v1; out = v2;
        } else {
            in = v2; out = v1;
        }

        out->setWalkStrength(strength->weakest(in->getWalkStrength()));
        out->setStay(in->getStay());
        if (out->getStay()) {
            execute();
        }
    }
};

class EqualityConstraint : public BinaryConstraint {
public:
    // Install a constraint with the given strength equating the given
    // variables.
    EqualityConstraint(Variable* var1, Variable* var2,
                       Sym* strength, Planner* planner):BinaryConstraint(var1, var2, strength, planner) {
        addConstraint(planner);
    }

    // Enforce this constraint. Assume that it is satisfied.
    void execute() {
        if (direction == FORWARD) {
            v2->setValue(v1->getValue());
        } else {
            v1->setValue(v2->getValue());
        }
    }
};

class ScaleConstraint : public BinaryConstraint {

protected:
    Variable* scale;  // scale factor input variable
    Variable* offset; // offset input variable

public:
    ScaleConstraint(Variable* src, Variable* scale,
                    Variable* offset, Variable* dest, Sym* strength,
                    Planner* planner):BinaryConstraint(src, dest, strength, planner) {
        this->scale = scale;
        this->offset = offset;
        addConstraint(planner);
    }

    // Add myself to the constraint graph.
    void addToGraph() {
        v1->addConstraint(this);
        v2->addConstraint(this);
        scale->addConstraint(this);
        offset->addConstraint(this);
        direction = 0;
    }

    // Remove myself from the constraint graph.
    void removeFromGraph() {
        if (v1 != 0) { v1->removeConstraint(this); }
        if (v2 != 0) { v2->removeConstraint(this); }
        if (scale  != 0) { scale->removeConstraint(this); }
        if (offset != 0) { offset->removeConstraint(this); }
        direction = 0;
    }

    // Enforce this constraint. Assume that it is satisfied.
    void execute() {
        if (direction == FORWARD) {
            v2->setValue(v1->getValue() * scale->getValue() + offset->getValue());
        } else {
            v1->setValue((v2->getValue() - offset->getValue()) / scale->getValue());
        }
    }

    void inputsDo(ForEachInterface<Variable*>& fn) {
        if (direction == FORWARD) {
            fn.apply(v1);
            fn.apply(scale);
            fn.apply(offset);
        } else {
            fn.apply(v2);
            fn.apply(scale);
            fn.apply(offset);
        }
    }

    // Calculate the walkabout strength, the stay flag, and, if it is
    // 'stay', the value for the current output of this
    // constraint. Assume this constraint is satisfied.
    void recalculate() {
        Variable* in;
        Variable* out;

        if (direction == FORWARD) {
            in  = v1; out = v2;
        } else {
            out = v1; in  = v2;
        }

        out->setWalkStrength(strength->weakest(in->getWalkStrength()));
        out->setStay(in->getStay() && scale->getStay() && offset->getStay());
        if (out->getStay()) {
            execute(); // stay optimization
        }
    }
};

void AbstractConstraint::addConstraint(Planner *planner)
{
    addToGraph();
    planner->incrementalAdd(this);

}

void AbstractConstraint::destroyConstraint(Planner *planner)
{
    if (isSatisfied()) {
        planner->incrementalRemove(this);
    }
    removeFromGraph();
}

bool AbstractConstraint::inputsKnown(int mark)
{
    class Iter : public TestInterface<Variable*>
    {
        int mark;
    public:
        Iter(int m):mark(m){}
        bool test(Variable* const & v) const
        {
            return !(v->getMark() == mark || v->getStay() || v->getDeterminedBy() == 0);
        }
    } iter(mark);
    return !inputsHasOne(iter);
}

AbstractConstraint *AbstractConstraint::satisfy(int mark, Planner *planner)
{
    AbstractConstraint* overridden = 0;

    chooseMethod(mark);

    if (isSatisfied()) {
        // constraint can be satisfied
        // mark inputs to allow cycle detection in addPropagate
        class Iter : public ForEachInterface<Variable*>
        {
            int mark;
        public:
            Iter(int mark):mark(mark){}
            void apply(Variable* const & in)
            {
                in->setMark(mark);
            }
        } it(mark);
        inputsDo(it);

        Variable* out = getOutput();
        overridden = out->getDeterminedBy();
        if (overridden != 0) {
            overridden->markUnsatisfied();
        }
        out->setDeterminedBy(this);
        if (!planner->addPropagate(this, mark)) {
            throw "Cycle encountered";
        }
        out->setMark(mark);
    } else {
        overridden = 0;
        if (strength->sameAs(Strength::required())) {
            throw "Could not satisfy a required constraint";
        }
    }
    return overridden;

}

void Planner::change(Variable *var, int newValue)
{
    EditConstraint* editC = new EditConstraint(var, Strength::PREFERRED, this);

    Vector<AbstractConstraint*> editV;
    editV.append(editC);
    Plan* plan = extractPlanFromConstraints(editV);
    for (int i = 0; i < 10; i++) {
        var->setValue(newValue);
        plan->execute();
    }
    editC->destroyConstraint(this);
    delete editC;
    delete plan;
}

void Planner::chainTest(int n)
{
    Planner* planner = new Planner();
    Vector<AbstractConstraint*> toDelete;
    Vector<Variable*> vars(n + 1);
    for( int i = 0; i < vars.capacity(); i++ )
        vars.atPut(i, new Variable());

    // Build chain of n equality constraints
    for (int i = 0; i < n; i++) {
        Variable* v1 = vars.at(i);
        Variable* v2 = vars.at(i + 1);
        toDelete.append( new EqualityConstraint(v1, v2, Strength::REQUIRED, planner));
    }

    toDelete.append( new StayConstraint(vars.at(n), Strength::STRONG_DEFAULT, planner) );
    AbstractConstraint* editC = new EditConstraint(vars.at(0), Strength::PREFERRED, planner);
    toDelete.append( editC );

    Vector<AbstractConstraint*> editV;
    editV.append(editC);
    Plan* plan = planner->extractPlanFromConstraints(editV);
    for (int i = 0; i < 100; i++) {
        vars.at(0)->setValue(i);
        plan->execute();
        if (vars.at(n)->getValue() != i) {
            throw "Chain test failed!";
        }
    }
    editC->destroyConstraint(planner);

    for( int i = 0; i < vars.size(); i++ )
        delete vars.at(i);

    for( int i = 0; i < toDelete.size(); i++ )
        delete toDelete.at(i);

    delete plan;
    delete planner;
}

void Planner::projectionTest(int n)
{
    Planner* planner = new Planner();

    Vector<Variable*> dests, toDelete;
    Vector<AbstractConstraint*> toDelete2;

    Variable* scale  = Variable::value(10);
    Variable* offset = Variable::value(1000);
    toDelete.append(scale);
    toDelete.append(offset);

    Variable* src = 0;
    Variable* dst = 0;
    for (int i = 1; i <= n; i++) {
        src = Variable::value(i);
        dst = Variable::value(i);
        toDelete.append(src);
        toDelete.append(dst);
        dests.append(dst);
        toDelete2.append( new StayConstraint(src, Strength::DEFAULT, planner));
        toDelete2.append( new ScaleConstraint(src, scale, offset, dst, Strength::REQUIRED, planner));
    }

    planner->change(src, 17);
    if (dst->getValue() != 1170) {
        throw "Projection test 1 failed!";
    }

    planner->change(dst, 1050);
    if (src->getValue() != 5) {
        throw "Projection test 2 failed!";
    }

    planner->change(scale, 5);
    for (int i = 0; i < n - 1; ++i) {
        if (dests.at(i)->getValue() != (i + 1) * 5 + 1000) {
            throw "Projection test 3 failed!";
        }
    }

    planner->change(offset, 2000);
    for (int i = 0; i < n - 1; ++i) {
        if (dests.at(i)->getValue() != (i + 1) * 5 + 2000) {
            throw "Projection test 4 failed!";
        }
    }

    for( int i = 0; i < toDelete.size(); i++ )
        delete toDelete.at(i);
    for( int i = 0; i < toDelete2.size(); i++ )
        delete toDelete2.at(i);
    delete planner;
}

bool DeltaBlue::innerBenchmarkLoop(int innerIterations)
{
    Strength::init();
    Planner::chainTest(innerIterations);
    Planner::projectionTest(innerIterations);
    Strength::deinit();
    return true;
}
