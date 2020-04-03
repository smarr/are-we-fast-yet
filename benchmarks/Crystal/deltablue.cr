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

abstract class AbstractConstraint
  getter :strength

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
    !inputs_has_one { | v | !(v.mark == mark || v.stay || !v.determined_by) }
  end

  def satisfy(mark, planner)
    choose_method(mark)

    if is_satisfied
      inputs_do { | i | i.mark = mark }

      outx = output
      overridden = outx.determined_by

      if overridden
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

  abstract def choose_method(mark)
  abstract def execute
  abstract def inputs_do(&block : Variable -> Void)
  abstract def inputs_has_one(&block : Variable -> Bool)
  abstract def is_satisfied
  abstract def mark_unsatisfied
  abstract def output
  abstract def recalculate
  abstract def remove_from_graph
end

class Plan < Vector(AbstractConstraint?)
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

  def incremental_add(constraint : AbstractConstraint)
    mark = new_mark
    overridden = constraint.satisfy(mark, self)

    while overridden
      overridden = overridden.satisfy(mark, self)
    end
  end

  def incremental_remove(constraint : AbstractConstraint)
    out_v = constraint.output
    constraint.mark_unsatisfied
    constraint.remove_from_graph
    unsatisfied = remove_propagate_from(out_v)
    unsatisfied.each { |u| incremental_add(u) }
  end

  def extract_plan_from_constraints(constraints)
    sources = Vector(AbstractConstraint?).new

    constraints.each { | c |
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
      c = todo.remove_first.not_nil!

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

  def add_constraints_consuming_to(v : Variable, coll)
    determining_c = v.determined_by

    v.constraints.each { | c |
      if (!(c == determining_c)) && c.is_satisfied # Ruby uses .equal?
        coll.append(c)
      end
    }
  end

  def add_propagate(c, mark)
    todo = Vector(AbstractConstraint?).with(c)

    until todo.empty?
      d = todo.remove_first.not_nil!
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
      if c != determining_c && c.is_satisfied
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
    vars = Array(Variable).new(n + 1) { |i| Variable.new }

    # thread a chain of equality constraints through the variables
    (0..(n - 1)).each { | i |
      v1 = vars[i]
      v2 = vars[i + 1]

      EqualityConstraint.new(v1, v2, SYM_REQUIRED, planner)
    }

    StayConstraint.new(vars.last, SYM_STRONG_DEFAULT, planner)
    edit = EditConstraint.new(vars.first, SYM_PREFERRED, planner)
    plan = planner.extract_plan_from_constraints([edit])

    (1..100).each { | v |
      vars.first.value = v
      plan.execute

      if vars.last.value != v
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
      StayConstraint.new(src, SYM_DEFAULT, planner)
      ScaleConstraint.new(src, scale, offset, dst, SYM_REQUIRED, planner)
    }

    planner.change_var(src.not_nil!, 17)
    if dst.not_nil!.value != 1170; raise "Projection 1 failed" end

    planner.change_var(dst.not_nil!, 1050)
    if src.not_nil!.value != 5; raise "Projection 2 failed" end

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

class Sym
  getter :custom_hash

  def initialize(hash : Int32)
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
  getter :arithmetic_value
  
  @arithmetic_value : Int32

  def initialize(strength_sym : Sym)
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
    table = IdentityDictionary(Sym, Int32).new
    table.at_put(SYM_ABSOLUTE_STRONGEST, -10000)
    table.at_put(SYM_REQUIRED,             -800)
    table.at_put(SYM_STRONG_PREFERRED,     -600)
    table.at_put(SYM_PREFERRED,            -400)
    table.at_put(SYM_STRONG_DEFAULT,       -200)
    table.at_put(SYM_DEFAULT,                 0)
    table.at_put(SYM_WEAK_DEFAULT,          500)
    table.at_put(SYM_ABSOLUTE_WEAKEST,    10000)
    table
  end

  def self.create_strength_constants
    constants = IdentityDictionary(Sym, Strength).new
    STRENGHT_TABLE.keys.each { | strength_sym |
      constants.at_put(strength_sym, self.new(strength_sym))
    }
    constants
  end

  STRENGHT_TABLE     = create_strength_table
  STRENGHT_CONSTANTS = create_strength_constants

  def self.of(sym) : Strength
    STRENGHT_CONSTANTS.at(sym).not_nil!
  end
end

abstract class BinaryConstraint < AbstractConstraint
  def initialize(v1 : Variable, v2 : Variable, strength : Sym, planner : Planner)
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
    if @v1
      @v1.remove_constraint(self)
    end

    if @v2
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

  def inputs_do(&block : Variable -> Void)
    if @direction == :forward
      yield @v1
    else
      yield @v2
    end
  end

  def inputs_has_one(&block : Variable -> Bool)
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

    if outx.stay
      execute
    end
  end
end

abstract class UnaryConstraint < AbstractConstraint
  getter :output

  def initialize(v : Variable, strength : Sym, planner : Planner)
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
    if @output
      @output.remove_constraint(self)
    end
    @satisfied = false
  end

  def choose_method(mark)
    @satisfied = @output.mark != mark && @strength.stronger(@output.walk_strength)
  end

  def inputs_do(&block : Variable -> Void)
    # No-op. I have no input variable.
  end

  def inputs_has_one(&block : Variable -> Bool)
    false
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
  def initialize(var1 : Variable, var2 : Variable, strength : Sym, planner : Planner)
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
  def initialize(src : Variable, scale : Variable, offset : Variable, dest : Variable, strength : Sym, planner : Planner)
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
    if @v1
      @v1.remove_constraint(self)
    end

    if @v2
      @v2.remove_constraint(self)
    end

    if @scale
      @scale.remove_constraint(self)
    end

    if @offset
      @offset.remove_constraint(self)
    end

    @direction = nil
  end

  def execute
    if @direction == :forward
      @v2.value = (@v1.value * @scale.value + @offset.value).to_i32
    else
      @v1.value = ((@v2.value - @offset.value) / @scale.value).to_i32
    end
  end

  def inputs_do(&block : Variable -> Void)
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
  property value : Int32
  property :constraints
  property :determined_by
  property :walk_strength
  property :stay
  property :mark
  
  @determined_by : AbstractConstraint?
  @walk_strength : Strength

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

ABSOLUTE_STRONGEST = Strength.of(SYM_ABSOLUTE_STRONGEST)
ABSOLUTE_WEAKEST   = Strength.of(SYM_ABSOLUTE_WEAKEST)
REQUIRED           = Strength.of(SYM_REQUIRED)
