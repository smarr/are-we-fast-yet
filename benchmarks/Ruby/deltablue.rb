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
# collections, a couple magic methods, ``OrderedCollection`` being a list & things
# altering those collections changed to the builtin methods) but largely retains
# the layout & logic from the original. (Ugh.)
# 
# .. _`V8's source code`: (http://code.google.com/p/v8/source/browse/branches/bleeding_edge/benchmarks/deltablue.js)
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
    each { | c | c.execute }
  end
end

class Planner
  def initialize
    @current_mark = 1
  end

  def incremental_add(constraint)
    mark = new_mark
    overridden = constraint.satisfy(mark, self)

    until overridden.nil?
      overridden = overridden.satisfy(mark, self)
    end
  end

  def incremental_remove(constraint)
    out = constraint.output
    constraint.mark_unsatisfied
    constraint.remove_from_graph
    unsatisfied = remove_propagate_from(out)
    unsatisfied.each { |u| incremental_add(u) }
  end

  def extract_plan_from_constraints(constraints)
    sources = Vector.new

    constraints.each { | c |
      if c.is_input and c.is_satisfied
        sources.append(c)
      end
    }

    make_plan(sources)
  end

  def make_plan(sources)
    mark = new_mark
    plan = Plan.new
    todo = sources

    until todo.empty?
      c = todo.remove_first

      if c.output.mark != mark and c.inputs_known(mark)
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

    v.constraints.each { | c |
      if c != determining_c and c.is_satisfied
        coll.append(c)
      end
    }
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
    edit_constraint = EditConstraint.new(var, :preferred, self)
    plan = extract_plan_from_constraints([edit_constraint])
    10.times {
      var.value = val
      plan.execute
    }
    edit_constraint.destroy_constraint(self)
  end

  def constraints_consuming(v) # &block
    determining_c = v.determined_by
    v.constraints.each { | c |
      if c != determining_c and c.is_satisfied
        yield c
      end
    }
  end

  def new_mark
    @current_mark += 1
  end

  def remove_propagate_from(out)
    unsatisfied = Vector.new

    out.determined_by = nil
    out.walk_strength = ABSOLUTE_WEAKEST
    out.stay = true

    todo = Vector.with(out)

    until todo.empty?
      v = todo.remove_first

      v.constraints.each { | c |
        unless c.is_satisfied
          unsatisfied.append(c)
        end
      }

      constraints_consuming(v) { | c |
        c.recalculate
        todo.append(c.output)
      }
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
    (0..(n - 1)).each { | i |
      v1 = vars[i]
      v2 = vars[i + 1]

      EqualityConstraint.new(v1, v2, :required, planner)
    }

    StayConstraint.new(vars.last, :strong_default, planner)
    edit = EditConstraint.new(vars.first, :preferred, planner)
    plan = planner.extract_plan_from_constraints([edit])

    (1..100).each { | v |
      vars.first.value = v
      plan.execute

      if vars.last.value != v
        raise 'Chain test failed!!'
      end
    }

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

    (1..n).each { | i |
      src = Variable.value(i)
      dst = Variable.value(i)
      dests.append(dst)
      StayConstraint.new(src, :default, planner)
      ScaleConstraint.new(src, scale, offset, dst, :required, planner)
    }

    planner.change_var(src, 17)
    if dst.value != 1170; raise 'Projection 1 failed' end

    planner.change_var(dst, 1050)
    if src.value != 5; raise 'Projection 2 failed' end

    planner.change_var(scale, 5)
    (0..(n - 2)).each { | i |
      if dests.at(i).value != ((i + 1) * 5 + 1000)
        raise 'Projection 3 failed'
      end
    }

    planner.change_var(offset, 2000)
    (0..(n - 2)).each { | i |
      if dests.at(i).value != ((i + 1) * 5 + 2000)
        raise 'Projection 4 failed'
      end
    }
  end
end


