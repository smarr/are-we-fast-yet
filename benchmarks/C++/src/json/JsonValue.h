#ifndef JSONVALUE
#define JSONVALUE

#include <memory>
#include "../som/Error.cpp"

using namespace std;

namespace json {

    class JsonArray;
    class JsonObject;

    class JsonValue {
        public:
            JsonValue() = default;

            virtual bool isObject();
            virtual bool isArray();
            virtual bool isNumber();     
            virtual bool isString();
            virtual bool isBoolean();
            virtual bool isTrue();
            virtual bool isFalse();
            virtual bool isNull();
            virtual shared_ptr<JsonObject> asObject();
            virtual shared_ptr<JsonArray> asArray();
            virtual string toString();
    };
}

#endif //JSONVALUE