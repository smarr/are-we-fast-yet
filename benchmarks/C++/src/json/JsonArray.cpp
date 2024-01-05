#include "JsonArray.h"

namespace json {
    JsonArray::JsonArray() {
        _values = make_shared<Vector<shared_ptr<JsonValue>>>();
    }

    void JsonArray::add(shared_ptr<JsonValue> value) {
        if (value == nullptr) {
            throw Error("value is null");
        }
        _values->append(value);
    }

    int JsonArray::size() {
        return _values->size();
    }

    shared_ptr<JsonValue> JsonArray::get(int index) {
        return _values->at(index);
    }

    bool JsonArray::isArray() {
        return true;
    }

    shared_ptr<JsonArray> JsonArray::asArray() {
        return shared_from_this();
    }
}