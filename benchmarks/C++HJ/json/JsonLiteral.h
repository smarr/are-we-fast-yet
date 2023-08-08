#include <memory>
#include <string>
#include "JsonValue.h"

using namespace std;

namespace json {
    class JsonLiteral: public JsonValue {
        private:
            string _value;
            bool _isNull;
            bool _isTrue;
            bool _isFalse;

        public:
            static shared_ptr<JsonLiteral> NNULL;
            static shared_ptr<JsonLiteral> TRUE;
            static shared_ptr<JsonLiteral> FALSE;

            JsonLiteral(string value);

            string toString() override;
            bool isNull() override;
            bool isTrue() override;
            bool isFalse() override;
            bool isBoolean() override;

    };
}
