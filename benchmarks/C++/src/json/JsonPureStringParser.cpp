#include "JsonPureStringParser.h"

namespace json {

            shared_ptr<JsonValue> JsonPureStringParser::readValue() {
                if (_current == "n") {
                    return readNull();
                } else if (_current == "t") {
                    return readTrue();
                } else if (_current == "f") {
                    return readFalse();
                } else if (_current == "\"") {
                    return readString();
                } else if (_current == "[") {
                    return readArray();
                } else if (_current == "{") {
                    return readObject();
                }
                else if (_current == "-" || _current == "0" || _current == "1" || _current == "2" || _current == "3" || _current == "4"
                    || _current == "5" || _current == "6" || _current == "7" || _current == "8" || _current == "9") {
                    return readNumber();
                }
                else 
                    throw expected("value");
            }

            shared_ptr<JsonObject> JsonPureStringParser::readObject() {
                read();
                shared_ptr<JsonObject> object = make_shared<JsonObject>();
                skipWhiteSpace();
                if (readChar("}")) {
                    return object;
                }
                do {
                    skipWhiteSpace();
                    string name = readName();
                    skipWhiteSpace();
                    if (!readChar(":")) {
                        throw expected("':'");
                    }
                    skipWhiteSpace();

                    object->add(name, readValue());
                    skipWhiteSpace();
                } 
                while (readChar(","));
                if (!readChar("}")) {
                    throw expected("',' or '}'");
                }
                return object;
            }

            string JsonPureStringParser::readName() {
                if (_current != "\"") {
                    throw expected("name");
                }
                return readStringInternal();
            }

            shared_ptr<JsonArray> JsonPureStringParser::readArray() {
                read();
                shared_ptr<JsonArray> array = make_shared<JsonArray>();
                skipWhiteSpace();
                if (readChar("]")) {
                    return array;
                }
                do {
                    skipWhiteSpace();
                    array->add(readValue());
                    skipWhiteSpace();
                }  while (readChar(","));

                if (!readChar("]")) {
                        throw expected("',' or ']'");
                }
                return array;
            }

            shared_ptr<JsonValue> JsonPureStringParser::readNull() {
                read();
                readRequiredChar("u");
                readRequiredChar("l");
                readRequiredChar("l");
                return JsonLiteral::NNULL;
            }

            shared_ptr<JsonValue> JsonPureStringParser::readTrue() {
                read();
                readRequiredChar("r");
                readRequiredChar("u");
                readRequiredChar("e");
                return JsonLiteral::TRUE;
            }

            shared_ptr<JsonValue> JsonPureStringParser::readFalse() {
                read();
                readRequiredChar("a");
                readRequiredChar("l");
                readRequiredChar("s");
                readRequiredChar("e");
                return JsonLiteral::FALSE;
            }

            void JsonPureStringParser::readRequiredChar(string ch) {
                if (!readChar(ch)) {
                    throw expected("'" + ch + "'");
                }
            }

            shared_ptr<JsonValue> JsonPureStringParser::readString() {
                return make_shared<JsonString>(readStringInternal());
            }

            string JsonPureStringParser::readStringInternal() {
                read();
                startCapture();
                while (_current != "\"") {
                    if (_current == "\\") {
                        pauseCapture();
                        readEscape();
                        startCapture();
                    } else {
                        read();
                    }
                }
                string string = endCapture();
                read();
                return string;
            }

            void JsonPureStringParser::readEscape() {
                read();
                if (_current == "\"" || _current == "/" || _current == "\\") 
                    _captureBuffer += _current;
                else if (_current == "b")
                    _captureBuffer += "\b";
                else if (_current == "f")
                    _captureBuffer += "\f";
                else if (_current == "n")
                    _captureBuffer += "\n";
                else if (_current == "r")
                    _captureBuffer += "\r";
                else if (_current == "t")
                    _captureBuffer += "\t";
                else 
                    throw expected("valid escape sequence");
                read();
            }

            shared_ptr<JsonValue> JsonPureStringParser::readNumber() {
                startCapture();
                readChar("-");
                string firstDigit = _current;
                if (!readDigit()) {
                    throw expected("digit");
                }
                if (firstDigit != "0") {
                    while (readDigit()) { }
                }
                readFraction();
                readExponent();
                return make_shared<JsonNumber>(endCapture());
            }

            bool JsonPureStringParser::readFraction() {
                if (!readChar(".")) {
                    return false;
                }
                if (!readDigit()) {
                    throw expected("digit");
                }
                while (readDigit()) { }
                return true;
            }

            bool JsonPureStringParser::readExponent() {
                if (!readChar("e") && !readChar("E")) {
                    return false;
                }
                if (!readChar("+")) {
                    readChar("-");
                }
                if (!readDigit()) {
                    throw expected("digit");
                }

                while (readDigit()) { }
                return true;
            }

            bool JsonPureStringParser::readChar(string ch) {
                if (_current != ch) {
                    return false;
                }
                read();
                return true;
            }

            bool JsonPureStringParser::readDigit() {
                if (!isDigit()) {
                    return false;
                }
                read();
                return true;
            }

            void JsonPureStringParser::skipWhiteSpace() {
                while (isWhiteSpace()) {
                    read();
                }
            }

            void JsonPureStringParser::read() {
                if ("\n" == _current) {
                    _line++;
                    _column = 0;
                }
                _index++;
                if (_index < _input.length()) {
                    _current = _input.substr(_index, 1);
                } else {
                    _current = "";
                }
            }

            void JsonPureStringParser::startCapture() {
                _captureStart = _index;
            }

            void JsonPureStringParser::pauseCapture() {
                int _end = _current == "" ? _index : _index - 1;
                _captureBuffer += _input.substr(_captureStart, _end - _captureStart + 1);
                _captureStart = -1;
            }

            string JsonPureStringParser::endCapture() {
                int _end = _current == "" ? _index : _index - 1;
                string captured;
                if ("" == _captureBuffer) {
                    captured = _input.substr(_captureStart, _end - _captureStart + 1);
                } else {
                    _captureBuffer += _input.substr(_captureStart, _end - _captureStart + 1);
                    captured = _captureBuffer;
                    _captureBuffer = "";
                }
                _captureStart = -1;
                return captured;
            }

            ParseException JsonPureStringParser::expected(string expected) {
                if (isEndOfText()) {
                    return error("Unexpected end of input");
                }

                return error("Expected " + expected);
            }

            ParseException JsonPureStringParser::error(string message) {
                return ParseException(message, _index, _line, _column - 1);
            }

            bool JsonPureStringParser::isWhiteSpace() {
                return " " == _current || "\t" == _current || "\n" == _current || "\r" == _current;
            }


            bool JsonPureStringParser::isDigit() {
                return "0" == _current ||
                    "1" == _current ||
                    "2" == _current ||
                    "3" == _current ||
                    "4" == _current ||
                    "5" == _current ||
                    "6" == _current ||
                    "7" == _current ||
                    "8" == _current ||
                    "9" == _current;
            }

            bool JsonPureStringParser::isEndOfText() {
                return _current == "";
            }

            JsonPureStringParser::JsonPureStringParser(string string) {
                _input = string;
                _index = -1;
                _line = 1;
                _captureStart = -1;
                _column = 0;
                _current = "";
                _captureBuffer = "";
            }

            shared_ptr<JsonValue> JsonPureStringParser::parse() {
                read();
                skipWhiteSpace();
                shared_ptr<JsonValue> result = readValue();
                skipWhiteSpace();
                if (!isEndOfText()) {
                    throw Error("Unexpected character");
                }
                return result;
            }

}
