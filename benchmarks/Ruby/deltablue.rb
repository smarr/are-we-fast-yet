# frozen_string_literal: true

# The benchmark in its current state is a derivation from the SOM version,
# which is derived from Mario Wolczko's Smalltalk version of DeltaBlue.
#
# The original license details are availble here:
# http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html

# This file itself, and its souce control history is however based on the
# following. It is unclear whether this still bears any relevance since the
# nature of the code was essentially reverted back to the Smalltalk version.
#
# Port of deltablue.py, as documented below, to Ruby.
# Stefan Marr, 2014-04-28
#
# Was: deltablue.py
# =================
#
# Ported for the PyPy project.
# Contributed by Daniel Lindsley
#
# This implementation of the DeltaBlue benchmark was directly ported
# from the `V8's source code`_, which was in turn derived
# from the Smalltalk implementation by John Maloney and Mario
# Wolczko. The original Javascript implementation was licensed under the GPL.
#
# It's been updated in places to be more idiomatic to Python (for loops over
# collections, a couple magic methods, OrderedCollection being a list & things
# altering those collections changed to the builtin methods) but largely retains
# the layout & logic from the original. (Ugh.)
#
# .. _`V8's source code`: (http://code.google.com/p/v8/source/browse/branches/bleeding_edge/benchmarks/deltablue.js)

require_relative 'benchmark'
require_relative 'som'

class DeltaBlue < Benchmark
  def inner_benchmark_loop(inner_iterations)
    Planner.chain_test(inner_iterations)
    Planner.projection_test(inner_iterations)
    true
  end
end

class Plan < Vector
  def initialize
    super(15)
  end

  def execute
    each { |c| c.execute }
  end
end

