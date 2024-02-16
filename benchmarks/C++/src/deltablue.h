#pragma once

#include <any>

#include "benchmark.h"
#include "memory/object_tracker.h"
#include "som/error.h"
#include "som/identity_dictionary.h"
#include "som/vector.h"

enum Direction { FORWARD, BACKWARD, NONE };

class Sym {
 private:
  uint32_t hash;

 public:
  explicit constexpr Sym(uint32_t hash_value) noexcept : hash(hash_value) {}

  [[nodiscard]] uint32_t customHash() const { return hash; }
};

class Strength {
 public:
  static const Sym ABSOLUTE_STRONGEST;
  static const Sym REQUIRED;
  static const Sym STRONG_PREFERRED;
  static const Sym PREFERRED;
  static const Sym STRONG_DEFAULT;
  static const Sym DEFAULT;
  static const Sym WEAK_DEFAULT;
  static const Sym ABSOLUTE_WEAKEST;

 private:
  int32_t const _arithmeticValue;
  const Sym* const _symbolicValue;

  static IdentityDictionary<const Sym, int32_t>* createStrengthTable();
  static IdentityDictionary<const Sym, const Strength*>*
  createStrengthConstants();
  static void releaseStrengthConstants();

 public:
  explicit Strength(const Sym* const symbolicValue)
      : _arithmeticValue(*_strengthTable->at(symbolicValue)),
        _symbolicValue(symbolicValue) {}

  bool sameAs(const Strength* const s) const {
    return _arithmeticValue == s->_arithmeticValue;
  }

  bool stronger(const Strength* const s) const {
    return _arithmeticValue < s->_arithmeticValue;
  }

  bool weaker(const Strength* const s) const {
    return _arithmeticValue > s->_arithmeticValue;
  }

  const Strength* strongest(const Strength* const s) const {
    return s->stronger(this) ? s : this;
  }

  const Strength* weakest(const Strength* const s) const {
    return s->weaker(this) ? s : this;
  }

  [[nodiscard]] int32_t getArithmeticValue() const { return _arithmeticValue; }

  static const Strength* of(const Sym* const strength) {
    return *_strengthConstant->at(strength);
  }
  static const Strength* absoluteWeakest();
  static const Strength* required();
  static void initializeConstants();
  static void releaseConstants();

 private:
  static const Strength* _absoluteWeakest;
  static const Strength* _required;

  static IdentityDictionary<const Sym, int32_t>* _strengthTable;
  static IdentityDictionary<const Sym, const Strength*>* _strengthConstant;
};

class Planner;
class Variable;

class AbstractConstraint : public TrackedObject {
 protected:
  const Strength* const _strength;
  Planner* const _planer{nullptr};

 public:
  explicit AbstractConstraint(const Sym* strength)
      : _strength(Strength::of(strength)) {}

  [[nodiscard]] const Strength* getStrength() { return _strength; }

  virtual bool isInput() { return false; }
  void addConstraint(Planner* planner);
  void destroyConstraint(Planner* planner);
  bool inputsKnown(int32_t mark);
  AbstractConstraint* satisfy(int32_t mark, Planner* planner);

  virtual bool isSatisfied() = 0;
  virtual void addToGraph() = 0;
  virtual void removeFromGraph() = 0;
  virtual Direction chooseMethod(int32_t mark) = 0;
  virtual void execute() = 0;
  virtual void inputsDo(std::function<void(Variable*)> fn) = 0;
  virtual bool inputsHasOne(std::function<bool(Variable*)> fn) = 0;
  virtual void markUnsatisfied() = 0;
  virtual Variable* getOutput() = 0;
  virtual void recalculate() = 0;
};

class Variable : public TrackedObject {
 private:
  int32_t _value{0};
  Vector<AbstractConstraint*>* _constraints;
  AbstractConstraint* _determinedBy{nullptr};
  int32_t _mark{0};
  const Strength* _walkStrength;
  bool _stay{true};

 public:
  Variable()
      : _constraints(new Vector<AbstractConstraint*>(2)),
        _walkStrength(Strength::absoluteWeakest()) {}

