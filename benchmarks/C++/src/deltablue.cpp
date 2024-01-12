#include "deltablue.h"
#include "som/dictionary.h"
#include "som/error.h"
#include "som/identity_dictionary.h"

#include <cstdint>

// Set static member variable
const Sym Strength::ABSOLUTE_STRONGEST(0);
const Sym Strength::REQUIRED(1);
const Sym Strength::STRONG_PREFERRED(2);
const Sym Strength::PREFERRED(3);
const Sym Strength::STRONG_DEFAULT(4);
const Sym Strength::DEFAULT(5);
const Sym Strength::WEAK_DEFAULT(6);
const Sym Strength::ABSOLUTE_WEAKEST(7);

IdentityDictionary<const Sym, int32_t>* Strength::_strengthTable;
IdentityDictionary<const Sym, const Strength*>* Strength::_strengthConstant;
const Strength* Strength::_absoluteWeakest;
const Strength* Strength::_required;

const Strength* Strength::absoluteWeakest() {
  return _absoluteWeakest;
}

const Strength* Strength::required() {
  return _required;
}

IdentityDictionary<const Sym, int32_t>* Strength::createStrengthTable() {
  auto* strengthTable = new IdentityDictionary<const Sym, int32_t>();
  strengthTable->atPut(&Strength::ABSOLUTE_STRONGEST, -10000);
  strengthTable->atPut(&Strength::REQUIRED, -800);
  strengthTable->atPut(&Strength::STRONG_PREFERRED, -600);
  strengthTable->atPut(&Strength::PREFERRED, -400);
  strengthTable->atPut(&Strength::STRONG_DEFAULT, -200);
  strengthTable->atPut(&Strength::DEFAULT, 0);
  strengthTable->atPut(&Strength::WEAK_DEFAULT, 500);
  strengthTable->atPut(&Strength::ABSOLUTE_WEAKEST, 10000);
  return strengthTable;
}

IdentityDictionary<const Sym, const Strength*>*
Strength::createStrengthConstants() {
  auto* strengthConstant = new IdentityDictionary<const Sym, const Strength*>();
  auto* keys = _strengthTable->getKeys();
  keys->forEach([&strengthConstant](const Sym* const key) -> void {
    const Sym* keySym = dynamic_cast<const Sym*>(key);
    strengthConstant->atPut(keySym, new Strength(keySym));
  });
  delete keys;

  return strengthConstant;
}

void Strength::initializeConstants() {
  _strengthTable = createStrengthTable();
  _strengthConstant = createStrengthConstants();
  _absoluteWeakest = of(&Strength::ABSOLUTE_WEAKEST);
  _required = of(&Strength::REQUIRED);
}

void Strength::releaseStrengthConstants() {
  _strengthConstant->destroyValues();
  delete _strengthConstant;
}

void Strength::releaseConstants() {
  delete _strengthTable;
  Strength::releaseStrengthConstants();
  _absoluteWeakest = nullptr;
  _required = nullptr;
}

void AbstractConstraint::addConstraint(Planner* planner) {
  addToGraph();
  planner->incrementalAdd(this);
}

void AbstractConstraint::destroyConstraint(Planner* planner) {
  if (isSatisfied()) {
    planner->incrementalRemove(this);
  }
  removeFromGraph();
}

bool AbstractConstraint::inputsKnown(int32_t mark) {
  return !inputsHasOne([mark](Variable* v) -> bool {
    return !(v->getMark() == mark || v->getStay() ||
             v->getDeterminedBy() == nullptr);
  });
}

AbstractConstraint* AbstractConstraint::satisfy(int32_t mark,
                                                Planner* planner) {
  AbstractConstraint* overridden = nullptr;

  chooseMethod(mark);

  if (isSatisfied()) {
    inputsDo([mark](Variable* in) -> void { in->setMark(mark); });

    Variable* out = getOutput();
    overridden = out->getDeterminedBy();
    if (overridden != nullptr) {
      overridden->markUnsatisfied();
    }
    out->setDeterminedBy(this);
    if (!planner->addPropagate(this, mark)) {
      throw Error("Cycle encountered");
    }
    out->setMark(mark);
  } else {
    overridden = nullptr;
    if (_strength->sameAs(Strength::required())) {
      throw Error("Could not satisfy a required constraint");
    }
  }
  return overridden;
}
