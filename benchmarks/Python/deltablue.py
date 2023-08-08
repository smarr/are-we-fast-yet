# This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
# DeltaBlue.
#
# It is modified to use the SOM class library and Java 8 features.
# License details:
#   http://web.archive.org/web/20050825101121/
#      http://www.sunlabs.com/people/mario/java_benchmarking/index.html
from abc import abstractmethod
from enum import Enum

from benchmark import Benchmark
from som.identity_dictionary import IdentityDictionary
from som.vector import Vector, vector_with


class DeltaBlue(Benchmark):
    def inner_benchmark_loop(self, inner_iterations):
        _Planner.chain_test(inner_iterations)
        _Planner.projection_test(inner_iterations)
        return True

    def benchmark(self):
        raise Exception("should never be reached")

    def verify_result(self, result):
        raise Exception("should never be reached")


class _Plan(Vector):
    def __init__(self):
        super().__init__(15)

    def execute(self):
        self.for_each(lambda c: c.execute())


class _Planner:
    def __init__(self):
        self._current_mark = 1

    # Attempt to satisfy the given constraint and, if successful,
    # incrementally update the dataflow graph. Details: If satifying
    # the constraint is successful, it may override a weaker constraint
    # on its output. The algorithm attempts to resatisfy that
    # constraint using some other method. This process is repeated
    # until either a) it reaches a variable that was not previously
    # determined by any constraint or b) it reaches a constraint that
    # is too weak to be satisfied using any of its methods. The
    # variables of constraints that have been processed are marked with
    # a unique mark value so that we know where we've been. This allows
    # the algorithm to avoid getting into an infinite loop even if the
    # constraint graph has an inadvertent cycle.
    def incremental_add(self, c):
        mark = self._new_mark()
        overridden = c.satisfy(mark, self)

        while overridden is not None:
            overridden = overridden.satisfy(mark, self)

    # Entry point for retracting a constraint. Remove the given
    # constraint and incrementally update the dataflow graph.
    # Details: Retracting the given constraint may allow some currently
    # unsatisfiable downstream constraint to be satisfied. We therefore collect
    # a list of unsatisfied downstream constraints and attempt to
    # satisfy each one in turn. This list is traversed by constraint
    # strength, strongest first, as a heuristic for avoiding
    # unnecessarily adding and then overriding weak constraints.
    # Assume: c is satisfied.
    def incremental_remove(self, c):
        out = c.get_output()
        c.mark_unsatisfied()
        c.remove_from_graph()

        unsatisfied = self._remove_propagate_from(out)
        unsatisfied.for_each(self.incremental_add)

    # Extract a plan for resatisfaction starting from the outputs of
    # the given constraints, usually a set of input constraints.

    def extract_plan_from_constraints(self, constraints):
        sources = Vector()

        def each(c):
            if c.is_input() and c.is_satisfied():
                sources.append(c)

        constraints.for_each(each)

        return self._make_plan(sources)

    # Extract a plan for resatisfaction starting from the given source
    # constraints, usually a set of input constraints. This method
    # assumes that stay optimization is desired; the plan will contain
    # only constraints whose output variables are not stay. Constraints
    # that do no computation, such as stay and edit constraints, are
    # not included in the plan.
    # Details: The outputs of a constraint are marked when it is added
    # to the plan under construction. A constraint may be appended to
    # the plan when all its input variables are known. A variable is
    # known if either a) the variable is marked (indicating that has
    # been computed by a constraint appearing earlier in the plan), b)
    # the variable is 'stay' (i.e. it is a constant at plan execution
    # time), or c) the variable is not determined by any
    # constraint. The last provision is for past states of history
    # variables, which are not stay but which are also not computed by
    # any constraint.
    # Assume: sources are all satisfied.
    def _make_plan(self, sources):
        mark = self._new_mark()
        plan = _Plan()
        todo = sources

        while not todo.is_empty():
            c = todo.remove_first()

            if c.get_output().mark != mark and c.inputs_known(mark):
                # not in plan already and eligible for inclusion
                plan.append(c)
                c.get_output().mark = mark
                self._add_constraints_consuming_to(c.get_output(), todo)
        return plan

    # The given variable has changed. Propagate new values downstream.
    def propagate_from(self, v):
        todo = Vector()
        self._add_constraints_consuming_to(v, todo)

        while not todo.is_empty():
            c = todo.remove_first()
            c.execute()
            self._add_constraints_consuming_to(c.get_output(), todo)

    @staticmethod
    def _add_constraints_consuming_to(v, coll):
        determining_c = v.determined_by

        def each(c):
            if c is not determining_c and c.is_satisfied():
                coll.append(c)

        v.constraints.for_each(each)

    # Recompute the walkabout strengths and stay flags of all variables
    # downstream of the given constraint and recompute the actual
    # values of all variables whose stay flag is true. If a cycle is
    # detected, remove the given constraint and answer
    # false. Otherwise, answer true.
    # Details: Cycles are detected when a marked variable is
    # encountered downstream of the given constraint. The sender is
    # assumed to have marked the inputs of the given constraint with
    # the given mark. Thus, encountering a marked node downstream of
    # the output constraint means that there is a path from the
    # constraint's output to one of its inputs.
    def add_propagate(self, c, mark):
        todo = vector_with(c)

        while not todo.is_empty():
            d = todo.remove_first()

            if d.get_output().mark == mark:
                self.incremental_remove(c)
                return False

            d.recalculate()
            self._add_constraints_consuming_to(d.get_output(), todo)

        return True

    def change(self, var, new_value):
        edit_c = _EditConstraint(var, _PREFERRED, self)

        edit_v = vector_with(edit_c)
        plan = self.extract_plan_from_constraints(edit_v)
        for _ in range(10):
            var.value = new_value
            plan.execute()

        edit_c.destroy_constraint(self)

    @staticmethod
    def _constraints_consuming(v, fn):
        determining_c = v.determined_by

        def each(c):
            if c is not determining_c and c.is_satisfied():
                fn(c)

        v.constraints.for_each(each)

    # Select a previously unused mark value.
    def _new_mark(self):
        self._current_mark += 1
        return self._current_mark

    # Update the walkabout strengths and stay flags of all variables
    # downstream of the given constraint. Answer a collection of
    # unsatisfied constraints sorted in order of decreasing strength.
    def _remove_propagate_from(self, out):
        unsatisfied = Vector()

        out.determined_by = None
        out.walk_strength = _absolute_weakest
        out.stay = True

        todo = vector_with(out)

        while not todo.is_empty():
            v = todo.remove_first()

            def each(c):
                if not c.is_satisfied():
                    unsatisfied.append(c)

            v.constraints.for_each(each)

            def recalc(c):
                c.recalculate()
                todo.append(c.get_output())

            self._constraints_consuming(v, recalc)

        def comp(c1, c2):
            return -1 if c1.strength.stronger(c2.strength) else 1

        unsatisfied.sort(comp)
        return unsatisfied

    # This is the standard DeltaBlue benchmark. A long chain of
    # equality constraints is constructed with a stay constraint on
    # one end. An edit constraint is then added to the opposite end
    # and the time is measured for adding and removing this
    # constraint, and extracting and executing a constraint
    # satisfaction plan. There are two cases. In case 1, the added
    # constraint is stronger than the stay constraint and values must
    # propagate down the entire length of the chain. In case 2, the
    # added constraint is weaker than the stay constraint so it cannot
    # be accomodated. The cost in this case is, of course, very
    # low. Typical situations lie somewhere between these two
    # extremes.
    @staticmethod
    def chain_test(n):
        planner = _Planner()
        variables = [None] * (n + 1)
        for i in range(n + 1):
            variables[i] = _Variable()

        # Build chain of n equality constraints
        for i in range(n):
            v1 = variables[i]
            v2 = variables[i + 1]
            _EqualityConstraint(v1, v2, _REQUIRED, planner)

        _StayConstraint(variables[n], _STRONG_DEFAULT, planner)
        edit_c = _EditConstraint(variables[0], _PREFERRED, planner)

        edit_v = vector_with(edit_c)
        plan = planner.extract_plan_from_constraints(edit_v)
        for i in range(100):
            variables[0].value = i
            plan.execute()
            if variables[n].value != i:
                raise Exception("Chain test failed!")

        edit_c.destroy_constraint(planner)

    # This test constructs a two sets of variables related to each
    # other by a simple linear transformation (scale and offset). The
    # time is measured to change a variable on either side of the
    # mapping and to change the scale and offset factors.
    @staticmethod
    def projection_test(n):
        planner = _Planner()

        dests = Vector()

        scale = _Variable(10)
        offset = _Variable(1000)

        src = None
        dst = None
        for i in range(1, n + 1):
            src = _Variable(i)
            dst = _Variable(i)
            dests.append(dst)
            _StayConstraint(src, _DEFAULT, planner)
            _ScaleConstraint(src, scale, offset, dst, _REQUIRED, planner)

        planner.change(src, 17)
        if dst.value != 1170:
            raise Exception("Projection test 1 failed!")

        planner.change(dst, 1050)
        if src.value != 5:
            raise Exception("Projection test 2 failed!")

        planner.change(scale, 5)
        for i in range(n - 1):
            if dests.at(i).value != (i + 1) * 5 + 1000:
                raise Exception("Projection test 3 failed!")

        planner.change(offset, 2000)
        for i in range(n - 1):
            if dests.at(i).value != (i + 1) * 5 + 2000:
                raise Exception("Projection test 4 failed!")


