// This benchmark is derived from Mario Wolczko's Smalltalk version of DeltaBlue.
//
// It is modified to use the SOM class library.
// License details:
//
// http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
//
// Original comment.
//  NAME      DeltaBlue benchmark
//  AUTHOR    John Maloney, heavily modified by Mario Wolczko (Mario.Wolczko@sun.com)
//  FUNCTION  Language implementation benchmark
//  ST-VERSION   4.1
//  PREREQUISITES
//  CONFLICTS
//  DISTRIBUTION   world
//  VERSION  1
//  DATE     2 Oct 1996
// SUMMARY
// This benchmark is an implementation of the DeltaBlue Constraint Solver
// described in `The DeltaBlue Algorithm: An Incremental Constraint
// Hierarchy Solver', by Bjorn N. Freeman-Benson and John Maloney,
// Communications of the ACM, January 1990 (also as University of
// Washington TR 89-08-06)

import "harness" as harness
import "core" as core

type Sym = interface {
  customHash
}

class newSym(hash') {
  method customHash {
    hash'
  }
}

def SymAbsoluteStrongest : Sym = newSym(0)
def SymRequired          : Sym = newSym(1)
def SymStrongPreferred   : Sym = newSym(2)
def SymPreferred         : Sym = newSym(3)
def SymStrongDefault     : Sym = newSym(4)
def SymDefault           : Sym = newSym(5)
def SymWeakDefault       : Sym = newSym(6)
def SymAbsoluteWeakest   : Sym = newSym(7)

def strengthTable: Dictionary = createStrengthTable
def strengthConstants: Dictionary = createStrengthConstants

def AbsoluteStrongest : Strength = strengthOf(SymAbsoluteStrongest)
def AbsoluteWeakest   : Strength = strengthOf(SymAbsoluteWeakest)
def Required          : Strength = strengthOf(SymRequired)

def forward: Direction = newDirection("forward")
def backward: Direction = newDirection("backward")

type Direction = interface {
  directionName
}

