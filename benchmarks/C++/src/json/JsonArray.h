#ifndef JSONARRAY
#define JSONARRAY

#include "../som/Vector.cpp"
#include "JsonValue.h"
#include <memory>
#include "../som/Error.cpp"

namespace json {
    class JsonArray : public JsonValue, public std::enable_shared_from_this<JsonArray> {
        private:
            shared_ptr<Vector<shared_ptr<JsonValue>>> _values;
        
        public:

            JsonArray();

            void add(shared_ptr<JsonValue> value);
            int size();
            shared_ptr<JsonValue> get(int index);
            bool isArray() override;
            shared_ptr<JsonArray> asArray() override;
    };
}

#endif //JSONARRAY