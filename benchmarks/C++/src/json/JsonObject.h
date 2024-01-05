#ifndef JSONNUMBER
#define JSONNUMBER

#include "../som/Vector.cpp"
#include "JsonValue.h"
#include "../som/Error.cpp"
#include <string>
#include <memory>
#include <map>

using namespace std;

namespace json {
    class JsonObject : public JsonValue, public std::enable_shared_from_this<JsonObject> {
        private:
            shared_ptr<Vector<string>> _names;
            shared_ptr<Vector<shared_ptr<JsonValue>>> _values;
            map<string, int> _table;

            int indexOf(string name);

        public:
            JsonObject();

            void add(string name, shared_ptr<JsonValue> value);
            shared_ptr<JsonValue> get(string name);
            int size() const;
            bool isEmpty() const;
            bool isObject() override;
            shared_ptr<JsonObject> asObject() override;
    };
}

#endif //JSONNUMBER
