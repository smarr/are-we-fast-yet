#pragma once

#include <vector>

class TrackedObject;

class ObjectTracker {
 private:
  static std::vector<TrackedObject*> trackedObjects;

 public:
  static void track(TrackedObject* obj) { trackedObjects.push_back(obj); }

  static void releaseAll();
};

class TrackedObject {
 public:
  TrackedObject() { ObjectTracker::track(this); }
  virtual ~TrackedObject() = default;
};
