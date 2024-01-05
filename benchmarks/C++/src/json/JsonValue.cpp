#include "JsonValue.h"

namespace json {

    bool JsonValue::isObject() {
        return false;
    }

    
    bool JsonValue::isArray() {
        return false;
    }

    
    bool JsonValue::isNumber() {
        return false;
    }

    
    bool JsonValue::isString() {
        return false;
    }

    
    bool JsonValue::isBoolean() {
        return false;
    }

    
    bool JsonValue::isTrue() {
        return false;
    }

    
    bool JsonValue::isFalse() {
        return false;
    }

    
    bool JsonValue::isNull() {
        return false;
    }

    shared_ptr<JsonObject> JsonValue::asObject() {
        throw Error("Not an object: " + toString());
    }

    shared_ptr<JsonArray> JsonValue::asArray() {
        throw Error("Not an array: " + toString());
    }

    string JsonValue::toString() {
        return "";
    }
}
