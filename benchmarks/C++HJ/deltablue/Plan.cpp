#include "Plan.h"

using namespace std;

namespace deltablue {

    Plan::Plan() : Vector<shared_ptr<AbstractConstraint>>() {}

    void Plan::execute() {
        forEach([&](shared_ptr<AbstractConstraint> c) -> void {
                c->execute();
        });
    }
}