class _Sym:
    def __init__(self, hash_):
        self._hash = hash_

    def custom_hash(self):
        return self._hash


_ABSOLUTE_STRONGEST = _Sym(0)
_REQUIRED = _Sym(1)
_STRONG_PREFERRED = _Sym(2)
_PREFERRED = _Sym(3)
_STRONG_DEFAULT = _Sym(4)
_DEFAULT = _Sym(5)
_WEAK_DEFAULT = _Sym(6)
_ABSOLUTE_WEAKEST = _Sym(7)


class _Strength:
    def __init__(self, strength_sym):
        self._symbolic_value = strength_sym
        self.arithmetic_value = _strength_table.at(strength_sym)

    def same_as(self, s):
        return self.arithmetic_value == s.arithmetic_value

    def stronger(self, s):
        return self.arithmetic_value < s.arithmetic_value

    def weaker(self, s):
        return self.arithmetic_value > s.arithmetic_value

    def strongest(self, s):
        return s if s.stronger(self) else self

    def weakest(self, s):
        return s if s.weaker(self) else self

    @staticmethod
    def of(strength):
        return _strength_constant.at(strength)


def _create_strength_table():
    strength_table = IdentityDictionary()
    strength_table.at_put(_ABSOLUTE_STRONGEST, -10000)
    strength_table.at_put(_REQUIRED, -800)
    strength_table.at_put(_STRONG_PREFERRED, -600)
    strength_table.at_put(_PREFERRED, -400)
    strength_table.at_put(_STRONG_DEFAULT, -200)
    strength_table.at_put(_DEFAULT, 0)
    strength_table.at_put(_WEAK_DEFAULT, 500)
    strength_table.at_put(_ABSOLUTE_WEAKEST, 10000)
    return strength_table