  ~Variable() override { delete _constraints; }

  static Variable* value(int32_t aValue) {
    auto* v = new Variable();
    v->setValue(aValue);
    return v;
  }

  void addConstraint(AbstractConstraint* c) { _constraints->append(c); }

  [[nodiscard]] Vector<AbstractConstraint*>* getConstraints() const {
    return _constraints;
  }

  [[nodiscard]] AbstractConstraint* getDeterminedBy() const {
    return _determinedBy;
  }

  void setDeterminedBy(AbstractConstraint* c) { _determinedBy = c; }

  [[nodiscard]] int32_t getMark() const { return _mark; }

  void setMark(int32_t markValue) { _mark = markValue; }

  void removeConstraint(AbstractConstraint* c) {
    _constraints->remove(c);
    if (_determinedBy == c) {
      _determinedBy = nullptr;
    }
  }

  [[nodiscard]] bool getStay() const { return _stay; }
  void setStay(bool v) { _stay = v; }

  [[nodiscard]] int32_t getValue() const { return _value; }

  void setValue(int32_t value) { _value = value; }

  [[nodiscard]] const Strength* getWalkStrength() const {
    return _walkStrength;
  }

  void setWalkStrength(const Strength* strength) { _walkStrength = strength; }
};

class BinaryConstraint : public AbstractConstraint {
 protected:
  Variable* _v1;
  Variable* _v2;
  Direction _direction{NONE};

 public:
  BinaryConstraint(Variable* var1,
                   Variable* var2,
                   const Sym* strength,
                   Planner*)
      : AbstractConstraint(strength), _v1(var1), _v2(var2) {}

  // can't free _v1 and _v2 here,
  // because they are shared with other constraints
  ~BinaryConstraint() override = default;

  bool isSatisfied() override { return _direction != NONE; }

  void addToGraph() override {
    _v1->addConstraint(this);
    _v2->addConstraint(this);
    _direction = NONE;
  }

  void removeFromGraph() override {
    _v1->removeConstraint(this);
    _v2->removeConstraint(this);
    _direction = NONE;
  }

  Direction chooseMethod(int32_t mark) override {
    if (_v1->getMark() == mark) {
      if (_v2->getMark() != mark &&
          _strength->stronger(_v2->getWalkStrength())) {
        _direction = FORWARD;
        return _direction;
      }

      _direction = NONE;
      return _direction;
    }

    if (_v2->getMark() == mark) {
      if (_v1->getMark() != mark &&
          _strength->stronger(_v1->getWalkStrength())) {
        _direction = BACKWARD;
        return _direction;
      }

      _direction = NONE;
      return _direction;
    }

    if (_v1->getWalkStrength()->weaker(_v2->getWalkStrength())) {
      if (_strength->stronger(_v1->getWalkStrength())) {
        _direction = BACKWARD;
        return _direction;
      }

      _direction = NONE;
      return _direction;
    }

    if (_strength->stronger(_v2->getWalkStrength())) {
      _direction = FORWARD;
      return _direction;
    }

    _direction = NONE;
    return _direction;
  }

  void inputsDo(std::function<void(Variable*)> fn) override {
    if (_direction == FORWARD) {
      fn(_v1);
    } else {
      fn(_v2);
    }
  }

  bool inputsHasOne(std::function<bool(Variable*)> fn) override {
    if (_direction == FORWARD) {
      return fn(_v1);
    }
    return fn(_v2);
  }

  void markUnsatisfied() override { _direction = NONE; }
  Variable* getOutput() override { return _direction == FORWARD ? _v2 : _v1; }

  void recalculate() override {
    Variable* in = nullptr;
    Variable* out = nullptr;

    if (_direction == FORWARD) {
      in = _v1;
      out = _v2;
    } else {
      in = _v2;
      out = _v1;
    }

    out->setWalkStrength(_strength->weakest(in->getWalkStrength()));
    out->setStay(in->getStay());
    if (out->getStay()) {
      execute();
    }
  }
};

class UnaryConstraint : public AbstractConstraint {
 protected:
  Variable* _output;       // possible output variable
  bool _satisfied{false};  // true if I am currently satisfied