class Planner
  def initialize
    @current_mark = 1
  end

  def incremental_add(constraint)
    mark = new_mark
    overridden = constraint.satisfy(mark, self)

    overridden = overridden.satisfy(mark, self) while overridden
  end

  def incremental_remove(constraint)
    out_v = constraint.output
    constraint.mark_unsatisfied
    constraint.remove_from_graph
    unsatisfied = remove_propagate_from(out_v)
    unsatisfied.each { |u| incremental_add(u) }
  end

  def extract_plan_from_constraints(constraints)
    sources = Vector.new

    constraints.each do |c|
      sources.append(c) if c.is_input && c.is_satisfied
    end

    make_plan(sources)
  end

  def make_plan(sources)
    mark = new_mark
    plan = Plan.new
    todo = sources

    until todo.empty?
      c = todo.remove_first

      if c.output.mark != mark && c.inputs_known(mark)
        plan.append(c)
        c.output.mark = mark
        add_constraints_consuming_to(c.output, todo)
      end
    end

    plan
  end

  def propagate_from(v)
    todo = Vector.new
    add_constraints_consuming_to(v, todo)

    until todo.empty?
      c = todo.remove_first
      c.execute
      add_constraints_consuming_to(c.output, todo)
    end
  end

  def add_constraints_consuming_to(v, coll)
    determining_c = v.determined_by

    v.constraints.each do |c|
      coll.append(c) if (!c.equal? determining_c) && c.is_satisfied
    end
  end

  def add_propagate(c, mark)
    todo = Vector.with(c)

    until todo.empty?
      d = todo.remove_first
      if d.output.mark == mark
        incremental_remove(c)
        return false
      end
      d.recalculate
      add_constraints_consuming_to(d.output, todo)
    end
    true
  end

  def change_var(var, val)
    edit_constraint = EditConstraint.new(var, SYM_PREFERRED, self)
    plan = extract_plan_from_constraints(Vector.with(edit_constraint))
    10.times do
      var.value = val
      plan.execute
    end
    edit_constraint.destroy_constraint(self)
  end

  def constraints_consuming(v)
    determining_c = v.determined_by
    v.constraints.each do |c|
      yield c if c != determining_c && c.is_satisfied
    end
  end

  def new_mark
    @current_mark += 1
  end

  def remove_propagate_from(out_v)
    unsatisfied = Vector.new

    out_v.determined_by = nil
    out_v.walk_strength = ABSOLUTE_WEAKEST
    out_v.stay = true

    todo = Vector.with(out_v)

    until todo.empty?
      v = todo.remove_first

      v.constraints.each do |c|
        unsatisfied.append(c) unless c.is_satisfied
      end

      constraints_consuming(v) do |c|
        c.recalculate
        todo.append(c.output)
      end
    end

    unsatisfied.sort { |c1, c2| c1.strength.stronger(c2.strength) }
    unsatisfied
  end

  def self.chain_test(n)
    # This is the standard DeltaBlue benchmark. A long chain of equality
    # constraints is constructed with a stay constraint on one end. An
    # edit constraint is then added to the opposite end and the time is
    # measured for adding and removing this constraint, and extracting
    # and executing a constraint satisfaction plan. There are two cases.
    # In case 1, the added constraint is stronger than the stay
    # constraint and values must propagate down the entire length of the
    # chain. In case 2, the added constraint is weaker than the stay
    # constraint so it cannot be accomodated. The cost in this case is,
    # of course, very low. Typical situations lie somewhere between these
    # two extremes.

    planner = Planner.new
    vars = Array.new(n + 1) { Variable.new }

    # thread a chain of equality constraints through the variables
    (0..(n - 1)).each do |i|
      v1 = vars[i]
      v2 = vars[i + 1]

      EqualityConstraint.new(v1, v2, SYM_REQUIRED, planner)
    end

    StayConstraint.new(vars.last, SYM_STRONG_DEFAULT, planner)
    edit = EditConstraint.new(vars.first, SYM_PREFERRED, planner)
    plan = planner.extract_plan_from_constraints([edit])

    (1..100).each do |v|
      vars.first.value = v
      plan.execute

      raise 'Chain test failed!!' if vars.last.value != v
    end

    edit.destroy_constraint(planner)
  end

  def self.projection_test(n)
    # This test constructs a two sets of variables related to each
    # other by a simple linear transformation (scale and offset). The
    # time is measured to change a variable on either side of the
    # mapping and to change the scale and offset factors.

    planner = Planner.new
    dests   = Vector.new
    scale   = Variable.value(10)
    offset  = Variable.value(1000)

    src = nil
    dst = nil

    (1..n).each do |i|
      src = Variable.value(i)
      dst = Variable.value(i)
      dests.append(dst)
      StayConstraint.new(src, SYM_DEFAULT, planner)
      ScaleConstraint.new(src, scale, offset, dst, SYM_REQUIRED, planner)
    end

    planner.change_var(src, 17)
    raise 'Projection 1 failed' if dst.value != 1170

    planner.change_var(dst, 1050)
    raise 'Projection 2 failed' if src.value != 5

    planner.change_var(scale, 5)
    (0..(n - 2)).each do |i|
      raise 'Projection 3 failed' if dests.at(i).value != ((i + 1) * 5 + 1000)
    end

    planner.change_var(offset, 2000)
    (0..(n - 2)).each do |i|
      raise 'Projection 4 failed' if dests.at(i).value != ((i + 1) * 5 + 2000)
    end
  end
end

class Sym
  attr_reader :custom_hash

  def initialize(hash)
    @custom_hash = hash
  end
end

SYM_ABSOLUTE_STRONGEST = Sym.new(0)
SYM_REQUIRED           = Sym.new(1)
SYM_STRONG_PREFERRED   = Sym.new(2)
SYM_PREFERRED          = Sym.new(3)
SYM_STRONG_DEFAULT     = Sym.new(4)
SYM_DEFAULT            = Sym.new(5)
SYM_WEAK_DEFAULT       = Sym.new(6)
SYM_ABSOLUTE_WEAKEST   = Sym.new(7)

