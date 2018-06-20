// This implementation of the DeltaBlue benchmark is derived
// from the Smalltalk implementation by John Maloney and Mario
// Wolczko. Some parts have been translated directly, whereas
// others have been modified more aggresively to make it feel
// more like a JavaScript program.
//
//
// A JavaScript implementation of the DeltaBlue constraint-solving
// algorithm, as described in:
//
// "The DeltaBlue Algorithm: An Incremental Constraint Hierarchy Solver"
//   Bjorn N. Freeman-Benson and John Maloney
//   January 1990 Communications of the ACM,
//   also available as University of Washington TR 89-08-06.
//
// Beware: this benchmark is written in a grotesque style where
// the constraint model is built by side-effects from constructors.
// I've kept it this way to avoid deviating too much from the original
// implementation.
//
//
// Adapted for Grace by Richard Roberts
// 28/05/2018
//
//


class OrderedCollection {

    def elms = platform.kernel.platform.new

    method add(elm) {
        elms.append(elm)
    }

    method at(index) {
        elms.at(index)
    }

    method size {
        return elms.size
    }

    method removeFirst {
        return elms.remove(elms.at(elems.size));
    }

    method remove(elm) {
        var index := 1.asInteger
        var skipped := 1.asInteger

        1.asInteger.to(size) do {
            var value := at(i)

            (value != elm).ifTrue {
                elms. at (index) put (value)
                index := index + 1
            } ifFalse {
                skipped := skipped + 1
            }
        }

        1.asInteger.to(skipped) do {
            removeFirst
        }
    }
}

//
// S t r e n g t h
//

class Strength {
    //
    // Strengths are used to measure the relative importance of constraints.
    // New strengths may be inserted in the strength hierarchy without
    // disrupting current constraints.  Strengths cannot be created outside
    // this class, so pointer comparison can be used for value comparison.
    //
    constructor(strengthValue, name) {
        this.strengthValue = strengthValue;
        this.name = name;
    }

    nextWeaker() {
        switch (this.strengthValue) {
        case 0: return Strength.WEAKEST;
        case 1: return Strength.WEAK_DEFAULT;
        case 2: return Strength.NORMAL;
        case 3: return Strength.STRONG_DEFAULT;
        case 4: return Strength.PREFERRED;
        case 5: return Strength.REQUIRED;
        }
    }

    static stronger(s1, s2) {
        return s1.strengthValue < s2.strengthValue;
    }

    static weaker(s1, s2) {
        return s1.strengthValue > s2.strengthValue;
    }

    static weakestOf(s1, s2) {
        return this.weaker(s1, s2) ? s1 : s2;
    }

    static strongest(s1, s2) {
        return this.stronger(s1, s2) ? s1 : s2;
    }
}

// Strength constants.
Strength.REQUIRED = new Strength(0, 'required')
Strength.STONG_PREFERRED = new Strength(1, 'strongPreferred')
Strength.PREFERRED = new Strength(2, 'preferred')
Strength.STRONG_DEFAULT = new Strength(3, 'strongDefault')
Strength.NORMAL = new Strength(4, 'normal')
Strength.WEAK_DEFAULT = new Strength(5, 'weakDefault')
Strength.WEAKEST = new Strength(6, 'weakest')

/* --- *
 * C o n s t r a i n t
 * --- */

export class Constraint {
    /**
     * An abstract class representing a system-maintainable relationship
     * (or "constraint") between a set of variables. A constraint supplies
     * a strength instance variable; concrete subclasses provide a means
     * of storing the constrained variables and other information required
     * to represent a constraint.
     */
    constructor(strength, planner) {
        this.planner = planner;
        this.strength = strength;
    }

    /**
     * Activate this constraint and attempt to satisfy it.
     */
    addConstraint(planner) {
        if (planner !== undefined && this.planner) {
            this.destroyConstraint();
            this.planner = planner;
        }
        this.addToGraph();
        this.planner.incrementalAdd(this);
    }

    /**
     * Attempt to find a way to enforce this constraint. If successful,
     * record the solution, perhaps modifying the current dataflow
     * graph. Answer the constraint that this constraint overrides, if
     * there is one, or nil, if there isn't.
     * Assume: I am not already satisfied.
     */
    satisfy(mark) {
        this.chooseMethod(mark);
        if (!this.isSatisfied()) {
            if (this.strength == Strength.REQUIRED) {
                // alert('Could not satisfy a required constraint!');
            }
            return null;
        }
        this.markInputs(mark);
        var out = this.output();
        var overridden = out.determinedBy;
        if (overridden != null) overridden.markUnsatisfied();
        out.determinedBy = this;
        if (!(this.planner.addPropagate(this, mark))) {
            alert('Cycle encountered');
        }
        out.mark = mark;
        return overridden;
    }

