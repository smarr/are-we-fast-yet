// @ts-check
// The benchmark in its current state is a derivation from the SOM version,
// which is derived from Mario Wolczko's Smalltalk version of DeltaBlue.
//
// The original license details are available here:
// http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html

const { Benchmark } = require('./benchmark');
const { Vector, IdentityDictionary } = require('./som');

class Plan extends Vector {
  constructor() {
    super(15);
  }

  execute() {
    this.forEach((c) => c.execute());
  }
}

class Sym {
  constructor(hash) {
    this.hash = hash;
  }

  customHash() {
    return this.hash;
  }
}

const ABSOLUTE_STRONGEST = new Sym(0);
const REQUIRED = new Sym(1);
const STRONG_PREFERRED = new Sym(2);
const PREFERRED = new Sym(3);
const STRONG_DEFAULT = new Sym(4);
const DEFAULT = new Sym(5);
const WEAK_DEFAULT = new Sym(6);
const ABSOLUTE_WEAKEST = new Sym(7);

function createStrengthTable() {
  const strengthTable = new IdentityDictionary();
  strengthTable.atPut(ABSOLUTE_STRONGEST, -10000);
  strengthTable.atPut(REQUIRED, -800);
  strengthTable.atPut(STRONG_PREFERRED, -600);
  strengthTable.atPut(PREFERRED, -400);
  strengthTable.atPut(STRONG_DEFAULT, -200);
  strengthTable.atPut(DEFAULT, 0);
  strengthTable.atPut(WEAK_DEFAULT, 500);
  strengthTable.atPut(ABSOLUTE_WEAKEST, 10000);
  return strengthTable;
}

class Strength {
  constructor(symbolicValue) {
    this.arithmeticValue = Strength.strengthTable.at(symbolicValue);
  }

  sameAs(s) {
    return this.arithmeticValue === s.arithmeticValue;
  }

  stronger(s) {
    return this.arithmeticValue < s.arithmeticValue;
  }

  weaker(s) {
    return this.arithmeticValue > s.arithmeticValue;
  }

  strongest(s) {
    return s.stronger(this) ? s : this;
  }

  weakest(s) {
    return s.weaker(this) ? s : this;
  }

  static of(strength) {
    return Strength.strengthConstant.at(strength);
  }

  static strengthTable = createStrengthTable();

  static createStrengthConstants() {
    const strengthConstant = new IdentityDictionary();
    Strength.strengthTable.getKeys().forEach((key) => {
      strengthConstant.atPut(key, new Strength(key));
    });
    return strengthConstant;
  }

  static strengthConstant = Strength.createStrengthConstants();

  static absoluteWeakest = Strength.of(ABSOLUTE_WEAKEST);

  static required = Strength.of(REQUIRED);
}

class AbstractConstraint {
  constructor(strengthSym) {
    this.strength = Strength.of(strengthSym);
  }

  isInput() {
    return false;
  }

  addConstraint(planner) {
    this.addToGraph();
    planner.incrementalAdd(this);
  }

  destroyConstraint(planner) {
    if (this.isSatisfied()) {
      planner.incrementalRemove(this);
    }
    this.removeFromGraph();
  }

  inputsKnown(mark) {
    return !this.inputsHasOne((v) => !(v.mark === mark || v.stay || v.determinedBy === null));
  }

  satisfy(mark, planner) {
    let overridden;
    this.chooseMethod(mark);

    if (this.isSatisfied()) {
      // constraint can be satisfied
      // mark inputs to allow cycle detection in addPropagate
      this.inputsDo((i) => { i.mark = mark; });

      const out = this.getOutput();
      overridden = out.determinedBy;
      if (overridden !== null) {
        overridden.markUnsatisfied();
      }
      out.determinedBy = this;
      if (!planner.addPropagate(this, mark)) {
        throw new Error('Cycle encountered');
      }
      out.mark = mark;
    } else {
      overridden = null;
      if (this.strength.sameAs(Strength.required)) {
        throw new Error('Could not satisfy a required constraint');
      }
    }
    return overridden;
  }
}