class Strength
  attr_reader :arithmetic_value

  def initialize(strength_sym)
    @symbolic_value   = strength_sym
    @arithmetic_value = STRENGTH_TABLE.at(strength_sym)
  end

  def same_as(strength)
    @arithmetic_value == strength.arithmetic_value
  end

  def stronger(strength)
    @arithmetic_value < strength.arithmetic_value
  end

  def weaker(strength)
    @arithmetic_value > strength.arithmetic_value
  end

  def strongest(strength)
    if strength.stronger(self)
      strength
    else
      self
    end
  end

  def weakest(strength)
    if strength.weaker(self)
      strength
    else
      self
    end
  end

  def self.create_strength_table
    table = IdentityDictionary.new
    table.at_put(SYM_ABSOLUTE_STRONGEST, -10_000)
    table.at_put(SYM_REQUIRED,              -800)
    table.at_put(SYM_STRONG_PREFERRED,      -600)
    table.at_put(SYM_PREFERRED,             -400)
    table.at_put(SYM_STRONG_DEFAULT,        -200)
    table.at_put(SYM_DEFAULT,                  0)
    table.at_put(SYM_WEAK_DEFAULT,           500)
    table.at_put(SYM_ABSOLUTE_WEAKEST,    10_000)
    table
  end

  def self.create_strength_constants
    constants = IdentityDictionary.new
    STRENGTH_TABLE.keys.each do |strength_sym|
      constants.at_put(strength_sym, new(strength_sym))
    end
    constants
  end

  STRENGTH_TABLE     = create_strength_table
  STRENGTH_CONSTANTS = create_strength_constants

  def self.of(sym)
    STRENGTH_CONSTANTS.at(sym)
  end
end

class AbstractConstraint
  attr_reader :strength

  def initialize(strength_sym)
    @strength = Strength.of(strength_sym)
  end

  def is_input
    false
  end

  def add_constraint(planner)
    add_to_graph
    planner.incremental_add(self)
  end

  def destroy_constraint(planner)
    planner.incremental_remove(self) if is_satisfied
    remove_from_graph
  end

  def inputs_known(mark)
    !inputs_has_one { |v| !(v.mark == mark || v.stay || !v.determined_by) }
  end

  def satisfy(mark, planner)
    choose_method(mark)

    if is_satisfied
      inputs_do { |i| i.mark = mark }

      outx = output
      overridden = outx.determined_by

      overridden.mark_unsatisfied if overridden

      outx.determined_by = self

      unless planner.add_propagate(self, mark)
        raise 'Cycle encountered adding: Constraint removed'
      end

      outx.mark = mark
      overridden
    else
      if @strength.same_as(REQUIRED)
        raise 'Failed to satisfy a required constraint'
      end
      nil
    end
  end
end

class BinaryConstraint < AbstractConstraint
  def initialize(v1, v2, strength, _planner)
    super(strength)
    @v1 = v1
    @v2 = v2
    @direction = nil
  end

  def is_satisfied
    @direction != nil
  end

  def add_to_graph
    @v1.add_constraint(self)
    @v2.add_constraint(self)
    @direction = nil
  end

  def remove_from_graph
    @v1.remove_constraint(self) if @v1
    @v2.remove_constraint(self) if @v2

    @direction = nil
  end

  def choose_method(mark)
    if @v1.mark == mark
      if @v2.mark != mark && @strength.stronger(@v2.walk_strength)
        return @direction = :forward
      else
        return @direction = nil
      end
    end

    if @v2.mark == mark
      if @v1.mark != mark && @strength.stronger(@v1.walk_strength)
        return @direction = :backward
      else
        return @direction = nil
      end
    end

    if @v1.walk_strength.weaker(@v2.walk_strength)
      if @strength.stronger(@v1.walk_strength)
        @direction = :backward
      else
        @direction = nil
      end
    else
      if @strength.stronger(@v2.walk_strength)
        @direction = :forward
      else
        @direction = nil
      end
    end
  end

  def inputs_do
    if @direction == :forward
      yield @v1
    else
      yield @v2
    end
  end

  def inputs_has_one
    if @direction == :forward
      yield @v1
    else
      yield @v2
    end
  end

  def mark_unsatisfied
    @direction = nil
  end

  def output
    if @direction == :forward
      @v2
    else
      @v1
    end
  end

  def recalculate
    if @direction == :forward
      ihn  = @v1
      outx = @v2
    else
      ihn  = @v2
      outx = @v1
    end

    outx.walk_strength = @strength.weakest(ihn.walk_strength)
    outx.stay = ihn.stay

    execute if outx.stay
  end
