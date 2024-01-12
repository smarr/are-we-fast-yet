#include <string> 
#include "JsonValue.h"

using namespace std;

namespace json {
    class JsonString : public JsonValue {
        private: 
            string _string;
        public: 

            JsonString(string string) {
                _string = string;
            }

            bool isString() override {
                return true;
            }
    };
}
