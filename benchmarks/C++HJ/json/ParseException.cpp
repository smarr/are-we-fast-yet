#include <string>

using namespace std;

namespace json {
    class ParseException : virtual public exception {
        private:
            int _offset;
            int _line;
            int _column;
            string _what;

        public:
            ParseException(string message, int offset, int line, int column) {
                _offset = offset;
                _line   = line;
                _column = column;
                _what = message + " at " + to_string(line) + ":" + to_string(column);
            }
            
            int getOffset() {
                return _offset;
            }

            int getLine() {
                return _line;
            }

            int getColumn() {
                return _column;
            }

            const char *what() const throw() {
                return (_what.c_str());
            }
    };
}