def _create_strength_constants():
    strength_constant = IdentityDictionary()
    _strength_table.get_keys().for_each(
        lambda key: strength_constant.at_put(key, _Strength(key))
    )
    return strength_constant


_strength_table = _create_strength_table()
_strength_constant = _create_strength_constants()

_absolute_weakest = _Strength.of(_ABSOLUTE_WEAKEST)
_required = _Strength.of(_REQUIRED)


class _Direction(Enum):
    FORWARD = 1
    BACKWARD = 2


# ------------------------ constraints ------------------------------------


# I am an abstract class representing a system-maintainable
# relationship (or "constraint") between a set of variables. I supply
# a strength instance variable; concrete subclasses provide a means
# of storing the constrained variables and other information required
# to represent a constraint.
class _AbstractConstraint:
    def __init__(self, strength):
        self.strength = _Strength.of(strength)

    # Normal constraints are not input constraints. An input constraint
    # is one that depends on external state, such as the mouse, the
    # keyboard, a clock, or some arbitrary piece of imperative code.
    def is_input(self):
        return False

    # Answer true if this constraint is satisfied in the current solution.
    @abstractmethod
    def is_satisfied(self):
        pass

    # Activate this constraint and attempt to satisfy it.
    def add_constraint(self, planner):
        self.add_to_graph()
        planner.incremental_add(self)

    # Add myself to the constraint graph.
    @abstractmethod
    def add_to_graph(self):
        pass

    # Deactivate this constraint, remove it from the constraint graph,
    # possibly causing other constraints to be satisfied, and destroy it.
    def destroy_constraint(self, planner):
        if self.is_satisfied():
            planner.incremental_remove(self)

        self.remove_from_graph()

    # Remove myself from the constraint graph.
    @abstractmethod
    def remove_from_graph(self):
        pass

    # Decide if I can be satisfied and record that decision. The output
    # of the chosen method must not have the given mark and must have
    # a walkabout strength less than that of this constraint.
    @abstractmethod
    def choose_method(self, mark):
        pass

    # Enforce this constraint. Assume that it is satisfied.
    @abstractmethod
    def execute(self):
        pass

    @abstractmethod
    def inputs_do(self, fn):
        pass

    @abstractmethod
    def inputs_has_one(self, fn):
        pass

    # Assume that I am satisfied. Answer true if all my current inputs
    # are known. A variable is known if either a) it is 'stay' (i.e. it
    # is a constant at plan execution time), b) it has the given mark
    # (indicating that it has been computed by a constraint appearing
    # earlier in the plan), or c) it is not determined by any
    # constraint.
    def inputs_known(self, mark):
        return not self.inputs_has_one(
            lambda v: not (v.mark == mark or v.stay or v.determined_by is None)
        )

    # Record the fact that I am unsatisfied.
    @abstractmethod
    def mark_unsatisfied(self):
        pass

    # Answer my current output variable. Raise an error if I am not
    # currently satisfied.
    @abstractmethod
    def get_output(self):
        pass

    # Calculate the walkabout strength, the stay flag, and, if it is
    # 'stay', the value for the current output of this
    # constraint. Assume this constraint is satisfied.
    @abstractmethod
    def recalculate(self):
        pass

    # Attempt to find a way to enforce this constraint. If successful,
    # record the solution, perhaps modifying the current dataflow
    # graph. Answer the constraint that this constraint overrides, if
    # there is one, or nil, if there isn't.
    # Assume: I am not already satisfied.
    def satisfy(self, mark, planner):
        self.choose_method(mark)

        if self.is_satisfied():
            # constraint can be satisfied
            # mark inputs to allow cycle detection in addPropagate
            def each(input_):
                input_.mark = mark

            self.inputs_do(each)

            out = self.get_output()
            overridden = out.determined_by
            if overridden is not None:
                overridden.mark_unsatisfied()

            out.determined_by = self
            if not planner.add_propagate(self, mark):
                raise Exception("Cycle encountered")

            out.mark = mark
        else:
            overridden = None
            if self.strength.same_as(_required):
                raise Exception("Could not satisfy a required constraint")

        return overridden


