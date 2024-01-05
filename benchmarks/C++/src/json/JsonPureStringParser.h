#ifndef JSONPURESTRINGPARSER
#define JSONPURESTRINGPARSER


#include <string>
#include <memory>
#include "../som/Error.cpp"
#include "JsonArray.h"
#include "JsonObject.h"
#include "ParseException.h"
#include "JsonLiteral.h"
#include "JsonNumber.h"
#include "JsonString.h"

using namespace std;

namespace json {
    class JsonPureStringParser {
        private:
            string _input;
            int _index;
            int _line;
            int _column;
            string _current;
            string _captureBuffer;
            int _captureStart;

            shared_ptr<JsonValue> readValue();
            shared_ptr<JsonObject> readObject();
            string readName();
            shared_ptr<JsonArray> readArray();
            shared_ptr<JsonValue> readNull();
            shared_ptr<JsonValue> readTrue();
            shared_ptr<JsonValue> readFalse();
            void readRequiredChar(string ch);
            shared_ptr<JsonValue> readString();
            string readStringInternal();
            void readEscape();
            shared_ptr<JsonValue> readNumber();
            bool readFraction();
            bool readExponent();
            bool readChar(string ch);
            bool readDigit();
            void skipWhiteSpace();
            void read();
            void startCapture();
            void pauseCapture();
            string endCapture();
            ParseException expected(string expected);
            ParseException error(string message);
            bool isWhiteSpace();
            bool isDigit();
            bool isEndOfText();

        public: 
            JsonPureStringParser(string string);

            shared_ptr<JsonValue> parse();

    };
}


#endif //JSONPURESTRINGPARSER