class BinaryConstraint extends AbstractConstraint {
  // eslint-disable-next-line no-unused-vars
  constructor(var1, var2, strength, planner) {
    super(strength);
    this.v1 = var1;
    this.v2 = var2;
    this.direction = null;
  }

  isSatisfied() {
    return this.direction !== null;
  }

  addToGraph() {
    this.v1.addConstraint(this);
    this.v2.addConstraint(this);
    this.direction = null;
  }

  removeFromGraph() {
    if (this.v1 !== null) {
      this.v1.removeConstraint(this);
    }
    if (this.v2 !== null) {
      this.v2.removeConstraint(this);
    }
    this.direction = null;
  }

  chooseMethod(mark) {
    if (this.v1.mark === mark) {
      if (this.v2.mark !== mark && this.strength.stronger(this.v2.walkStrength)) {
        this.direction = 'forward';
        return this.direction;
      }
      this.direction = null;
      return this.direction;
    }

    if (this.v2.mark === mark) {
      if (this.v1.mark !== mark && this.strength.stronger(this.v1.walkStrength)) {
        this.direction = 'backward';
        return this.direction;
      }
      this.direction = null;
      return this.direction;
    }

    // If we get here, neither variable is marked, so we have a choice.
    if (this.v1.walkStrength.weaker(this.v2.walkStrength)) {
      if (this.strength.stronger(this.v1.walkStrength)) {
        this.direction = 'backward';
        return this.direction;
      }
      this.direction = null;
      return this.direction;
    }
    if (this.strength.stronger(this.v2.walkStrength)) {
      this.direction = 'forward';
      return this.direction;
    }
    this.direction = null;
    return this.direction;
  }

  inputsDo(fn) {
    if (this.direction === 'forward') {
      fn(this.v1);
    } else {
      fn(this.v2);
    }
  }

  inputsHasOne(fn) {
    if (this.direction === 'forward') {
      return fn(this.v1);
    }
    return fn(this.v2);
  }

  markUnsatisfied() {
    this.direction = null;
  }

  getOutput() {
    return this.direction === 'forward' ? this.v2 : this.v1;
  }

  recalculate() {
    let ihn;
    let out;

    if (this.direction === 'forward') {
      ihn = this.v1; out = this.v2;
    } else {
      ihn = this.v2; out = this.v1;
    }

    out.walkStrength = this.strength.weakest(ihn.walkStrength);
    out.stay = ihn.stay;
    if (out.stay) {
      this.execute();
    }
  }
}

class UnaryConstraint extends AbstractConstraint {
  constructor(v, strength, planner) {
    super(strength);
    this.output = v;
    this.satisfied = false;

    this.addConstraint(planner);
  }

  isSatisfied() {
    return this.satisfied;
  }

  addToGraph() {
    this.output.addConstraint(this);
    this.satisfied = false;
  }

  removeFromGraph() {
    if (this.output !== null) {
      this.output.removeConstraint(this);
    }
    this.satisfied = false;
  }

  chooseMethod(mark) {
    this.satisfied = this.output.mark !== mark
      && this.strength.stronger(this.output.walkStrength);
    return null;
  }

  // eslint-disable-next-line no-unused-vars
  inputsDo(fn) {
    // I have no input variables
  }

  // eslint-disable-next-line no-unused-vars
  inputsHasOne(fn) {
    return false;
  }

  markUnsatisfied() {
    this.satisfied = false;
  }

  getOutput() {
    return this.output;
  }

  recalculate() {
    this.output.walkStrength = this.strength;
    this.output.stay = !this.isInput();
    if (this.output.stay) {
      this.execute(); // stay optimization
    }
  }
}

class EditConstraint extends UnaryConstraint {
  isInput() {
    return true;
  }

  execute() {}
}

class EqualityConstraint extends BinaryConstraint {
  constructor(var1, var2, strength, planner) {
    super(var1, var2, strength, planner);
    this.addConstraint(planner);
  }

  execute() {
    if (this.direction === 'forward') {
      this.v2.value = this.v1.value;
    } else {
      this.v1.value = this.v2.value;
    }
  }
}