# I am an abstract superclass for constraints having two possible
# output variables.
class _BinaryConstraint(_AbstractConstraint):
    def __init__(self, var1, var2, strength, _planner):
        super().__init__(strength)
        self._v1 = var1
        self._v2 = var2
        self._direction = None

    # Answer true if this constraint is satisfied in the current solution.
    def is_satisfied(self):
        return self._direction is not None

    # Add myself to the constraint graph.
    def add_to_graph(self):
        self._v1.add_constraint(self)
        self._v2.add_constraint(self)
        self._direction = None

    # Remove myself from the constraint graph.
    def remove_from_graph(self):
        if self._v1 is not None:
            self._v1.remove_constraint(self)
        if self._v2 is not None:
            self._v2.remove_constraint(self)
        self._direction = None

    # Decide if I can be satisfied and which way I should flow based on
    # the relative strength of the variables I relate, and record that
    # decision.
    def choose_method(self, mark):
        if self._v1.mark == mark:
            if self._v2.mark != mark and self.strength.stronger(self._v2.walk_strength):
                self._direction = _Direction.FORWARD
                return self._direction

            self._direction = None
            return self._direction

        if self._v2.mark == mark:
            if self._v1.mark != mark and self.strength.stronger(self._v1.walk_strength):
                self._direction = _Direction.BACKWARD
                return self._direction

            self._direction = None
            return self._direction

        # If we get here, neither variable is marked, so we have a choice.
        if self._v1.walk_strength.weaker(self._v2.walk_strength):
            if self.strength.stronger(self._v1.walk_strength):
                self._direction = _Direction.BACKWARD
                return self._direction

            self._direction = None
            return self._direction

        if self.strength.stronger(self._v2.walk_strength):
            self._direction = _Direction.FORWARD
            return self._direction

        self._direction = None
        return self._direction

    def inputs_do(self, fn):
        if self._direction is _Direction.FORWARD:
            fn(self._v1)
        else:
            fn(self._v2)

    def inputs_has_one(self, fn):
        if self._direction is _Direction.FORWARD:
            return fn(self._v1)

        return fn(self._v2)

    # Record the fact that I am unsatisfied.
    def mark_unsatisfied(self):
        self._direction = None

    # Answer my current output variable.
    def get_output(self):
        return self._v2 if self._direction is _Direction.FORWARD else self._v1

    # Calculate the walkabout strength, the stay flag, and, if it is
    # 'stay', the value for the current output of this
    # constraint. Assume this constraint is satisfied.
    def recalculate(self):
        if self._direction is _Direction.FORWARD:
            input_ = self._v1
            output = self._v2
        else:
            input_ = self._v2
            output = self._v1

        output.walk_strength = self.strength.weakest(input_.walk_strength)
        output.stay = input_.stay
        if output.stay:
            self.execute()