    destroyConstraint() {
        if (this.isSatisfied()) {
            this.planner.incrementalRemove(this);
        } else {
            this.removeFromGraph();
        }
    }

    /**
     * Normal constraints are not input constraints.  An input constraint
     * is one that depends on external state, such as the mouse, the
     * keybord, a clock, or some arbitraty piece of imperative code.
     */
    isInput() {
        return false;
    }
}

/* --- *
 * U n a r y   C o n s t r a i n t
 * --- */

export class UnaryConstraint extends Constraint {
    /**
     * Abstract superclass for constraints having a single possible output
     * variable.
     */
    constructor(v, strength, planner) {
        super(strength, planner);
        this.myOutput = v;
        this.satisfied = false;
    }

    /**
     * Adds this constraint to the constraint graph
     */
    addToGraph() {
        this.myOutput.addConstraint(this);
        this.satisfied = false;
    }

    /**
     * Decides if this constraint can be satisfied and records that
     * decision.
     */
    chooseMethod(mark) {
        this.satisfied = (this.myOutput.mark != mark) &&
            Strength.stronger(this.strength, this.myOutput.walkStrength);
    }

    /**
     * Returns true if this constraint is satisfied in the current solution.
     */
    isSatisfied() {
        return this.satisfied;
    }

    markInputs(mark) {
        // has no inputs
    }

    /**
     * Returns the current output variable.
     */
    output() {
        return this.myOutput;
    }

    /**
     * Calculate the walkabout strength, the stay flag, and, if it is
     * 'stay', the value for the current output of this constraint. Assume
     * this constraint is satisfied.
     */
    recalculate() {
        this.myOutput.walkStrength = this.strength;
        this.myOutput.stay = !this.isInput();
        if (this.myOutput.stay) this.execute(); // Stay optimization
    }

    /**
     * Records that this constraint is unsatisfied
     */
    markUnsatisfied() {
        this.satisfied = false;
    }

    inputsKnown() {
        return true;
    }

    removeFromGraph() {
        if (this.myOutput != null) this.myOutput.removeConstraint(this);
        this.satisfied = false;
    }
}

/* --- *
 * S t a y   C o n s t r a i n t
 * --- */

export class StayConstraint extends UnaryConstraint {
    /**
     * Variables that should, with some level of preference, stay the same.
     * Planners may exploit the fact that instances, if satisfied, will not
     * change their output during plan execution.  This is called "stay
     * optimization".
     */


    execute() {
        // Stay constraints do nothing
    }
}

/* --- *
 * E d i t   C o n s t r a i n t
 * --- */

export class EditConstraint extends UnaryConstraint {
    /**
     * A unary input constraint used to mark a variable that the client
     * wishes to change.
     */

    /**
     * Edits indicate that a variable is to be changed by imperative code.
     */
    isInput() {
        return true;
    }

    execute() {
        // Edit constraints do nothing
    }
}

/* --- *
 * B i n a r y   C o n s t r a i n t
 * --- */

var Direction = new Object();
Direction.NONE = 0;
Direction.FORWARD = 1;
Direction.BACKWARD = -1;

export class BinaryConstraint extends Constraint {
    /**
     * Abstract superclass for constraints having two possible output
     * variables.
     */
    constructor(var1, var2, strength, planner) {
        super(strength, planner);
        this.v1 = var1;
        this.v2 = var2;
        this.direction = Direction.NONE;
    }

    /**
     * Decides if this constraint can be satisfied and which way it
     * should flow based on the relative strength of the variables related,
     * and record that decision.
     */
    chooseMethod(mark) {
        if (this.v1.mark == mark) {
            if (this.v2.mark != mark &&
                Strength.stronger(this.strength, this.v2.walkStrength)) {
                this.direction = Direction.FORWARD;
            } else {
                this.direction = Direction.NONE;
            }
        }
        if (this.v2.mark == mark) {
            if (this.v1.mark != mark &&
                Strength.stronger(this.strength, this.v1.walkStrength)) {
                this.direction = Direction.BACKWARD;
            } else {
                this.direction = Direction.NONE;
            }
        }
        if (Strength.weaker(this.v1.walkStrength, this.v2.walkStrength)) {
            this.direction = Strength.stronger(this.strength, this.v1.walkStrength) ?
                Direction.BACKWARD :
                Direction.NONE;
        } else {
            this.direction = Strength.stronger(this.strength, this.v2.walkStrength) ?
                Direction.FORWARD :
                Direction.BACKWARD;
        }
    }

