#ifndef PARSEEXCEPTION
#define PARSEEXCEPTION

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
            ParseException(string message, int offset, int line, int column);
            
            int getOffset();
            int getLine();
            int getColumn();
            const char *what() const throw();
    };
}

#endif //PARSEEXCEPTION