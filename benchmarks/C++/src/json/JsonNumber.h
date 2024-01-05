#ifndef JSONUMBER
#define JSONUMBER

#include <string>
#include "../som/Error.cpp"
#include "JsonValue.h"

using namespace std;

namespace json {
    class JsonNumber : public JsonValue {
        private:
            string _string;

        public:
            JsonNumber(string string);

            string toString() override;
            bool isNumber() override;
    };
}

#endif //JSONUMBER