    /**
     * Add this constraint to the constraint graph
     */
    addToGraph() {
        this.v1.addConstraint(this);
        this.v2.addConstraint(this);
        this.direction = Direction.NONE;
    }

    /**
     * Answer true if this constraint is satisfied in the current solution.
     */
    isSatisfied() {
        return this.direction != Direction.NONE;
    }

    /**
     * Mark the input variable with the given mark.
     */
    markInputs(mark) {
        this.input().mark = mark;
    }

    /**
     * Returns the current input variable
     */
    input() {
        return (this.direction == Direction.FORWARD) ? this.v1 : this.v2;
    }

    /**
     * Returns the current output variable
     */
    output() {
        return (this.direction == Direction.FORWARD) ? this.v2 : this.v1;
    }

    /**
     * Calculate the walkabout strength, the stay flag, and, if it is
     * 'stay', the value for the current output of this
     * constraint. Assume this constraint is satisfied.
     */
    recalculate() {
        var ihn = this.input(), out = this.output();
        out.walkStrength = Strength.weakestOf(this.strength, ihn.walkStrength);
        out.stay = ihn.stay;
        if (out.stay) this.execute();
    }

    /**
     * Record the fact that this constraint is unsatisfied.
     */
    markUnsatisfied() {
        this.direction = Direction.NONE;
    }

    inputsKnown(mark) {
        var i = this.input();
        return i.mark == mark || i.stay || i.determinedBy == null;
    }

    removeFromGraph() {
        if (this.v1 != null) this.v1.removeConstraint(this);
        if (this.v2 != null) this.v2.removeConstraint(this);
        this.direction = Direction.NONE;
    }
}
export class UserConstraint extends Constraint {
    /**
     * Constraints that use a custom function to map multiple inputs to one output
     */
    constructor(strengthOrPredicateOrFormulas,
                predicateOrFormulasOrPlanner,
                formulasOrPlanner,
                planner) {
        var strength, formulas, predicate;
        if (planner) { // 4-args
            strength = strengthOrPredicateOrFormulas;
            predicate = predicateOrFormulasOrPlanner;
            formulas = formulasOrPlanner;
        } else if (formulasOrPlanner) {
            // 3-args
            if (typeof(formulasOrPlanner) == 'function') {
                strength = strengthOrPredicateOrFormulas;
                predicate = predicateOrFormulasOrPlanner;
                formulas = formulasOrPlanner;
            } else {
                planner = formulasOrPlanner;
                if (typeof(strengthOrPredicateOrFormulas) == 'function') {
                    predicate = strengthOrPredicateOrFormulas;
                    formulas = predicateOrFormulasOrPlanner;
                } else {
                    strength = strengthOrPredicateOrFormulas;
                    formulas = predicateOrFormulasOrPlanner;
                }
            }
        } else if (predicateOrFormulasOrPlanner) {
            // 2-args
            if (typeof(strengthOrPredicateOrFormulas) == 'function') {
                if (typeof(predicateOrFormulasOrPlanner) == 'function') {
                    predicate = strengthOrPredicateOrFormulas;
                    formulas = predicateOrFormulasOrPlanner;
                } else {
                    formulas = strengthOrPredicateOrFormulas;
                    planner = predicateOrFormulasOrPlanner;
                }
            } else {
                strength = strengthOrPredicateOrFormulas;
                formulas = predicateOrFormulasOrPlanner;
            }
        } else {
            // 1-arg
            formulas = strengthOrPredicateOrFormulas;
        }
        strength = strength || Strength.required;

        super(strength, planner);
        this.predicate = predicate;
        this.formulas = [];
        this.outputs = [];
        this.inputs = [];
        this.satisfied = false;
        formulas(this);
    }
    formula(output, inputs, func) {
        if (inputs.include(output)) {
            throw 'output cannot be determined by itself';
        }
        var idx = this.outputs.indexOf(output),
            len = this.outputs.length;
        if (idx >= 0) {
            throw 'multiple formulas for ' + output;
        }
        this.outputs.push(output);
        inputs.each(function(input) {
            if (!this.inputs.include(input)) {
                this.inputs.push(input);
            }
        }.bind(this));
        this.formulas[len] = {inputs: inputs, func: func};
    }