class ScaleConstraint extends BinaryConstraint {
  constructor(src, scale, offset, dest, strength, planner) {
    super(src, dest, strength, planner);
    this.scale = scale;
    this.offset = offset;

    this.addConstraint(planner);
  }

  addToGraph() {
    this.v1.addConstraint(this);
    this.v2.addConstraint(this);
    this.scale.addConstraint(this);
    this.offset.addConstraint(this);
    this.direction = null;
  }

  removeFromGraph() {
    if (this.v1 !== null) { this.v1.removeConstraint(this); }
    if (this.v2 !== null) { this.v2.removeConstraint(this); }
    if (this.scale !== null) { this.scale.removeConstraint(this); }
    if (this.offset !== null) { this.offset.removeConstraint(this); }
    this.direction = null;
  }

  execute() {
    if (this.direction === 'forward') {
      this.v2.value = this.v1.value * this.scale.value + this.offset.value;
    } else {
      this.v1.value = (this.v2.value - this.offset.value) / this.scale.value;
    }
  }

  inputsDo(fn) {
    if (this.direction === 'forward') {
      fn(this.v1);
      fn(this.scale);
      fn(this.offset);
    } else {
      fn(this.v2);
      fn(this.scale);
      fn(this.offset);
    }
  }

  recalculate() {
    let ihn;
    let out;

    if (this.direction === 'forward') {
      ihn = this.v1; out = this.v2;
    } else {
      out = this.v1; ihn = this.v2;
    }

    out.walkStrength = this.strength.weakest(ihn.walkStrength);
    out.stay = ihn.stay && this.scale.stay && this.offset.stay;
    if (out.stay) {
      this.execute(); // stay optimization
    }
  }
}

class StayConstraint extends UnaryConstraint {
  execute() {}
}

class Variable {
  constructor() {
    this.value = 0;
    this.constraints = new Vector(2);
    this.determinedBy = null;
    this.walkStrength = Strength.absoluteWeakest;
    this.stay = true;
    this.mark = 0;
  }

  addConstraint(c) {
    this.constraints.append(c);
  }

  removeConstraint(c) {
    this.constraints.remove(c);
    if (this.determinedBy === c) {
      this.determinedBy = null;
    }
  }

  static value(aValue) {
    const v = new Variable();
    v.value = aValue;
    return v;
  }
}

class Planner {
  constructor() {
    this.currentMark = 1;
  }

  newMark() {
    this.currentMark += 1;
    return this.currentMark;
  }

  incrementalAdd(c) {
    const mark = this.newMark();
    let overridden = c.satisfy(mark, this);

    while (overridden !== null) {
      overridden = overridden.satisfy(mark, this);
    }
  }

  incrementalRemove(c) {
    const out = c.getOutput();
    c.markUnsatisfied();
    c.removeFromGraph();

    const unsatisfied = this.removePropagateFrom(out);
    unsatisfied.forEach((u) => this.incrementalAdd(u));
  }

  extractPlanFromConstraints(constraints) {
    const sources = new Vector();

    constraints.forEach((c) => {
      if (c.isInput() && c.isSatisfied()) {
        sources.append(c);
      }
    });

    return this.makePlan(sources);
  }

  makePlan(sources) {
    const mark = this.newMark();
    const plan = new Plan();
    const todo = sources;

    while (!todo.isEmpty()) {
      const c = todo.removeFirst();

      if (c.getOutput().mark !== mark && c.inputsKnown(mark)) {
        // not in plan already and eligible for inclusion
        plan.append(c);
        c.getOutput().mark = mark;
        this.addConstraintsConsumingTo(c.getOutput(), todo);
      }
    }
    return plan;
  }

  propagateFrom(v) {
    const todo = new Vector();
    this.addConstraintsConsumingTo(v, todo);

    while (!todo.isEmpty()) {
      const c = todo.removeFirst();
      c.execute();
      this.addConstraintsConsumingTo(c.getOutput(), todo);
    }
  }