 public:
  UnaryConstraint(Variable* v, const Sym* strength, Planner*)
      : AbstractConstraint(strength), _output(v) {
    // moved to subclass to avoid calling wrong method
    // addConstraint(planner);
  }

  // can't free _output here, because it is shared with other constraints
  ~UnaryConstraint() override = default;

  bool isSatisfied() override { return _satisfied; }

  void addToGraph() override {
    _output->addConstraint(this);
    _satisfied = false;
  }

  void removeFromGraph() override {
    if (_output != nullptr) {
      _output->removeConstraint(this);
    }
    _satisfied = false;
  }

  Direction chooseMethod(int32_t mark) override {
    _satisfied = (_output->getMark() != mark) &&
                 _strength->stronger(_output->getWalkStrength());
    return NONE;
  }

  void inputsDo(std::function<void(Variable*)>) override {}

  bool inputsHasOne(std::function<bool(Variable*)>) override { return false; }

  void markUnsatisfied() override { _satisfied = false; }
  Variable* getOutput() override { return _output; }

  void recalculate() override {
    _output->setWalkStrength(_strength);
    _output->setStay(!isInput());
    if (_output->getStay()) {
      execute();
    }
  }
};

class EditConstraint : public UnaryConstraint {
 public:
  EditConstraint(Variable* v, const Sym* strength, Planner* planner)
      : UnaryConstraint(v, strength, planner) {
    addConstraint(planner);  // moved here from UnaryConstraint constructor to
                             // make sure the right method is called
  }

  bool isInput() override { return true; }

  void execute() override {
    // Edit constraints do nothing.
  }
};

class EqualityConstraint : public BinaryConstraint {
 public:
  EqualityConstraint(Variable* var1,
                     Variable* var2,
                     const Sym* strength,
                     Planner* planner)
      : BinaryConstraint(var1, var2, strength, planner) {
    addConstraint(planner);
  }

  void execute() override {
    if (_direction == Direction::FORWARD) {
      _v2->setValue(_v1->getValue());
    } else {
      _v1->setValue(_v2->getValue());
    }
  }
};

class Plan : public Vector<AbstractConstraint*> {
 public:
  Plan() : Vector<AbstractConstraint*>(15) {}

  void execute() {
    forEach([&](AbstractConstraint* c) -> void { c->execute(); });
  }
};

class ScaleConstraint : public BinaryConstraint {
 private:
  Variable* _scale;
  Variable* _offset;

 public:
  ScaleConstraint(Variable* src,
                  Variable* scale,
                  Variable* offset,
                  Variable* dest,
                  const Sym* strength,
                  Planner* planner)
      : BinaryConstraint(src, dest, strength, planner),
        _scale(scale),
        _offset(offset) {
    addConstraint(planner);
  }

  void addToGraph() override {
    _v1->addConstraint(this);
    _v2->addConstraint(this);
    _scale->addConstraint(this);
    _offset->addConstraint(this);
    _direction = NONE;
  }

  void removeFromGraph() override {
    if (_v1 != nullptr) {
      _v1->removeConstraint(this);
    }
    if (_v2 != nullptr) {
      _v2->removeConstraint(this);
    }
    if (_scale != nullptr) {
      _scale->removeConstraint(this);
    }
    if (_offset != nullptr) {
      _offset->removeConstraint(this);
    }
    _direction = NONE;
  }

  void execute() override {
    if (_direction == FORWARD) {
      _v2->setValue(_v1->getValue() * _scale->getValue() + _offset->getValue());
    } else {
      _v1->setValue((_v2->getValue() - _offset->getValue()) /
                    _scale->getValue());
    }
  }

  void inputsDo(std::function<void(Variable*)> fn) override {
    if (_direction == FORWARD) {
      fn(_v1);
      fn(_scale);
      fn(_offset);
    } else {
      fn(_v2);
      fn(_scale);
      fn(_offset);
    }
  }
  void recalculate() override {
    Variable* in = nullptr;
    Variable* out = nullptr;

    if (_direction == FORWARD) {
      in = _v1;
      out = _v2;
    } else {
      out = _v1;
      in = _v2;
    }

    out->setWalkStrength(_strength->weakest(in->getWalkStrength()));
    out->setStay(in->getStay() && _scale->getStay() && _offset->getStay());
    if (out->getStay()) {
      execute();  // stay optimization
    }
  }
};

