#ifndef VARIABLE
#define VARIABLE

#include "Strength.h"
#include "../som/Vector.cpp"
#include <memory> 

using namespace std;

namespace deltablue {
    
    class AbstractConstraint;

    class Variable {
        private:
            
            int _value;                         
            shared_ptr<Vector<shared_ptr<AbstractConstraint>>> _constraints; 
            shared_ptr<AbstractConstraint> _determinedBy;
            int _mark;
            shared_ptr<Strength> _walkStrength;
            bool _stay; 

        public:
            Variable();

            static shared_ptr<Variable> value(int aValue);
            void addConstraint(shared_ptr<AbstractConstraint> c);
            shared_ptr<Vector<shared_ptr<AbstractConstraint>>> getConstraints();
            shared_ptr<AbstractConstraint> getDeterminedBy() const;
            void setDeterminedBy(shared_ptr<AbstractConstraint> c);
            int getMark() const;
            void setMark(int markValue);
            void removeConstraint(shared_ptr<AbstractConstraint> c);
            bool getStay() const;
            void setStay(bool v);
            int getValue() const;
            void setValue(int value);
            shared_ptr<Strength> getWalkStrength();
            void setWalkStrength(shared_ptr<Strength> strength);
    };
}

#endif //VARIABLE