    /**
     * Decides if this constraint can be satisfied and which way it
     * should flow based on the relative strength of the variables related,
     * and record that decision.
     */
    chooseMethod(mark) {
        var weakest_output = null,
            weakest_strength = this.strength,
            out = null;
        this.outputs.each(function(out) {
            if (out.mark != mark &&
                Strength.stronger(weakest_strength, out.walkStrength)) {
                weakest_strength = out.walkStrength;
                weakest_output = out;
            }
        }.bind(this));
        this.out = weakest_output;
        this.satisfied = (!!this.out);
    }

    /**
     * Add this constraint to the constraint graph
     */
    addToGraph() {
        var that = this;
            this.variables.each(function(ea) {
                ea.addConstraint(that);
            });
            this.satisfied = false;
    }

    /**
     * Answer true if this constraint is satisfied in the current solution.
     */
    isSatisfied() {
        return this.satisfied;
    }

    /**
     * Mark the input variable with the given mark.
     */
    markInputs(mark) {
        var out = this.out;
        this.inputs.each(function(ea) {
            if (ea !== out) {
               ea.mark = mark;
            }
        });
    }

    /**
     * Calculate the walkabout strength, the stay flag, and, if it is
     * 'stay', the value for the current output of this
     * constraint. Assume this constraint is satisfied.
     */
    recalculate() {
        var out = this.out;
        out.walkStrength = this.strength;
        this.inputs.each(function(ea) {
            out.walkStrength = Strength.weakestOf(
                out.walkStrength,
                ea.walkStrength
            );
        });
        out.stay = this.inputs.all(function(ea) { return ea.stay });
        if (out.stay) this.execute();
    }

    /**
     * Record the fact that this constraint is unsatisfied.
     */
    markUnsatisfied() {
        this.satisfied = false;
    }

    inputsKnown(mark) {
        var out = this.out;
        return this.inputs.all(function(i) {
           return i === out || i.mark == mark || i.stay || i.determinedBy == null;
        });
    }

    removeFromGraph() {
        var that = this;
        this.variables.each(function(ea) {
            ea.removeConstraint(that);
        });
        this.satisfied = false;
    }
    execute() {
        if (!this.predicate || !this.predicate()) {
            var formula = this.formulas[this.outputs.indexOf(this.out)],
                func = formula.func,
                inputs = formula.inputs;
            this.out.value = func.apply(null, inputs.collect(function(ea) {
                return ea.value;
            }).concat([this.out.value]));
        }
    }
    output() {
        return this.out;
    }
    get variables() {
        return this.outputs.concat(this.inputs).uniq();
    }

}
/* --- *
 * S c a l e   C o n s t r a i n t
 * --- */

export class ScaleConstraint extends BinaryConstraint {
    /**
     * Relates two variables by the linear scaling relationship: "v2 =
     * (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
     * this relationship but the scale factor and offset are considered
     * read-only.
     */
    constructor(src, scale, offset, dest, strength, planner) {
        super(src, dest, strength, planner);
        this.direction = Direction.NONE;
        this.scale = scale;
        this.offset = offset;
    }

    /**
     * Adds this constraint to the constraint graph.
     */
    addToGraph() {
        super.addToGraph();
        this.scale.addConstraint(this);
        this.offset.addConstraint(this);
    }

    removeFromGraph() {
        super.removeFromGraph();
        if (this.scale != null) this.scale.removeConstraint(this);
        if (this.offset != null) this.offset.removeConstraint(this);
    }

    markInputs(mark) {
        super.markInputs(mark);
        this.scale.mark = this.offset.mark = mark;
    }

    /**
     * Enforce this constraint. Assume that it is satisfied.
     */
    execute() {
        if (this.direction == Direction.FORWARD) {
            this.v2.value = this.v1.value * this.scale.value + this.offset.value;
        } else {
            this.v1.value = (this.v2.value - this.offset.value) / this.scale.value;
        }
    }

