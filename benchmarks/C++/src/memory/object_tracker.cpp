#include "object_tracker.h"

std::vector<TrackedObject*> ObjectTracker::trackedObjects{};

void ObjectTracker::releaseAll() {
  for (auto* obj : trackedObjects) {
    delete obj;
  }
  trackedObjects.clear();
}
