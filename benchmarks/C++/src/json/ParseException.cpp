#include "ParseException.h"

namespace json {
    ParseException::ParseException(string message, int offset, int line, int column) {
        _offset = offset;
        _line   = line;
        _column = column;
        _what = message + " at " + to_string(line) + ":" + to_string(column);
    }
    
    int ParseException::getOffset() {
        return _offset;
    }

    int ParseException::getLine() {
        return _line;
    }

    int ParseException::getColumn() {
        return _column;
    }

    const char *ParseException::what() const throw() {
        return (_what.c_str());
    }

}