end

class UnaryConstraint < AbstractConstraint
  attr_reader :output

  def initialize(v, strength, planner)
    super(strength)
    @output    = v
    @satisfied = false
    add_constraint(planner)
  end

  def is_satisfied
    @satisfied
  end

  def add_to_graph
    @output.add_constraint(self)
    @satisfied = false
  end

  def remove_from_graph
    @output.remove_constraint(self) if @output
    @satisfied = false
  end

  def choose_method(mark)
    @satisfied = @output.mark != mark &&
                 @strength.stronger(@output.walk_strength)
  end

  def inputs_do
    # No-op. I have no input variable.
  end

  def inputs_has_one
    false
  end

  def mark_unsatisfied
    @satisfied = false
  end

  def recalculate
    @output.walk_strength = @strength
    @output.stay          = !is_input

    execute if @output.stay
  end
end

class EditConstraint < UnaryConstraint
  def is_input
    true
  end

  def execute
    # Edit constraints does nothing.
  end
end

class EqualityConstraint < BinaryConstraint
  def initialize(var1, var2, strength, planner)
    super(var1, var2, strength, planner)
    add_constraint(planner)
  end

  def execute
    if @direction == :forward
      @v2.value = @v1.value
    else
      @v1.value = @v2.value
    end
  end
end

class ScaleConstraint < BinaryConstraint
  def initialize(src, scale, offset, dest, strength, planner)
    super(src, dest, strength, planner)
    @scale  = scale
    @offset = offset
    add_constraint(planner)
  end

  def add_to_graph
    @v1.add_constraint(self)
    @v2.add_constraint(self)
    @scale.add_constraint(self)
    @offset.add_constraint(self)
    @direction = nil
  end

  def remove_from_graph
    @v1.remove_constraint(self) if @v1
    @v2.remove_constraint(self) if @v2

    @scale.remove_constraint(self)  if @scale
    @offset.remove_constraint(self) if @offset

    @direction = nil
  end

  def execute
    if @direction == :forward
      @v2.value = @v1.value * @scale.value + @offset.value
    else
      @v1.value = (@v2.value - @offset.value) / @scale.value
    end
  end

  def inputs_do
    if @direction == :forward
      yield @v1
      yield @scale
      yield @offset
    else
      yield @v2
      yield @scale
      yield @offset
    end
  end

  def recalculate
    if @direction == :forward
      ihn  = @v1
      outx = @v2
    else
      outx = @v2
      ihn  = @v1
    end
    outx.walk_strength = @strength.weakest(ihn.walk_strength)
    outx.stay = (ihn.stay && @scale.stay && @offset.stay)

    execute if outx.stay
  end
end

class StayConstraint < UnaryConstraint
  def execute
    # Stay Constraints do nothing
  end
end

class Variable
  attr_accessor :value, :constraints, :determined_by, :walk_strength, :stay, :mark

  def initialize
    @value         = 0
    @constraints   = Vector.new(2)
    @determined_by = nil
    @walk_strength = ABSOLUTE_WEAKEST
    @stay          = true
    @mark          = 0
  end

  def add_constraint(constraint)
    @constraints.append(constraint)
  end

  def remove_constraint(constraint)
    @constraints.remove(constraint)

    @determined_by = nil if @determined_by == constraint
  end

  def self.value(initial_value)
    o = new
    o.value = initial_value
    o
  end
end

ABSOLUTE_STRONGEST = Strength.of(SYM_ABSOLUTE_STRONGEST)
ABSOLUTE_WEAKEST   = Strength.of(SYM_ABSOLUTE_WEAKEST)
REQUIRED           = Strength.of(SYM_REQUIRED)