# I am an abstract superclass for constraints having a single
# possible output variable.
class _UnaryConstraint(_AbstractConstraint):
    def __init__(self, v, strength, planner):
        super().__init__(strength)
        self._output = v  # possible output variable
        self._satisfied = False  # true if I am currently satisfied
        self.add_constraint(planner)

    # Answer true if this constraint is satisfied in the current solution.
    def is_satisfied(self):
        return self._satisfied

    # Add myself to the constraint graph.
    def add_to_graph(self):
        self._output.add_constraint(self)
        self._satisfied = False

    # Remove myself from the constraint graph.
    def remove_from_graph(self):
        if self._output is not None:
            self._output.remove_constraint(self)
        self._satisfied = False

    # Decide if I can be satisfied and record that decision.
    def choose_method(self, mark):
        self._satisfied = self._output.mark != mark and self.strength.stronger(
            self._output.walk_strength
        )

    @abstractmethod
    def execute(self):
        pass

    def inputs_do(self, fn):
        # I have no input variables
        pass

    def inputs_has_one(self, fn):
        return False

    # Record the fact that I am unsatisfied.
    def mark_unsatisfied(self):
        self._satisfied = False

    # Answer my current output variable.
    def get_output(self):
        return self._output

    # Calculate the walkabout strength, the stay flag, and, if it is
    # 'stay', the value for the current output of this
    # constraint. Assume this constraint is satisfied."
    def recalculate(self):
        self._output.walk_strength = self.strength
        self._output.stay = not self.is_input()
        if self._output.stay:
            self.execute()  # stay optimization


# I am a unary input constraint used to mark a variable that the
# client wishes to change.
class _EditConstraint(_UnaryConstraint):
    # I indicate that a variable is to be changed by imperative code.
    def is_input(self):
        return True

    def execute(self):  # Edit constraints do nothing.
        pass


