#include "Strength.h"

using namespace std;

namespace deltablue {

    shared_ptr<IdentityDictionary<int>> Strength::_strengthTable;
    shared_ptr<IdentityDictionary<shared_ptr<Strength>>> Strength::_strengthConstant;
    shared_ptr<Strength> Strength::_absoluteWeakest;
    shared_ptr<Strength> Strength::_required;

    // Set static member variable
    shared_ptr<Strength::Sym> Strength::ABSOLUTE_STRONGEST = make_shared<Sym>(0);
    shared_ptr<Strength::Sym> Strength::REQUIRED           = make_shared<Sym>(1);
    shared_ptr<Strength::Sym> Strength::STRONG_PREFERRED   = make_shared<Sym>(2);
    shared_ptr<Strength::Sym> Strength::PREFERRED          = make_shared<Sym>(3);
    shared_ptr<Strength::Sym> Strength::STRONG_DEFAULT     = make_shared<Sym>(4);
    shared_ptr<Strength::Sym> Strength::DEFAULT            = make_shared<Sym>(5);
    shared_ptr<Strength::Sym> Strength::WEAK_DEFAULT       = make_shared<Sym>(6);
    shared_ptr<Strength::Sym> Strength::ABSOLUTE_WEAKEST   = make_shared<Sym>(7);

    Strength::Sym::Sym(int hash) {
        _hash = hash;
    }

    int Strength::Sym::customHash() {
          return _hash;
    }
        
    Strength::Strength(shared_ptr<Sym> symbolicValue) {
        _symbolicValue = symbolicValue;
        _arithmeticValue = _strengthTable->at(symbolicValue);
    }

        
    bool Strength::sameAs(shared_ptr<Strength> s) const {
        return _arithmeticValue == s->_arithmeticValue;
    }

    bool Strength::stronger(shared_ptr<Strength> s) const {
        return _arithmeticValue < s->_arithmeticValue;
    }

    bool Strength::weaker(shared_ptr<Strength> s) const {
        return _arithmeticValue > s->_arithmeticValue;
    }

    shared_ptr<Strength> Strength::strongest(shared_ptr<Strength> s) {
        return s->stronger(shared_from_this()) ? s : shared_from_this();
    }

    shared_ptr<Strength> Strength::weakest(shared_ptr<Strength> s) {
        return s->weaker(shared_from_this()) ? s : shared_from_this();
    }

    int Strength::get_arithmeticValue() const {
        return _arithmeticValue;
    }

    shared_ptr<Strength> Strength::of(shared_ptr<Sym> strength) {
        return _strengthConstant->atPtr(strength);
    }

    shared_ptr<Strength> Strength::absoluteWeakest() {
        return _absoluteWeakest;
    }

    shared_ptr<Strength> Strength::required() {
        return _required;
    }

    shared_ptr<IdentityDictionary<int>> Strength::createStrengthTable() {
        shared_ptr<IdentityDictionary<int>> strengthTable = make_shared<IdentityDictionary<int>>();
        strengthTable->atPut(Strength::ABSOLUTE_STRONGEST, -10000);
        strengthTable->atPut(Strength::REQUIRED,           -800);
        strengthTable->atPut(Strength::STRONG_PREFERRED,   -600);
        strengthTable->atPut(Strength::PREFERRED,          -400);
        strengthTable->atPut(Strength::STRONG_DEFAULT,     -200);
        strengthTable->atPut(Strength::DEFAULT,             0);
        strengthTable->atPut(Strength::WEAK_DEFAULT,        500);
        strengthTable->atPut(Strength::ABSOLUTE_WEAKEST,    10000);
        return strengthTable;
    }


    shared_ptr<IdentityDictionary<shared_ptr<Strength>>> Strength::createStrengthConstants() {
        shared_ptr<IdentityDictionary<shared_ptr<Strength>>> strengthConstant = make_shared<IdentityDictionary<shared_ptr<Strength>>>();
        _strengthTable->getKeys()->forEach([&](shared_ptr<CustomHash> key) -> void {
            shared_ptr<Sym> keySym = dynamic_pointer_cast<Sym>(key);
            strengthConstant->atPut(keySym, make_shared<Strength>(keySym));
        });

        return strengthConstant;
    }

    void Strength::initializeConstants() {
        _strengthTable = createStrengthTable();
        _strengthConstant = createStrengthConstants();
        _absoluteWeakest = of(Strength::ABSOLUTE_WEAKEST);
        _required = of(Strength::REQUIRED);
    }

}