class Strength
  attr_reader :arithmetic_value

  def initialize(strength_sym)
    @symbolic_value   = strength_sym
    @arithmetic_value = STRENGHT_TABLE.at(strength_sym)
  end

  def same_as(strength)
    @arithmetic_value < strength.arithmetic_value
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
    table = Dictionary.new
    table.at_put(:absolute_strongest, -10000)
    table.at_put(:required,             -800)
    table.at_put(:strong_preferred,     -600)
    table.at_put(:preferred,            -400)
    table.at_put(:strong_default,       -200)
    table.at_put(:default,                 0)
    table.at_put(:weak_default,          500)
    table.at_put(:absolute_weakest,    10000)
    table
  end

  def self.create_strength_constants
    constants = Dictionary.new
    STRENGHT_TABLE.keys.each { | strength_sym |
      constants.at_put(strength_sym, self.new(strength_sym))
    }
    constants
  end

  STRENGHT_TABLE     = create_strength_table
  STRENGHT_CONSTANTS = create_strength_constants

  def self.of(sym)
    STRENGHT_CONSTANTS.at(sym)
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

  def is_satisfied
    raise :subclass_responsibility
  end

  def add_constraint(planner)
    add_to_graph
    planner.incremental_add(self)
  end

  def add_to_graph
    raise :subclass_responsibility
  end

  def destroy_constraint(planner)
    if is_satisfied
      planner.incremental_remove(self)
    end
    remove_from_graph
  end

  def remove_from_graph
    raise :subclass_responsibility
  end

  def choose_method(mark)
    raise :subclass_responsibility
  end

  def execute
    raise :subclass_responsibility
  end

  def inputs_do
    raise :subclass_responsibility
  end

  def inputs_known(mark)
    inputs_do { | v |
      unless v.mark == mark or v.stay or v.determined_by nil?
        return false
      end
    }
    true
  end

  def mark_unsatisfied
    raise :subclass_responsibility
  end

  def output
    raise :subclass_responsibility
  end

  def recalculate
    raise :subclass_responsibility
  end

  def satisfy(mark, planner)
    choose_method(mark)

    if is_satisfied
      inputs_do { | i | i.mark = mark }

      out = output
      overridden = out.determined_by

      unless overridden.nil?
        overridden.mark_unsatisfied
      end

      out.determined_by = self

      unless planner.add_propagate(self, mark)
        raise 'Cycle encountered adding: Constraint removed'
      end

      out.mark = mark
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
  def initialize(v1, v2, strength, planner)
    super(strength)
    @v1 = v1
    @v2 = v2
    @direction = nil
  end

  def is_satisfied
    !@direction.nil?
  end

  def add_to_graph
    @v1.add_constraint(self)
    @v2.add_constraint(self)
    @direction = nil
  end

  def remove_from_graph
    unless @v1.nil?
      @v1.remove_constraint(self)
    end

    unless @v2.nil?
      @v2.remove_constraint(self)
    end

    @direction = nil
  end

  def choose_method(mark)
    if @v1.mark == mark
      if @v2.mark != mark and @strength.stronger(@v2.walk_strength)
        return @direction = :forward
      else
        return @direction = nil
      end
    end

    if @v2.mark == mark
      if @v1.mark != mark and @strength.stronger(@v1.walk_strength)
        return @direction = :backward
      else
        return @direction = nil
      end
    end

    if @v1.walk_strength.weaker(@v2.walk_strength)
      if @strength.stronger(@v1.walk_strength)
        return @direction = :backward
      else
        return @direction = nil
      end
    else
      if @strength.stronger(@v2.walk_strength)
        return @direction = :forward
      else
        return @direction = nil
      end
    end
  end

  def execute
    raise :subclass_responsibility
  end

  def inputs_do # &block
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
      ihn = @v1
      out = @v2
    else
      ihn = @v2
      out = @v1
    end

    out.walk_strength = @strength.weakest(ihn.walk_strength)
    out.stay = ihn.stay

    if out.stay
      execute
    end
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
    unless @output.nil?
      @output.remove_constraint(self)
    end
    @satisfied = false
  end

  def choose_method(mark)
    @satisfied = @output.mark != mark and @strength.stronger(@output.walk_strength)
  end

  def execute
    raise :subclass_responsibility
  end

  def inputs_do
    # No-op. I have no input variable.
  end

  def mark_unsatisfied
    @satisfied = false
  end

  def recalculate
    @output.walk_strength = @strength
    @output.stay          = !is_input

    if @output.stay
      execute
    end
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
    unless @v1.nil?
      @v1.remove_constraint(self)
    end

    unless @v2.nil?
      @v2.remove_constraint(self)
    end

    unless @scale.nil?
      @scale.remove_constraint(self)
    end

    unless @offset.nil?
      @offset.remove_constraint(self)
    end

    @direction = nil
  end

  def execute
    if @direction == :forward
      @v2.value = @v1.value * @scale.value + @offset.value
    else
      @v1.value = (@v2.value - @offset.value) / @scale.value
    end
  end

  def inputs_do # &block
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
      ihn = @v1
      out = @v2
    else
      out = @v2
      ihn = @v1
    end
    out.walk_strength = @strength.weakest(ihn.walk_strength)
    out.stay = (ihn.stay and @scale.stay and @offset.stay)

    if out.stay
      execute
    end
  end
end

class StayConstraint < UnaryConstraint
  def execute
    # Stay Constraints do nothing
  end
end

class Variable
  attr_accessor :value
  attr_accessor :constraints
  attr_accessor :determined_by
  attr_accessor :walk_strength
  attr_accessor :stay
  attr_accessor :mark

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

    if @determined_by == constraint
      @determined_by = nil
    end
  end

  def self.value(initial_value)
    o = self.new
    o.value = initial_value
    o
  end
end

ABSOLUTE_STRONGEST = Strength.of(:absolute_strongest)
ABSOLUTE_WEAKEST   = Strength.of(:absolute_weakest)
REQUIRED           = Strength.of(:required)
