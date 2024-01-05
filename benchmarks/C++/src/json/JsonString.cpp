#include "JsonString.h"


namespace json {

    JsonString::JsonString(string string) {
        _string = string;
    }

    bool JsonString::isString() {
        return true;
    }
}