class StayConstraint : public UnaryConstraint {
 public:
  StayConstraint(Variable* v, const Sym* strength, Planner* planner)
      : UnaryConstraint(v, strength, planner) {
    addConstraint(planner);  // moved here from UnaryConstraint constructor to
                             // make sure the right method is called
  }

  void execute() override {
    // StayConstraints do nothing.
  }
};

class Planner {
 private:
  int32_t _currentMark{1};

 public:
  Planner() = default;

  void incrementalAdd(AbstractConstraint* c) {
    const int32_t mark = newMark();
    AbstractConstraint* overridden = c->satisfy(mark, this);

    while (overridden != nullptr) {
      overridden = overridden->satisfy(mark, this);
    }
  }

  void incrementalRemove(AbstractConstraint* c) {
    Variable* out = c->getOutput();
    c->markUnsatisfied();
    c->removeFromGraph();

    Vector<AbstractConstraint*>* unsatisfied = removePropagateFrom(out);
    unsatisfied->forEach(
        [this](AbstractConstraint* u) -> void { incrementalAdd(u); });
    delete unsatisfied;
  }

  Plan* extractPlanFromConstraints(Vector<AbstractConstraint*>* constraints) {
    Vector<AbstractConstraint*> sources = Vector<AbstractConstraint*>();
    constraints->forEach([&](AbstractConstraint* c) -> void {
      if (c->isInput() && c->isSatisfied()) {
        sources.append(c);
      }
    });
    return makePlan(&sources);
  }

  Plan* makePlan(Vector<AbstractConstraint*>* sources) {
    const int32_t mark = newMark();
    Plan* plan = new Plan();
    Vector<AbstractConstraint*>* todo = sources;

    while (!todo->isEmpty()) {
      AbstractConstraint* c = todo->removeFirst();

      if (c->getOutput()->getMark() != mark && c->inputsKnown(mark)) {
        plan->append(c);
        c->getOutput()->setMark(mark);
        addConstraintsConsumingTo(c->getOutput(), todo);
      }
    }

    return plan;
  }

  void addConstraintsConsumingTo(Variable* v,
                                 Vector<AbstractConstraint*>* coll) {
    AbstractConstraint* determiningC = v->getDeterminedBy();

    v->getConstraints()->forEach(
        [&coll, determiningC](AbstractConstraint* c) -> void {
          if (c != determiningC && c->isSatisfied()) {
            coll->append(c);
          }
        });
  }

  bool addPropagate(AbstractConstraint* c, int32_t mark) {
    Vector<AbstractConstraint*>* todo = Vector<AbstractConstraint*>::with(c);

    while (!todo->isEmpty()) {
      AbstractConstraint* d = todo->removeFirst();

      if (d->getOutput()->getMark() == mark) {
        incrementalRemove(c);
        delete todo;
        return false;
      }

      d->recalculate();
      addConstraintsConsumingTo(d->getOutput(), todo);
    }

    delete todo;
    return true;
  }

  void change(Variable* var, int32_t newValue) {
    auto* editC = new EditConstraint(var, &Strength::PREFERRED, this);
    auto* editV = Vector<AbstractConstraint*>::with(editC);
    Plan* plan = extractPlanFromConstraints(editV);

    for (int32_t i = 0; i < 10; i += 1) {
      var->setValue(newValue);
      plan->execute();
    }

    editC->destroyConstraint(this);
    delete plan;
    delete editV;
  }

  void constraintsConsuming(Variable* v,
                            std::function<void(AbstractConstraint*)> fn) {
    AbstractConstraint* determiningC = v->getDeterminedBy();

    v->getConstraints()->forEach([&](AbstractConstraint* c) -> void {
      if (c != determiningC && c->isSatisfied()) {
        fn(c);
      }
    });
  }