  addConstraintsConsumingTo(v, coll) {
    const determiningC = v.determinedBy;

    v.constraints.forEach((c) => {
      if (c !== determiningC && c.isSatisfied()) {
        coll.append(c);
      }
    });
  }

  addPropagate(c, mark) {
    const todo = Vector.with(c);

    while (!todo.isEmpty()) {
      const d = todo.removeFirst();

      if (d.getOutput().mark === mark) {
        this.incrementalRemove(c);
        return false;
      }
      d.recalculate();
      this.addConstraintsConsumingTo(d.getOutput(), todo);
    }
    return true;
  }

  change(v, newValue) {
    const editC = new EditConstraint(v, PREFERRED, this);
    const editV = Vector.with(editC);
    const plan = this.extractPlanFromConstraints(editV);

    for (let i = 0; i < 10; i += 1) {
      v.value = newValue;
      plan.execute();
    }
    editC.destroyConstraint(this);
  }

  constraintsConsuming(v, fn) {
    const determiningC = v.determinedBy;
    v.constraints.forEach((c) => {
      if (c !== determiningC && c.isSatisfied()) {
        fn(c);
      }
    });
  }

  removePropagateFrom(out) {
    const unsatisfied = new Vector();

    out.determinedBy = null;
    out.walkStrength = Strength.absoluteWeakest;
    out.stay = true;

    const todo = Vector.with(out);

    while (!todo.isEmpty()) {
      const v = todo.removeFirst();

      v.constraints.forEach((c) => {
        if (!c.isSatisfied()) { unsatisfied.append(c); }
      });

      this.constraintsConsuming(v, (c) => {
        c.recalculate();
        todo.append(c.getOutput());
      });
    }

    unsatisfied.sort((c1, c2) => c1.strength.stronger(c2.strength));
    return unsatisfied;
  }

  static chainTest(n) {
    const planner = new Planner();
    const vars = new Array(n + 1);

    for (let i = 0; i < n + 1; i += 1) {
      vars[i] = new Variable();
    }

    // Build chain of n equality constraints
    for (let i = 0; i < n; i += 1) {
      const v1 = vars[i];
      const v2 = vars[i + 1];
      new EqualityConstraint(v1, v2, REQUIRED, planner);
    }

    new StayConstraint(vars[n], STRONG_DEFAULT, planner);

    const editC = new EditConstraint(vars[0], PREFERRED, planner);
    const editV = Vector.with(editC);
    const plan = planner.extractPlanFromConstraints(editV);

    for (let i = 0; i < 100; i += 1) {
      vars[0].value = i;
      plan.execute();
      if (vars[n].value !== i) {
        throw new Error('Chain test failed!');
      }
    }
    editC.destroyConstraint(planner);
  }

  static projectionTest(n) {
    const planner = new Planner();
    const dests = new Vector();
    const scale = Variable.value(10);
    const offset = Variable.value(1000);

    let src = null;
    let dst = null;

    for (let i = 1; i <= n; i += 1) {
      src = Variable.value(i);
      dst = Variable.value(i);
      dests.append(dst);
      new StayConstraint(src, DEFAULT, planner);
      new ScaleConstraint(src, scale, offset, dst, REQUIRED, planner);
    }

    planner.change(src, 17);
    if (dst.value !== 1170) {
      throw new Error('Projection test 1 failed!');
    }

    planner.change(dst, 1050);
    if (src.value !== 5) {
      throw new Error('Projection test 2 failed!');
    }

    planner.change(scale, 5);
    for (let i = 0; i < n - 1; i += 1) {
      if (dests.at(i).value !== (i + 1) * 5 + 1000) {
        throw new Error('Projection test 3 failed!');
      }
    }

    planner.change(offset, 2000);
    for (let i = 0; i < n - 1; i += 1) {
      if (dests.at(i).value !== (i + 1) * 5 + 2000) {
        throw new Error('Projection test 4 failed!');
      }
    }
  }
}

class DeltaBlue extends Benchmark {
  innerBenchmarkLoop(innerIterations) {
    Planner.chainTest(innerIterations);
    Planner.projectionTest(innerIterations);
    return true;
  }
}

exports.newInstance = () => new DeltaBlue();
