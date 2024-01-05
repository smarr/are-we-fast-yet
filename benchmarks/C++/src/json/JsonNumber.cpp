#include "JsonNumber.h"

using namespace std;

namespace json {
    JsonNumber::JsonNumber(string string) {
        _string = string;
        if (string.empty()) {
            throw Error("value is null");
        }
    }

    string JsonNumber::toString() {
        return _string;
    }

    bool JsonNumber::isNumber() {
        return true;
    }
}