#ifndef CALLSIGN
#define CALLSIGN

#include <memory>
using namespace std;

namespace CD {
class CallSign {
 private:
  int _value;

 public:
  CallSign(int value);
  int compareTo(shared_ptr<CallSign> other) const;
};
};  // namespace CD

#endif  // CALLSIGN