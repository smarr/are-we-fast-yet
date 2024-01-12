#include <string>
#include "../som/Error.cpp"
#include "JsonValue.h"

using namespace std;

namespace json {
    class JsonNumber : public JsonValue {
        private:
            string _string;

        public:
            JsonNumber(string string) {
                _string = string;
                if (string.empty()) {
                    throw Error("value is null");
                }
            }

            string toString() override {
                return _string;
            }

            bool isNumber() override {
                return true;
            }
    };
}