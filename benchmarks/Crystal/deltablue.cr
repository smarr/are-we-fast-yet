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
require "./benchmark"
require "./som"


class DeltaBlue < Benchmark
  def inner_benchmark_loop(inner_iterations)
    Planner.chain_test(inner_iterations)
    Planner.projection_test(inner_iterations)
    true
  end
end

class Plan < Vector(AbstractConstraint?)
  def initialize
    super(15)
  end

  def execute
    each { | c | c.not_nil!.execute }
  end
end

class Planner
  def initialize
    @current_mark = 1
  end

  def incremental_add(constraint : AbstractConstraint)
    mark = new_mark
    overridden = constraint.satisfy(mark, self)

    until overridden == nil
      overridden = overridden.not_nil!.satisfy(mark, self)
    end
  end

  def incremental_remove(constraint : AbstractConstraint)
    out_v = constraint.output
    if out_v.is_a?(Variable)
      constraint.mark_unsatisfied
      constraint.remove_from_graph
      unsatisfied = remove_propagate_from(out_v)
      unsatisfied.each { |u| incremental_add(u.not_nil!) }
    else
      raise "constraint.output is nil"
    end
  end

  def extract_plan_from_constraints(constraints)
    sources = Vector(AbstractConstraint?).new

    constraints.each { | c_ |
      c = c_.not_nil!
      if c.is_input && c.is_satisfied
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
      
      if c.is_a?(Nil)
        raise "remove_first was Nil even though not empty?"
      end
    
      c_output = c.output
      
      if c_output.is_a?(Nil)
        raise "c.output is nil"
      end

      if c_output.mark != mark && c.inputs_known(mark)
        plan.append(c)
        c_output.mark = mark
        add_constraints_consuming_to(c_output, todo)
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
    if v.is_a?(Nil)
      raise "v is nil"
    end
    
    determining_c = v.determined_by

    v.constraints.each { | c_ |
      c = c_.not_nil!
      if (!c == determining_c) && c.is_satisfied # Ruby uses .equal?
        coll.append(c)
      end
    }
  end

  def add_propagate(c, mark)
    todo = Vector(AbstractConstraint?).with(c)

    until todo.empty?
      d = todo.remove_first
      if d.is_a?(Nil)
        raise "remove_first returned Nil but was not empty?"
      end
      d_output = d.output
      if d_output.is_a?(Nil)
        raise "output was nil"
      end
      if d_output.mark == mark
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
    plan = extract_plan_from_constraints(Vector(AbstractConstraint?).with(edit_constraint))
    10.times {
      var.value = val
      plan.execute
    }
    edit_constraint.destroy_constraint(self)
  end

  def constraints_consuming(v : Variable) # &block
    determining_c = v.determined_by
    v.constraints.each { | c |
      if c != determining_c && c.not_nil!.is_satisfied
        yield c
      end
    }
  end

  def new_mark
    @current_mark += 1
  end

  def remove_propagate_from(out_v : Variable)
    unsatisfied = Vector(AbstractConstraint?).new

    out_v.determined_by = nil
    out_v.walk_strength = ABSOLUTE_WEAKEST
    out_v.stay = true

    todo = Vector(Variable?).with(out_v)

    until todo.empty?
      v = todo.remove_first.not_nil!
      v.not_nil!.constraints.each { | c |
        unless c.not_nil!.is_satisfied
          unsatisfied.append(c)
        end
      }

      constraints_consuming(v) { | c_ |
        c = c_.not_nil!
        c.recalculate
        c_output = c.output
        if c_output.is_a?(Nil)
          raise "c.output is nil"
        end
        todo.append(c_output)
      }
    end

    unsatisfied.sort { |c1, c2| c1.not_nil!.strength.stronger(c2.not_nil!.strength) }
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
    vars = Array(Variable?).new(n + 1) { |i| Variable.new }

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
      first = vars.first
      
      if first.is_a?(Nil)
        raise "first was nil"
      end
      
      first.value = v
      plan.execute
    
      last = vars.last
      
      if last.is_a?(Nil)
        raise "last was nil"
      end

      if last.value != v
        raise "Chain test failed!!"
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
    dests   = Vector(Variable?).new
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
    
    if src.is_a?(Nil)
      raise "src is Nil"
    end
    
    if dst.is_a?(Nil)
      raise "dst is Nil"
    end

    planner.change_var(src, 17)
    if dst.value != 1170; raise "Projection 1 failed" end

    planner.change_var(dst, 1050)
    if src.value != 5; raise "Projection 2 failed" end

    planner.change_var(scale, 5)
    (0..(n - 2)).each { | i |
      if dests.at(i).not_nil!.value != ((i + 1) * 5 + 1000)
        raise "Projection 3 failed"
      end
    }

    planner.change_var(offset, 2000)
    (0..(n - 2)).each { | i |
      if dests.at(i).not_nil!.value != ((i + 1) * 5 + 2000)
        raise "Projection 4 failed"
      end
    }
  end
end


class Strength
  property :arithmetic_value

  def initialize(strength_sym : Symbol)
    @symbolic_value   = strength_sym
    @arithmetic_value = STRENGHT_TABLE.at(strength_sym).not_nil!
  end

  def same_as(strength : Strength)
    @arithmetic_value == strength.arithmetic_value
  end

  def stronger(strength : Strength)
    @arithmetic_value < strength.arithmetic_value
  end

  def weaker(strength : Strength)
    @arithmetic_value > strength.arithmetic_value
  end

  def strongest(strength : Strength)
    if strength.stronger(self)
      strength
    else
      self
    end
  end

  def weakest(strength : Strength)
    if strength.weaker(self)
      strength
    else
      self
    end
  end

  def self.create_strength_table
    table = Dictionary(Symbol?, Int32?).new
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
    constants = Dictionary(Symbol?, Strength?).new
    STRENGHT_TABLE.keys.each { | strength_sym_ |
      strength_sym = strength_sym_.not_nil!
      constants.at_put(strength_sym, self.new(strength_sym))
    }
    constants
  end

  STRENGHT_TABLE     = create_strength_table
  STRENGHT_CONSTANTS = create_strength_constants

  def self.of(sym)
    STRENGHT_CONSTANTS.at(sym).not_nil!
  end
end


class AbstractConstraint
  property :strength

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
    if is_satisfied
      planner.incremental_remove(self)
    end
    remove_from_graph
  end

  def inputs_known(mark)
    !inputs_has_one { | v | !(v.mark == mark || v.stay || v.determined_by.nil?) }
  end

  def satisfy(mark, planner)
    choose_method(mark)

    if is_satisfied
      inputs_do { | i | i.mark = mark }

      outx = output
      
      if outx.is_a?(Nil)
        raise "outx is nil"
      end
      
      overridden = outx.determined_by

      unless overridden.is_a?(Nil)
        overridden.mark_unsatisfied
      end

      outx.determined_by = self

      unless planner.add_propagate(self, mark)
        raise "Cycle encountered adding: Constraint removed"
      end

      outx.mark = mark
      overridden
    else
      if @strength.same_as(REQUIRED)
        raise "Failed to satisfy a required constraint"
      end
      nil
    end
  end

  def choose_method(mark)
    raise "abstract choose_method"
  end
  
  def is_satisfied
    raise "abstract is_satisfied"
  end

  def recalculate
    raise "abstract recalculate"
  end

  def execute
    raise "abstract execute"
  end

  def output
    raise "abstract output"
  end

  def inputs_do(&block)
    raise "abstract inputs_do"
  end

  def mark_unsatisfied
    raise "abstract mark_unsatisfied"
  end

  def remove_from_graph
    raise "abstract remove_from_graph"
  end

  def inputs_has_one(&block)
    raise "abstract inputs_has_one"
  end
end

class BinaryConstraint < AbstractConstraint
  def initialize(v1, v2, strength, planner)
    super(strength)
    if v1.is_a?(Nil)
      raise "v1 is nil"
    end
    @v1 = v1
    if v2.is_a?(Nil)
      raise "v2 is nil"
    end
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

  def inputs_do # &block
    if @direction == :forward
      yield @v1
    else
      yield @v2
    end
  end

  def inputs_has_one # &block
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
      outx = @v2
    else
      ihn = @v2
      outx = @v1
    end

    outx.walk_strength = @strength.weakest(ihn.walk_strength)
    outx.stay = ihn.stay

    if outx.stay
      execute
    end
  end
end

class UnaryConstraint < AbstractConstraint
  property :output

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
    output = @output
    
    if output.is_a?(Nil)
      raise "output is nil"
    end
    
    output.add_constraint(self)
    @satisfied = false
  end

  def remove_from_graph
    output = @output
    unless output.is_a?(Nil)
      output.remove_constraint(self)
    end
    @satisfied = false
  end

  def choose_method(mark)
    output = @output
    
    if output.is_a?(Nil)
      raise "output is nil"
    end
    
    @satisfied = output.mark != mark && @strength.stronger(output.walk_strength)
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
    output = @output
    
    if output.is_a?(Nil)
      raise "output is nil"
    end
    
    output.walk_strength = @strength
    output.stay          = !is_input

    if output.stay
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
      outx = @v2
    else
      outx = @v2
      ihn = @v1
    end
    outx.walk_strength = @strength.weakest(ihn.walk_strength)
    outx.stay = (ihn.stay && @scale.stay && @offset.stay)

    if outx.stay
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
  property :value
  property :constraints
  property :determined_by
  property :walk_strength
  property :stay
  property :mark

  def initialize
    @value         = 0
    @constraints   = Vector(AbstractConstraint?).new(2)
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
