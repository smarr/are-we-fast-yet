
#ifndef RANDOM
#define RANDOM

class Random {
    private:
        int _seed;

    public:
        Random() {
            _seed = 74755;
        }
        
        int next() {
            _seed = ((_seed * 1309) + 13849) & 65535;
            return _seed;
        }
};

#endif //RANDOM