  int32_t newMark() {
    _currentMark += 1;
    return _currentMark;
  }

  Vector<AbstractConstraint*>* removePropagateFrom(Variable* out) {
    auto* unsatisfied = new Vector<AbstractConstraint*>();

    out->setDeterminedBy(nullptr);
    out->setWalkStrength(Strength::absoluteWeakest());
    out->setStay(true);

    auto* todo = Vector<Variable*>::with(out);

    while (!todo->isEmpty()) {
      Variable* v = todo->removeFirst();

      v->getConstraints()->forEach(
          [unsatisfied](AbstractConstraint* c) -> void {
            if (!c->isSatisfied()) {
              unsatisfied->append(c);
            }
          });

      constraintsConsuming(v, [&todo](AbstractConstraint* c) -> void {
        c->recalculate();
        todo->append(c->getOutput());
      });
    }
    delete todo;

    unsatisfied->sort(
        [](AbstractConstraint* c1, AbstractConstraint* c2) -> int32_t {
          return c1->getStrength()->stronger(c2->getStrength()) ? -1 : 1;
        });

    return unsatisfied;
  }

  static void chainTest(int32_t n) {
    Strength::initializeConstants();
    Planner planner = Planner();
    auto* vars = new Variable*[n + 1];
    for (int32_t i = 0; i < n + 1; i += 1) {
      vars[i] = new Variable();
    }
    for (int32_t i = 0; i < n; i += 1) {
      Variable* v1 = vars[i];
      Variable* v2 = vars[i + 1];
      new EqualityConstraint(v1, v2, &Strength::REQUIRED, &planner);
    }
    new StayConstraint(vars[n], &Strength::STRONG_DEFAULT, &planner);

    auto* editC = new EditConstraint(vars[0], &Strength::PREFERRED, &planner);
    auto* editV = Vector<AbstractConstraint*>::with(editC);
    Plan* plan = planner.extractPlanFromConstraints(editV);
    delete editV;

    for (int32_t i = 0; i < 100; i += 1) {
      vars[0]->setValue(i);
      plan->execute();
      if (vars[n]->getValue() != i) {
        throw Error("Chain test failed!");
      }
    }
    editC->destroyConstraint(&planner);
    delete plan;
    delete[] vars;
    ObjectTracker::releaseAll();
  }

  static void projectionTest(int32_t n) {
    Planner planner = Planner();
    Vector<Variable*> dests = Vector<Variable*>();

    Variable* scale = Variable::value(10);
    Variable* offset = Variable::value(1000);

    Variable* src = nullptr;
    Variable* dst = nullptr;
    for (int32_t i = 1; i <= n; i += 1) {
      src = Variable::value(i);
      dst = Variable::value(i);
      dests.append(dst);
      new StayConstraint(src, &Strength::DEFAULT, &planner);
      new ScaleConstraint(src, scale, offset, dst, &Strength::REQUIRED,
                          &planner);
    }

    planner.change(src, 17);
    if (dst->getValue() != 1170) {
      throw Error("Projection test 1 failed!");
    }

    planner.change(dst, 1050);
    if (src->getValue() != 5) {
      throw Error("Projection test 2 failed!");
    }

    planner.change(scale, 5);
    for (int32_t i = 0; i < n - 1; i += 1) {
      Variable* di = *dests.at(i);
      if (di->getValue() != (i + 1) * 5 + 1000) {
        throw Error("Projection test 3 failed!");
      }
    }

    planner.change(offset, 2000);
    for (int32_t i = 0; i < n - 1; i += 1) {
      Variable* di = *dests.at(i);
      if (di->getValue() != (i + 1) * 5 + 2000) {
        throw Error("Projection test 4 failed!");
      }
    }
    ObjectTracker::releaseAll();
    Strength::releaseConstants();
  }
};

class DeltaBlue : public Benchmark {
 public:
  bool inner_benchmark_loop(int32_t innerIterations) override {
    Planner::chainTest(innerIterations);
    Planner::projectionTest(innerIterations);
    return true;
  }

  std::any benchmark() override { return nullptr; }

  bool verify_result(std::any) override { return false; }
};
