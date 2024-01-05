#include "JsonObject.h"

namespace json {
    JsonObject::JsonObject() {
        _names  = make_shared<Vector<string>>();
        _values = make_shared<Vector<shared_ptr<JsonValue>>>();
    };

    int JsonObject::indexOf(string name) {
        int index = _table.find(name)->second;
        if (index != -1 && name == _names->at(index)) {
            return index;
        }
        throw new Error("NotImplemented");
    }


    void JsonObject::add(string name, shared_ptr<JsonValue> value) {
        if (name.empty()) {
            throw Error ("name is null");
        }
        if (value == nullptr) {
            throw Error ("value is null");
        }
        _table[name] = _names->size();
        _names->append(name);
        _values->append(value);
    }

    shared_ptr<JsonValue> JsonObject::get(string name) {
        if (name.empty()) {
            throw Error ("name is null");
        }
        int index = indexOf(name);
        return index == -1 ? nullptr : _values->at(index);
    }

    int JsonObject::size() const {
        return static_cast<int>(_names->size());
    }

    bool JsonObject::isEmpty() const {
        return _names->isEmpty();
    }

    bool JsonObject::isObject() {
        return true;
    }

    shared_ptr<JsonObject> JsonObject::asObject() {
        return shared_from_this();
    }
}