# I constrain two variables to have the same value: "v1 = v2".
class _EqualityConstraint(_BinaryConstraint):
    # Install a constraint with the given strength equating the given
    # variables.
    def __init__(self, var1, var2, strength, planner):
        super().__init__(var1, var2, strength, planner)
        self.add_constraint(planner)

    # Enforce this constraint. Assume that it is satisfied.
    def execute(self):
        if self._direction is _Direction.FORWARD:
            self._v2.value = self._v1.value
        else:
            self._v1.value = self._v2.value


# I relate two variables by the linear scaling relationship: "v2 =
# (v1 * scale) + offset". Either v1 or v2 may be changed to maintain
# this relationship but the scale factor and offset are considered
# read-only.
class _ScaleConstraint(_BinaryConstraint):
    def __init__(self, src, scale, offset, dest, strength, planner):
        super().__init__(src, dest, strength, planner)
        self._scale = scale  # scale factor input variable
        self._offset = offset  # offset input variable
        self.add_constraint(planner)

    # Add myself to the constraint graph.
    def add_to_graph(self):
        self._v1.add_constraint(self)
        self._v2.add_constraint(self)
        self._scale.add_constraint(self)
        self._offset.add_constraint(self)
        self._direction = None

    # Remove myself from the constraint graph.
    def remove_from_graph(self):
        if self._v1 is not None:
            self._v1.remove_constraint(self)
        if self._v2 is not None:
            self._v2.remove_constraint(self)
        if self._scale is not None:
            self._scale.remove_constraint(self)
        if self._offset is not None:
            self._offset.remove_constraint(self)
        self._direction = None

    # Enforce this constraint. Assume that it is satisfied.
    def execute(self):
        if self._direction is _Direction.FORWARD:
            self._v2.value = self._v1.value * self._scale.value + self._offset.value
        else:
            self._v1.value = (self._v2.value - self._offset.value) / self._scale.value

    def inputs_do(self, fn):
        if self._direction is _Direction.FORWARD:
            fn(self._v1)
            fn(self._scale)
            fn(self._offset)
        else:
            fn(self._v2)
            fn(self._scale)
            fn(self._offset)

    # Calculate the walkabout strength, the stay flag, and, if it is
    # 'stay', the value for the current output of this
    # constraint. Assume this constraint is satisfied.
    def recalculate(self):
        if self._direction is _Direction.FORWARD:
            input_ = self._v1
            output = self._v2
        else:
            output = self._v1
            input_ = self._v2

        output.walk_strength = self.strength.weakest(input_.walk_strength)
        output.stay = input_.stay and self._scale.stay and self._offset.stay
        if output.stay:
            self.execute()  # stay optimization


# I mark variables that should, with some level of preference, stay
# the same. I have one method with zero inputs and one output, which
# does nothing. Planners may exploit the fact that, if I am
# satisfied, my output will not change during plan execution. This is
# called "stay optimization".
class _StayConstraint(_UnaryConstraint):
    def execute(self):
        pass  # Stay constraints do nothing.


# ------------------------------ variables ------------------------------


# I represent a constrained variable. In addition to my value, I
# maintain the structure of the constraint graph, the current
# dataflow graph, and various parameters of interest to the DeltaBlue
# incremental constraint solver.
class _Variable:
    def __init__(self, value=0):
        self.value = value  # my value; changed by constraints
        self.constraints = Vector(2)  # normal constraints that reference me
        self.determined_by = None  # the constraint that currently determines
        # my value (or null if there isn't one)
        self.mark = 0  # used by the planner to mark constraints
        self.walk_strength = _absolute_weakest  # my walkabout strength
        self.stay = True  # true if I am a planning-time constant

    # Add the given constraint to the set of all constraints that refer to me.
    def add_constraint(self, c):
        self.constraints.append(c)

    # Remove all traces of c from this variable.
    def remove_constraint(self, c):
        self.constraints.remove(c)
        if self.determined_by is c:
            self.determined_by = None
