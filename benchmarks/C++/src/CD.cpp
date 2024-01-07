#include "CD.h"

namespace CD {
int CD::benchmark(int numAircrafts) {
  int numFrames = 200;

  shared_ptr<Simulator> simulator = make_shared<Simulator>(numAircrafts);
  shared_ptr<CollisionDetector> detector = make_shared<CollisionDetector>();
  int actualCollisions = 0;

  for (int i = 0; i < numFrames; i++) {
    double time = i / 10.0;
    shared_ptr<Vector<shared_ptr<Collision>>> collisions =
        detector->handleNewFrame(simulator->simulate(time));
    actualCollisions += collisions->size();
  }

  return actualCollisions;
}

bool CD::innerBenchmarkLoop(int innerIterations) {
  return verifyResult(benchmark(innerIterations), innerIterations);
}

bool CD::verifyResult(int actualCollisions, int numAircrafts) {
  if (numAircrafts == 1000) {
    return actualCollisions == 14484;
  }
  if (numAircrafts == 500) {
    return actualCollisions == 14484;
  }
  if (numAircrafts == 250) {
    return actualCollisions == 10830;
  }
  if (numAircrafts == 200) {
    return actualCollisions == 8655;
  }
  if (numAircrafts == 100) {
    return actualCollisions == 4305;
  }
  if (numAircrafts == 10) {
    return actualCollisions == 390;
  }
  if (numAircrafts == 2) {
    return actualCollisions == 42;
  }

  std::cout << "No verification result for " << numAircrafts << " found"
            << std::endl;
  std::cout << "Result is: " << actualCollisions << std::endl;
  return false;
}

std::any CD::benchmark() {
  throw Error("Should never be reached");
}

bool CD::verifyResult(std::any result) {
  (void)result;
  throw Error("Should never be reached");
}
};  // namespace CD