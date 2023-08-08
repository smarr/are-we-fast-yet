#include "CallSign.h"

namespace CD {
    CallSign::CallSign(int value) {
        _value = value;
    }

    int CallSign::compareTo(shared_ptr<CallSign> other) const {
        return (_value == other->_value) ? 0 : ((_value < other->_value) ? -1 : 1);
    }
};