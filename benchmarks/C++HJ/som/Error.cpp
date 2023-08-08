
#ifndef ERROR
#define ERROR

#include <string>

using namespace std;

class Error : virtual public exception {
    private:
        string _what;

    public:
        Error(string const &what) {
            _what = what;
        }
        
        Error(const char *what) {
            _what = string(what);
        }

        const char *what() const throw() {
            return (_what.c_str());
        }
};

#endif //ERROR