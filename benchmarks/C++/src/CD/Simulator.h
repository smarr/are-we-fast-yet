#ifndef SIMULATOR
#define SIMULATOR

#include <cmath>
#include "../som/Vector.cpp"
#include "Aircraft.h"

using namespace std;

namespace CD {
class Simulator {
 private:
  shared_ptr<Vector<shared_ptr<CallSign>>> _aircraft;

 public:
  Simulator(int numAircraft);

  shared_ptr<Vector<shared_ptr<Aircraft>>> simulate(double time);
};
};  // namespace CD

#endif  // SIMULATOR