    /**
     * Calculate the walkabout strength, the stay flag, and, if it is
     * 'stay', the value for the current output of this constraint. Assume
     * this constraint is satisfied.
     */
    recalculate() {
        var ihn = this.input(), out = this.output();
        out.walkStrength = Strength.weakestOf(this.strength, ihn.walkStrength);
        out.stay = ihn.stay && this.scale.stay && this.offset.stay;
        if (out.stay) this.execute();
    }
}

/* --- *
 * E q u a l i t  y   C o n s t r a i n t
 * --- */

export class EqualityConstraint extends BinaryConstraint {
    /**
     * Constrains two variables to have the same value.
     */


    /**
     * Enforce this constraint. Assume that it is satisfied.
     */
    execute() {
        this.output().value = this.input().value;
    }
}

/* --- *
 * V a r i a b l e
 * --- */

export class Variable {
    /**
     * A constrained variable. In addition to its value, it maintain the
     * structure of the constraint graph, the current dataflow graph, and
     * various parameters of interest to the DeltaBlue incremental
     * constraint solver.
     **/
    constructor(name, initialValue, planner) {
        this.planner = planner;
        this.value = initialValue;
        this.constraints = new OrderedCollection();
        this.determinedBy = null;
        this.mark = 0;
        this.walkStrength = Strength.WEAKEST;
        this.stay = true;
        this.name = name;
    }

    /**
     * Add the given constraint to the set of all constraints that refer
     * this variable.
     */
    addConstraint(c) {
        this.constraints.add(c);
    }

    /**
     * Removes all traces of c from this variable.
     */
    removeConstraint(c) {
        this.constraints.remove(c);
        if (this.determinedBy == c) this.determinedBy = null;
    }
    assignValue(newValue, optionalPriority) {
        var priority = optionalPriority || Strength.REQUIRED;
        var edit = new EditConstraint(this, priority, this.planner),
            edits = new OrderedCollection();
        edit.addConstraint();
        edits.add(edit);
        var plan = this.planner.extractPlanFromConstraints(edits);
        this.value = newValue;
        try {
            plan.execute();
        } finally {
            edit.destroyConstraint();
        }
    }
}

/* --- *
 * P l a n n e r
 * --- */

export class Planner {
    /**
     * The DeltaBlue planner
     */
    constructor() {
        this.currentMark = 0;
    }

    /**
     * Attempt to satisfy the given constraint and, if successful,
     * incrementally update the dataflow graph.  Details: If satifying
     * the constraint is successful, it may override a weaker constraint
     * on its output. The algorithm attempts to resatisfy that
     * constraint using some other method. This process is repeated
     * until either a) it reaches a variable that was not previously
     * determined by any constraint or b) it reaches a constraint that
     * is too weak to be satisfied using any of its methods. The
     * variables of constraints that have been processed are marked with
     * a unique mark value so that we know where we've been. This allows
     * the algorithm to avoid getting into an infinite loop even if the
     * constraint graph has an inadvertent cycle.
     */
    incrementalAdd(c) {
        var mark = this.newMark();
        var overridden = c.satisfy(mark);
        while (overridden != null)
            overridden = overridden.satisfy(mark);
    }

    /**
     * Entry point for retracting a constraint. Remove the given
     * constraint and incrementally update the dataflow graph.
     * Details: Retracting the given constraint may allow some currently
     * unsatisfiable downstream constraint to be satisfied. We therefore collect
     * a list of unsatisfied downstream constraints and attempt to
     * satisfy each one in turn. This list is traversed by constraint
     * strength, strongest first, as a heuristic for avoiding
     * unnecessarily adding and then overriding weak constraints.
     * Assume: c is satisfied.
     */
    incrementalRemove(c) {
        var out = c.output();
        c.markUnsatisfied();
        c.removeFromGraph();
        var unsatisfied = this.removePropagateFrom(out);
        var strength = Strength.REQUIRED;
        do {
            for (var i = 0; i < unsatisfied.size(); i++) {
                var u = unsatisfied.at(i);
                if (u.strength == strength)
                    this.incrementalAdd(u);
            }
            strength = strength.nextWeaker();
        } while (strength != Strength.WEAKEST);
    }

    /**
     * Select a previously unused mark value.
     */
    newMark() {
        return ++this.currentMark;
    }