class newDirection(name': String) -> Direction {
  method directionName -> String { name' }
}

class newDeltaBlue -> Benchmark {
  inherit harness.newBenchmark

  method innerBenchmarkLoop(innerIterations) {
    plannerChainTest(innerIterations)
    plannerProjectionTest(innerIterations)
    true
  }
}

// A Plan is an ordered list of constraints to be executed in sequence to
// resatisfy all currently satisfiable constraints in the face of one or more
// changing inputs.
class newPlan -> Vector {
  inherit core.Vector(15)

  method execute {
    // Execute my constraints in order
    forEach { c: Constraint -> c.execute }
  }
}

type Planner = interface {
  incrementalAdd(_)
  incrementalRemove(_)
  propagateFrom(_)
  addPropagate(_,_)
}

// This benchmark is an implementation of the DeltaBlue Constraint Solver
// described in `The DeltaBlue Algorithm: An Incremental Constraint
// Hierarchy Solver'', by Bjorn N. Freeman-Benson and John Maloney,
// Communications of the ACM, January 1990 (also as University of
// Washington TR 89-08-06).
//
// To run the benchmark, execute the expression `Planner standardBenchmark`.
class newPlanner -> Planner {
  var currentMark: Number := 1.asInteger

  method incrementalAdd(c: Constraint) -> Done {
    // Attempt to satisfy the given constraint and, if successful,
    // incrementally update the dataflow graph.
    //
    // Details: If satifying the constraint is successful, it may override a
    // weaker constraint on its output. The algorithm attempts to resatisfy
    // that constraint using some other method. This process is repeated
    // until either a) it reaches a variable that was not previously
    // determined by any constraint or b) it reaches a constraint that
    // is too weak to be satisfied using any of its methods. The variables
    // of constraints that have been processed are marked with a unique mark
    // value so that we know where we've been. This allows the algorithm to
    // avoid getting into an infinite loop even if the constraint graph has
    // an inadvertent cycle.
    def mark: Number = newMark
    var overridden: Constraint := c.satisfy(mark) propagate(self)
    { overridden == Done }.whileFalse {
        overridden := overridden.satisfy(mark)propagate(self)
    }
  }

  method incrementalRemove(c: Constraint) -> Done {
    // Entry point for retracting a constraint. Remove the given constraint,
    // which should be satisfied, and incrementally update the dataflow
    // graph.
    //
    // Details: Retracting the given constraint may allow some currently
    // unsatisfiable downstream constraint be satisfied. We thus collect a
    // list of unsatisfied downstream constraints and attempt to satisfy
    // each one in turn. This list is sorted by constraint strength,
    // strongest first, as a heuristic for avoiding unnecessarily adding
    // and then overriding weak constraints.
    def out: Variable = c.output
    c.markUnsatisfied
    c.removeFromGraph
    def unsatisfied: Vector = removePropagateFrom(out)
    unsatisfied.forEach { u: Constraint -> incrementalAdd(u) }
  }

  method extractPlanFromConstraints(constraints: Vector) -> Plan {
    // Extract a plan for resatisfaction starting from the outputs of the
    // given constraints, usually a set of input constraints.
    def sources: Vector = newVector()
    constraints.forEach { c: Constraint ->
      (c.isInput.and { c.isSatisfied }).ifTrue { sources.append(c) } }
    return makePlan(sources)
  }

  method makePlan(sources: Vector) -> Plan {
    // Extract a plan for resatisfaction starting from the given satisfied
    // source constraints, usually a set of input constraints. This method
    // assumes that stay optimization is desired; the plan will contain only
    // constraints whose output variables are not stay. Constraints that do
    // no computation, such as stay and edit constraints, are not included
    // in the plan.
    //
    // Details: The outputs of a constraint are marked when it is added to
    // the plan under construction. A constraint may be appended to the plan
    // when all its input variables are known. A variable is known if either
    // a) the variable is marked (indicating that has been computed by a
    // constraint appearing earlier in the plan), b) the variable is 'stay'
    // (i.e. it is a constant at plan execution time), or c) the variable
    // is not determined by any constraint. The last provision is for past
    // states of history variables, which are not stay but which are also
    // not computed by any constraint. *)
    def mark: Number = newMark
    def plan: Plan = newPlan
    def todo: Vector = sources
    { todo.isEmpty }.whileFalse {
       def c: Constraint = todo.removeFirst
       ((c.output.mark != mark).and {
         // not in plan already and...
         c.inputsKnown(mark) }).ifTrue {
           // eligible for inclusion
           plan.append(c)
           c.output.mark(mark)
           addConstraintsConsuming(c.output) to (todo)
       }
    }
    plan
  }

  method propagateFrom(v: Variable) -> Done {
    // The given variable has changed. Propagate new values downstream.
    def todo: Vector = newVector
    addConstraintsConsuming(v)to(todo)
    {todo.isEmpty}.whileFalse {
      def c: Constraint = todo.removeFirst
      c.execute
      addConstraintsConsuming(c.output)to(todo)
    }
  }

  method addConstraintsConsuming(v: Variable)to(aCollection: Vector) -> Done {
    def determiningC: Constraint = v.determinedBy
    v.constraints.forEach { c: Constraint ->
      ((c == determiningC).or { !c.isSatisfied }).ifFalse {
        aCollection.append(c)
      }
    }
  }

  method addPropagate(c: Constraint) mark(mark: Number) -> Boolean {
    // Recompute the walkabout strengths and stay flags of all variables
    // downstream of the given constraint and recompute the actual values
    // of all variables whose stay flag is true. If a cycle is detected,
    // remove the given constraint and answer false. Otherwise, answer true.

    // Details: Cycles are detected when a marked variable is encountered
    // downstream of the given constraint. The sender is assumed to have
    // marked the inputs of the given constraint with the given mark. Thus,
    // encountering a marked node downstream of the output constraint means
    // that there is a path from the constraint's output to one of its
    // inputs
    def todo: Vector = newVectorWith(c)
    { todo.isEmpty }.whileFalse {
      def d: Constraint = todo.removeFirst
      (d.output.mark == mark).ifTrue {
        incrementalRemove(c)
        return false
      }
      d.recalculate
      addConstraintsConsuming(d.output)to(todo)
    }
    true
  }

  method changeVar(aVariable: Variable) newValue(newValue: Number) -> Done {
    def editConstraint: EditConstraint = newEditConstraintVar(aVariable)strength(SymPreferred)addTo(self)
    def plan: Plan = extractPlanFromConstraints(newVectorWith(editConstraint))
    1.asInteger.to(10.asInteger)do { i: Number ->
      aVariable.value(newValue)
      plan.execute
    }
    editConstraint.destroyConstraint(self)
  }

  method constraintsConsuming(v: Variable) do(aBlock: Function) -> Done {
    def determiningC: Constraint = v.determinedBy
    v.constraints.forEach { c: Constraint ->
      ((c == determiningC).or { !c.isSatisfied }).ifFalse {
        aBlock.value(c)
      }
    }
  }

  method newMark -> Number {
    // Select a previously unused mark value.
    //
    // Details: We just keep incrementing. If necessary, the counter will
    // turn into a LargePositiveInteger. In that case, it will be a bit
    // slower to compute the next mark but the algorithms will all behave
    // correctly. We reserve the value '0' to mean 'unmarked'. Thus, this
    // generator starts at '1' and will never produce '0' as a mark value.
    currentMark := currentMark + 1.asInteger
    return currentMark
  }

  method removePropagateFrom(out: Variable) -> Vector {
    // Update the walkabout strengths and stay flags of all variables
    // downstream of the given constraint. Answer a collection of unsatisfied
    // constraints sorted in order of decreasing strength.
    def unsatisfied: Vector = newVector

    out.determinedBy(Done)
    out.walkStrength(AbsoluteWeakest)
    out.stay(true)
    def todo: Vector = newVectorWith(out)
    { todo.isEmpty }.whileFalse {
      def v: Variable = todo.removeFirst
      v.constraints.forEach { c: Constraint ->
        c.isSatisfied.ifFalse { unsatisfied.append(c) }
      }
      constraintsConsuming(v)do { c: Constraint ->
        c.recalculate
        todo.append(c.output)
      }
    }

    unsatisfied.sort { c1: Constraint, c2: Constraint -> c1.strength.stronger(c2.strength) }
    unsatisfied
  }
}

method plannerChainTest(n: Number) -> Done {
  // Do chain-of-equality-constraints performance tests
  def planner: Planner = newPlanner
  def vars: List = platform.kernel.Array.new(n + 1.asInteger)withAll{ newVariable }

  // thread a chain of equality constraints through the variables
  1.asInteger.to(n)do{ i: Number ->
    def v1: Variable = vars.at(i)
    def v2: Variable = vars.at(i + 1.asInteger)
    newEqualityConstraint(v1)v(v2)strength(SymRequired)addTo(planner)
  }

  newStayConstraintVar(vars.last)strength(SymStrongDefault)addTo(planner)
  def editConstraint: EditConstraint = newEditConstraintVar(vars.first)strength(SymPreferred)addTo(planner)
  def plan: Plan = planner.extractPlanFromConstraints(newVectorWith(editConstraint))

  1.asInteger.to(100.asInteger)do{ v: Variable ->
    vars.first.value(v)
    plan.execute
    (vars.last.value != v).ifTrue { error("Chain test failed!!") }
  }

  editConstraint.destroyConstraint(planner)
}

method projectionTest(n: Number) {
  // This test constructs a two sets of variables related to each other by
  // a simple linear transformation (scale and offset).
  def planner: Planner = newPlanner
  def dests: Vector = core.newVector
  def scale: Variable = newVariableValue(10)
  def offset: Variable = newVariableValue(1000)

  1.asInteger.to(n)do{ i: Number ->
    def src: Variable = newVariableValue(i)
    def dst: Variable = newVariableValue(i)
    dests.append(dst)
    newStayConstraint(src)strength(SymDefault)addTo(planner)
    newScaleConstraint(src)scale(scale)offset(offset)dst(dst)strength(SymRequired)addTo(planner)
  }

  planner.changeVar(src)newValue(17)
  (dst.value != 1170).ifTrue { error("Projection test 1 failed!!") }

  planner.changeVar(dst).newValue(1050)
  (src.value != 5).ifTrue { error("Projection test 2 failed!!") }

  planner.changeVar(scale)newValue(5)
  1.asInteger.to(n - 1.asInteger)do { i: Number ->
    (dests.at(i).value != ((i * 5) + 1000).asInteger).ifTrue {
      error("Projection test 3 failed!!")
    }
  }

  planner.changeVar(offset)newValue(2000)
  1.asInteger.to(n - 1.asInteger)do{ i: Number ->
    (dests.at(i).value != ((i * 5) + 2000).asInteger).ifTrue {
      error("Projection test 4 failed!!")
    }
  }
}

type Strength = interface {
  sameAs(_)
  stronger(_)
  weaker(_)
  strongest(_)
  weakest(_)
}

class newStrength(symVal) -> Strength {
  // Strengths are used to measure the relative importance of constraints. The
  // hierarchy of available strengths is determined by the class variable
  // StrengthTable (see my class initialization method). Because Strengths are
  // invariant, references to Strength instances are shared (i.e. all references
  // to `Strength of: SymRequired` point to a single, shared instance). New
  // strengths may be inserted in the strength hierarchy without disrupting
  // current constraints.
  //
  // Instance variables:
  //     symbolicValue      symbolic strength name (e.g. SymRequired) <Symbol>
  //     arithmeticValue    index of the constraint in the hierarchy, used for comparisons <Number>
  def symbolicValue: Sym = symVal
  def arithmeticValue: Number = strengthTable.at(symVal)

  method sameAs(aStrength) -> Boolean {
    // Answer true if I am the same strength as the given Strength.
    arithmeticValue == aStrength.arithmeticValue
  }

  method stronger(aStrength) -> Boolean {
    // Answer true if I am stronger than the given Strength.
    arithmeticValue < aStrength.arithmeticValue
  }

  method weaker(aStrength) -> Boolean {
    // Answer true if I am weaker than the given Strength.
    arithmeticValue > aStrength.arithmeticValue
  }

  method strongest(aStrength) -> Strength {
    // Answer the stronger of myself and aStrength.
    aStrength.stronger(self).
      ifTrue { return aStrength }
      ifFalse { return self }
  }

  method weakest(aStrength) -> Strength {
    // Answer the weaker of myself and aStrength.
    aStrength.weaker(self).
      ifTrue { return aStrength }
      ifFalse { return self }
  }
}

method createStrengthTable -> Dictionary {
  // Initialize the symbolic strength table.
  def table: Dictionary = core.newIdentityDictionary
  table.at(SymAbsoluteStrongest)put(-10000)
  table.at(SymRequired)put(-800)
  table.at(SymStrongPreferred)put(-600)
  table.at(SymPreferred)put(-400)
  table.at(SymStrongDefault)put(-200)
  table.at(SymDefault)put(0)
  table.at(SymWeakDefault)put(500)
  table.at(SymAbsoluteWeakest)put(10000)
  table
}

method createStrengthConstants -> Dictionary {
  def constants: Dictionary = core.newIdentityDictionary
  strengthTable.keys.forEach { strengthSymbol: Sym ->
    constants.
      at(strengthSymbol)
      put(newStrength(strengthSymbol))
  }
  constants
}

method strengthOf(aSymbol: Sym) -> Strength {
  // Answer an instance with the specified strength.
  strengthConstants.at(aSymbol)
}

type Constraint = interface {
  strength
  isInput
  isSatisfied
  addToGraph
  destroyConstraint(_)
  removeFromGraph
  execute
  inputsDo(_)
  inputsHasOne(_)
  inputsKnown(_)
  markUnsatisfied
  output
  recalculate
  satisfy(_,_)
}

class newAbstractConstraint(strengthSymbol) {
  // I am an abstract class representing a system-maintainable relationship
  // (or 'constraint') between a set of variables. I supply a strength instance
  // variable; concrete subclasses provide a means of storing the constrained
  // variables and other information required to represent a constraint.

  // Instance variables:
  //     strength            the strength of this constraint <Strength>
  def strength = strengthOf(strengthSymbol)

  method isInput {
    // Normal constraints are not input constraints. An input constraint is
    // one that depends on external state, such as the mouse, the keyboard,
    // a clock, or some arbitrary piece of imperative code.
    false
  }

  method isSatisfied {
    // Answer true if this constraint is satisfied in the current solution.
    error("AbstractConstraint subclassResponsibility")
  }

  method addConstraint(planner) {
    // Activate this constraint and attempt to satisfy it.
    addToGraph
    planner.incrementalAdd(self)
  }

  method addToGraph {
    // Add myself to the constraint graph.
    error("AbstractConstraint subclassResponsibility")
  }

  method destroyConstraint(planner) {
    // Deactivate this constraint, remove it from the constraint graph,
    // possibly causing other constraints to be satisfied, and destroy it.
    isSatisfied.ifTrue { planner.incrementalRemove(self) }
    removeFromGraph
  }

  method removeFromGraph {
    // Remove myself from the constraint graph.
    error("AbstractConstraint subclassResponsibility")
  }

  method chooseMethod(mark) {
    // Decide if I can be satisfied and record that decision. The output of
    // the choosen method must not have the given mark and must have a
    // walkabout strength less than that of this constraint.
    error("AbstractConstraint subclassResponsibility")
  }

  method execute {
    // Enforce this constraint. Assume that it is satisfied.
    error("AbstractConstraint subclassResponsibility")
  }

  method inputsDo(aBlock) {
    // Assume that I am satisfied. Evaluate the given block on all my current
    // input variables.
    error("AbstractConstraint subclassResponsibility")
  }

  method inputsKnown(mark) {
    // Assume that I am satisfied. Answer true if all my current inputs are
    //  known. A variable is known if either a) it is 'stay' (i.e. it is a
    //  constant at plan execution time), b) it has the given mark (indicating
    //  that it has been computed by a constraint appearing earlier in the
    //  plan), or c) it is not determined by any constraint.
    !(inputsHasOne { v: Variable ->
       !((v.mark == mark).or { v.stay.or { v.determinedBy == Done } })
    })
  }

  method markUnsatisfied {
    // Record the fact that I am unsatisfied.
    error("AbstractConstraint subclassResponsibility")
  }

  method output {
    // Answer my current output variable. Raise an error if I am not
    // currently satisfied.
    error("AbstractConstraint subclassResponsibility")
  }

  method recalculate {
    // Calculate the walkabout strength, the stay flag, and, if it is 'stay',
    // the value for the current output of this constraint. Assume this
    // constraint is satisfied.
    error("AbstractConstraint subclassResponsibility")
  }

  method satisfy(mark) propagate(planner) {
    // Attempt to find a way to enforce this (still unsatisfied) constraint.
    // If successful, record the solution, perhaps modifying the current
    // dataflow graph.  Answer the constraint that this constraint overrides,
    // if there is one, or nil, if there isn't.
    chooseMethod(mark)

    var overridden: Constraint
    isSatisfied.ifTrue {
      // constraint can be satisfied
      // mark inputs to allow cycle detection in addPropagate
      inputsDo { in: Variable -> in.mark(mark) }
      def out: Variable = output
      overridden := out.determinedBy
      (overridden == Done).ifFalse { overridden.markUnsatisfied }
      out.determinedBy(self)
      planner.addPropagate(self) mark(mark).ifFalse {
        error("Cycle encountered adding:\tConstraint removed.")
        return Done
      }
      out.mark(mark)
    } ifFalse {
      // constraint cannot be satisfied
      overridden := Done
      strength.sameAs(Required).ifTrue {
        error("Failed to satisfy a required constraint")
      }
    }
    overridden
  }
}

class newBinaryConstraint(var1: Variable)v(var2: Variable)
                 strength(strengthSymbol: Sym)addTo(planner: Planner) {
  inherit newAbstractConstraint(strengthSymbol)

  //  I am an abstract superclass for constraints having two possible output
  //  variables.

  // Instance variables:
  //     v1, v2      possible output variables <Variable>
  //     direction       one of:
  //                     #forward (v2 is output)
  //                     #backward ( v1 is output)
  //                     nil (not satisfied)
  def v1: Variable = var1
  def v2: Variable = var2
  var direction: Direction := Done

  method isSatisfied -> Boolean {
    // Answer true if this constraint is satisfied in the current solution.
    direction != Done
  }

  method addToGraph -> Done {
    // Add myself to the constraint graph.
    v1.addConstraint(self)
    v2.addConstraint(self)
    direction := Done
  }

  method removeFromGraph -> Done {
    // Remove myself from the constraint graph.
    (v1 == Done).ifFalse { v1.removeConstraint(self) }
    (v2 == Done).ifFalse { v2.removeConstraint(self) }
    direction := Done
  }

  method chooseMethod(mark: Number) -> Direction {
    // Decide if I can be satisfied and which way I should flow based on
    // the relative strength of the variables I relate, and record that
    // decision.

    (v1.mark == mark).ifTrue {
      // forward or nothing
      ((v2.mark != mark).and { strength.stronger(v2.walkStrength) }).
        ifTrue {
          direction := forward
          return direction
        }
        ifFalse {
          direction := Done
          return direction
        }
    }

    (v2.mark == mark).ifTrue {
      // backward or nothing
      ((v1.mark != mark).and { strength.stronger(v1.walkStrength) }).
          ifTrue {
            direction := backward
            return direction
          }
          ifFalse {
            direction := Done
            return direction
          }
     }

    // if we get here, neither variable is marked, so we have choice
    v1.walkStrength.weaker(v2.walkStrength)
      ifTrue {
        strength.stronger(v1.walkStrength).
          ifTrue {
            direction := backward
            return direction
          }
          ifFalse {
            direction := Done
            return direction
          }
      }
      ifFalse {
        strength.stronger(v2.walkStrength).
          ifTrue {
            direction := forward
            return direction
          }
          ifFalse {
            direction := Done
            return direction
          }
      }
  }

  method execute {
    // Enforce this constraint. Assume that it is satisfied.
    error("BinaryConstraint subclassResponsibility")
  }

  method inputsDo(aBlock: Function) -> Done {
    // Evaluate the given block on my current input variable.
    (direction == forward).
      ifTrue  { aBlock.value(v1) }
      ifFalse { aBlock.value(v2) }
  }

  method inputsHasOne(aBlock: Function) -> Boolean {
    (direction == forward).
        ifTrue  { aBlock.value(v1) }
        ifFalse { aBlock.value(v2) }
  }

  method markUnsatisfied -> Done {
    // Record the fact that I am unsatisfied.
    direction := Done
  }

  method output -> Variable {
    // Answer my current output variable.
    (direction == forward).
      ifTrue  { return v2 }
      ifFalse { return v1 }
  }

  method recalculate -> Done {
    // Calculate the walkabout strength, the stay flag, and, if it is 'stay',
    // the value for the current output of this constraint. Assume this
    // constraint is satisfied.
    (direction == forward).
      ifTrue {
        in := v1
        out := v2
      }
      ifFalse {
        in := v2
        out := v1
      }
    out.walkStrength(strength.weakest(in.walkStrength))
    out.stay(in.stay)
    // stay optimization
    out.stay.ifTrue { execute }
  }
}

class newUnaryConstraint(aVariable: Variable)strength(strengthSymbol: Sym)addTo(planner: Planner) {
  inherit newAbstractConstraint(strengthSymbol)
  // I am an abstract superclass for constraints having a single possible
  // output variable.

  // Instance variables:
  //     output      possible output variable <Variable>
  //     satisfied       true if I am currently satisfied <Boolean>
  def output: Variable = aVariable
  var satisfied: Boolean := false
  addConstraint(planner)

  method isSatisfied -> Boolean {
    // Answer true if this constraint is satisfied in the current solution.
    satisfied
  }

  method addToGraph -> Done {
    // Add myself to the constraint graph.
    output.addConstraint(self)
    satisfied := false
  }

  method removeFromGraph -> Done {
    // Remove myself from the constraint graph.
    (output == Done).ifFalse { output.removeConstraint(self) }
    satisfied := false
  }

  method chooseMethod(mark: Number) -> Done {
    // Decide if I can be satisfied and record that decision.
    satisfied :=
      (output.mark != mark).and {
        strength.stronger(output.walkStrength) }
    Done
  }

  method execute -> Done {
    // Enforce this constraint. Assume that it is satisfied.
    error("UnaryConstraint subclassResponsibility")
  }

  method inputsDo(aBlock: Function) -> Done {
    // I have no input variables.
  }

  method inputsHasOne(aBlock: Function) -> Boolean {
    false
  }

  method markUnsatisfied -> Done {
    // Record the fact that I am unsatisfied.
    satisfied := false
  }

  method recalculate -> Done {
    // Calculate the walkabout strength, the stay flag, and, if it is 'stay',
    // the value for the current output of this constraint. Assume this
    // constraint is satisfied.
    output.walkStrength(strength)
    output.stay(!isInput)
    // stay optimization
    output.stay.ifTrue { execute }
  }
}

class newEditConstraint(aVariable: Variable)strength(strengthSymbol: Sym)addTo(planner: Planner) {
  inherit newUnaryConstraint(aVariable)strength(strengthSymbol)addTo(planner)

  // I am a unary input constraint used to mark a variable that the client
  // wishes to change.

  method isInput -> Boolean {
    // I indicate that a variable is to be changed by imperative code.
    true
  }

  method execute -> Done {
    // Edit constraints do nothing.
  }
}

class newEqualityConstraint(var1: Variable)v(var2: Variable)strength(strengthSymbol: Sym)addTo(planner: Planner) {
  inherit newBinaryConstraint(var1)v(var2)strength(strengthSymbol)addTo(planner)
  // I constrain two variables to have the same value: `v1 = v2`.
  addConstraint(planner)

  method execute -> Done {
    // Enforce this constraint. Assume that it is satisfied.
    (direction == forward).
      ifTrue  { v2.value(v1.value) }
      ifFalse { v1.value(v2.value) }
  }
}

class newScaleConstraint(srcVar: Variable)scale(scaleVar: Variable)
                  offset(offsetVar: Variable)dst(dstVar: Variable)
                  strength(strengthSymbol: Sym)addTo(planner: Planner) {
  inherit newBinaryConstraint(srcVar)v(dstVar)strength(strengthSymbol)addTo(planner)
  // I relate two variables by the linear scaling relationship:
  // `v2 = (v1 * scale) + offset`. Either v1 or v2 may be changed to maintain
  // this relationship but the scale factor and offset are considered read-only.

  // Instance variables:
  //    scale       scale factor input variable <Variable>
  //    offset      offset input variable <Variable>
  def scale: Variable = scaleVar
  def offset: Variable = offsetVar
  addConstraint(planner)

  method addToGraph -> Done {
    // Add myself to the constraint graph.
    v1.addConstraint(self)
    v2.addConstraint(self)
    scale.addConstraint(self)
    offset.addConstraint(self)
    direction := Done
  }

  method removeFromGraph -> Done {
    // Remove myself from the constraint graph.
    (v1 == Done).ifFalse { v1.removeConstraint(self) }
    (v2 == Done).ifFalse { v2.removeConstraint(self) }
    (scale == Done).ifFalse { scale.removeConstraint(self) }
    (offset == Done).ifFalse { offset.removeConstraint(self) }
    direction := Done
  }

  method execute -> Done {
    // Enforce this constraint. Assume that it is satisfied.
    (direction == forward).
      ifTrue  { v2.value((v1.value * scale.value) + offset.value) }
      ifFalse { v1.value((v2.value - offset.value) / scale.value) }
  }

  method inputsDo(aBlock: Function) -> Done {
    // Evaluate the given block on my current input variable.
    (direction == forward).
      ifTrue {  aBlock.value(v1)
                aBlock.value(scale)
                aBlock.value(offset)
      }
      ifFalse { aBlock.value(v2)
                aBlock.value(scale)
                aBlock.value(offset)
      }
  }

  method recalculate -> Done {
    // Calculate the walkabout strength, the stay flag, and, if it is 'stay',
    // the value for the current output of this constraint. Assume this
    // constraint is satisfied.
    (direction == forward).
      ifTrue {
        in := v1
        out := v2
      }
      ifFalse {
        out := v1
        in := v2
      }
    out.walkStrength(strength.weakest(in.walkStrength))
    out.stay(in.stay.and { scale.stay.and { offset.stay } })
    // stay optimization
    out.stay.ifTrue { execute }
  }
}

class newStayConstraint(aVariable: Variable)strength(strengthSymbol: Sym)addTo(planner: Planner) -> Constraint {
  inherit newUnaryConstraint(aVariable)strength(strengthSymbol)addTo(planner)
  // I mark variables that should, with some level of preference, stay the
  // same. I have one method with zero inputs and one output, which does
  // nothing. Planners may exploit the fact that, if I am satisfied, my output
  // will not change during plan execution.
  // This is called 'stay optimization.'

  method execute -> Done {
    // Stay constraints do nothing.
  }
}

class newVariable -> Variable {
  // I represent a constrained variable. In addition to my value, I maintain the
  // structure of the constraint graph, the current dataflow graph, and various
  // parameters of interest to the DeltaBlue incremental constraint solver.

  // Instance variables:
  //     value           my value; changed by constraints, read by client <Object>
  //     constraints     normal constraints that reference me <Array of Constraint>
  //     determinedBy    the constraint that currently determines
  //                     my value (or nil if there isn't one) <Constraint>
  //     walkStrength        my walkabout strength <Strength>
  //     stay            true if I am a planning-time constant <Boolean>
  //     mark            used by the planner to mark constraints <Number> *)
  var value: Number        := 0.asInteger
  def constraints: Vector  = core.newVector(2)
  var determinedBy: Constraint := Done
  var walkStrength: Strength   := AbsoluteWeakest
  var stay: Boolean            := true
  var mark: Number             := 0.asInteger


  method addConstraint(aConstraint: Constraint) -> Done {
    // Add the given constraint to the set of all constraints that refer to me.
    constraints.append(aConstraint)
  }

  method removeConstraint(c: Constraint) -> Done {
    // Remove all traces of c from this variable
    constraints.remove(c)
    (determinedBy == c).ifTrue { determinedBy := Done }
  }
}

method newVariableValue(aValue: Number) {
  def o: Variable = newVariable
  o.value(aValue)
  o
}

method newInstance -> Benchmark { newDeltaBlue }