    /**
     * Extract a plan for resatisfaction starting from the given source
     * constraints, usually a set of input constraints. This method
     * assumes that stay optimization is desired; the plan will contain
     * only constraints whose output variables are not stay. Constraints
     * that do no computation, such as stay and edit constraints, are
     * not included in the plan.
     * Details: The outputs of a constraint are marked when it is added
     * to the plan under construction. A constraint may be appended to
     * the plan when all its input variables are known. A variable is
     * known if either a) the variable is marked (indicating that has
     * been computed by a constraint appearing earlier in the plan), b)
     * the variable is 'stay' (i.e. it is a constant at plan execution
     * time), or c) the variable is not determined by any
     * constraint. The last provision is for past states of history
     * variables, which are not stay but which are also not computed by
     * any constraint.
     * Assume: sources are all satisfied.
     */
    makePlan(sources) {
        var mark = this.newMark();
        var plan = new Plan();
        var todo = sources;
        while (todo.size() > 0) {
            var c = todo.removeFirst();
            if (c.output().mark != mark && c.inputsKnown(mark)) {
                plan.addConstraint(c);
                c.output().mark = mark;
                this.addConstraintsConsumingTo(c.output(), todo, c);
            }
        }
        return plan;
    }

    /**
     * Extract a plan for resatisfying starting from the output of the
     * given constraints, usually a set of input constraints.
     */
    extractPlanFromConstraints(constraints) {
        var sources = new OrderedCollection();
        for (var i = 0; i < constraints.size(); i++) {
            var c = constraints.at(i);
            if (c.isInput() && c.isSatisfied())
                // not in plan already and eligible for inclusion
                sources.add(c);
        }
        return this.makePlan(sources);
    }

    /**
     * Recompute the walkabout strengths and stay flags of all variables
     * downstream of the given constraint and recompute the actual
     * values of all variables whose stay flag is true. If a cycle is
     * detected, remove the given constraint and answer
     * false. Otherwise, answer true.
     * Details: Cycles are detected when a marked variable is
     * encountered downstream of the given constraint. The sender is
     * assumed to have marked the inputs of the given constraint with
     * the given mark. Thus, encountering a marked node downstream of
     * the output constraint means that there is a path from the
     * constraint's output to one of its inputs.
     */
    addPropagate(c, mark) {
        var todo = new OrderedCollection();
        todo.add(c);
        while (todo.size() > 0) {
            var d = todo.removeFirst();
            if (d.output().mark == mark) {
                this.incrementalRemove(c);
                return false;
            }
            d.recalculate();
            this.addConstraintsConsumingTo(d.output(), todo);
        }
        return true;
    }


    /**
     * Update the walkabout strengths and stay flags of all variables
     * downstream of the given constraint. Answer a collection of
     * unsatisfied constraints sorted in order of decreasing strength.
     */
    removePropagateFrom(out) {
        out.determinedBy = null;
        out.walkStrength = Strength.WEAKEST;
        out.stay = true;
        var unsatisfied = new OrderedCollection();
        var todo = new OrderedCollection();
        todo.add(out);
        while (todo.size() > 0) {
            var v = todo.removeFirst();
            for (var i = 0; i < v.constraints.size(); i++) {
                var c = v.constraints.at(i);
                if (!c.isSatisfied())
                    unsatisfied.add(c);
            }
            var determining = v.determinedBy;
            for (var i = 0; i < v.constraints.size(); i++) {
                var next = v.constraints.at(i);
                if (next != determining && next.isSatisfied()) {
                    next.recalculate();
                    todo.add(next.output());
                }
            }
        }
        return unsatisfied;
    }

    addConstraintsConsumingTo(v, coll, not) {
        var determining = v.determinedBy;
        var cc = v.constraints;
        for (var i = 0; i < cc.size(); i++) {
            var c = cc.at(i);
            if (c != determining && c.isSatisfied() && c != not) {
                coll.add(c);
            }
        }
    }
}
/* --- *
 * P l a n
 * --- */

export class Plan {
    /**
     * A Plan is an ordered list of constraints to be executed in sequence
     * to resatisfy all currently satisfiable constraints in the face of
     * one or more changing inputs.
     */
    constructor() {
        this.v = new OrderedCollection();
    }

    addConstraint(c) {
        this.v.add(c);
    }

    size() {
        return this.v.size();
    }

    constraintAt(index) {
        return this.v.at(index);
    }

    execute() {
        for (var i = 0; i < this.size(); i++) {
            var c = this.constraintAt(i);
            c.execute();
        }
    }
}