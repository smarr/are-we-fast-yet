#pragma once

#include <any>
#include <cmath>
#include <iostream>
#include <optional>

#include "benchmark.h"
#include "som/error.h"
#include "som/vector.h"

class Vector2D {
 private:
  static int32_t compareNumbers(double a, double b) {
    if (a == b) {
      return 0;
    }
    if (a < b) {
      return -1;
    }
    if (a > b) {
      return 1;
    }

    // We say that NaN is smaller than non-NaN.
    if (a == a) {
      return 1;
    }
    return -1;
  }

 public:
  const double x;
  const double y;

  constexpr Vector2D(double x, double y) : x(x), y(y) {}

  [[nodiscard]] Vector2D plus(const Vector2D& other) const {
    return {x + other.x, y + other.y};
  }

  [[nodiscard]] Vector2D minus(const Vector2D& other) const {
    return {x - other.x, y - other.y};
  }

  [[nodiscard]] int32_t compareTo(const Vector2D& other) const {
    const int32_t result = compareNumbers(x, other.x);

    if (result != 0) {
      return result;
    }

    return compareNumbers(y, other.y);
  }
};

class Vector3D {
 public:
  double x;
  double y;
  double z;

  constexpr Vector3D(double x, double y, double z) : x(x), y(y), z(z) {}
  constexpr Vector3D(const Vector3D& other) = default;

  Vector3D() = default;

  Vector3D& operator=(const Vector3D& other) = default;

  [[nodiscard]] Vector3D plus(const Vector3D& other) const {
    return {x + other.x, y + other.y, z + other.z};
  }

  [[nodiscard]] Vector3D minus(const Vector3D& other) const {
    return {x - other.x, y - other.y, z - other.z};
  }

  [[nodiscard]] double dot(const Vector3D& other) const {
    return x * other.x + y * other.y + z * other.z;
  }

  [[nodiscard]] double squaredMagnitude() const { return dot(*this); }
  [[nodiscard]] double magnitude() const { return sqrt(squaredMagnitude()); }

  [[nodiscard]] Vector3D times(double amount) const {
    return {x * amount, y * amount, z * amount};
  }
};

template <typename K, typename V>
class RedBlackTree {
 private:
  enum Color { RED = 0, BLACK = 1 };

  class Node {
    friend class RedBlackTree<K, V>;

   private:
    K _key;
    V _value;
    Node* _left{nullptr};
    Node* _right{nullptr};
    Node* _parent{nullptr};
    Color _color{RED};

    Node* successor() {
      Node* x = this;
      if (x->_right != nullptr) {
        return treeMinimum(x->_right);
      }
      Node* y = x->_parent;
      while (y != nullptr && x == y->_right) {
        x = y;
        y = y->_parent;
      }
      return y;
    }

    Node(K key, V value) : _key(key), _value(value) {}
    ~Node() {
      delete _left;
      delete _right;
    }
  };

  class InsertResult {
   public:
    const bool isNewEntry;
    Node* const newNode;
    V oldValue;

    InsertResult(bool isNewEntry, Node* newNode, V oldValue)
        : isNewEntry(isNewEntry), newNode(newNode), oldValue(oldValue) {}
  };

  Node* _root{nullptr};

  static Node* treeMinimum(Node* x) {
    Node* current = x;
    while (current->_left != nullptr) {
      current = current->_left;
    }
    return current;
  }

  InsertResult treeInsert(K key, V value) {
    Node* y = nullptr;
    Node* x = _root;

    while (x != nullptr) {
      y = x;
      const int32_t comparisonResult = key.compareTo(x->_key);
      if (comparisonResult < 0) {
        x = x->_left;
      } else if (comparisonResult > 0) {
        x = x->_right;
      } else {
        V oldValue = x->_value;
        x->_value = value;
        return InsertResult(false, nullptr, oldValue);
      }
    }

    Node* z = new Node(key, value);
    z->_parent = y;
    if (y == nullptr) {
      _root = z;
    } else {
      if (key.compareTo(y->_key) < 0) {
        y->_left = z;
      } else {
        y->_right = z;
      }
    }
    return InsertResult(true, z, V());
  }

  Node* leftRotate(Node* x) {
    Node* y = x->_right;

    // Turn y's left subtree into x's right subtree.
    x->_right = y->_left;
    if (y->_left != nullptr) {
      y->_left->_parent = x;
    }

    // Link x's parent to y
    y->_parent = x->_parent;
    if (x->_parent == nullptr) {
      _root = y;
    } else {
      if (x == x->_parent->_left) {
        x->_parent->_left = y;
      } else {
        x->_parent->_right = y;
      }
    }

    // Put x on y's left.
    y->_left = x;
    x->_parent = y;

    return y;
  }

  Node* rightRotate(Node* y) {
    Node* x = y->_left;

    // Turn x's right subtree into y's left subtree.
    y->_left = x->_right;
    if (x->_right != nullptr) {
      x->_right->_parent = y;
    }

    // Link y's parent to x;
    x->_parent = y->_parent;
    if (y->_parent == nullptr) {
      _root = x;
    } else {
      if (y == y->_parent->_left) {
        y->_parent->_left = x;
      } else {
        y->_parent->_right = x;
      }
    }

    x->_right = y;
    y->_parent = x;

    return x;
  }

  void removeFixup(Node* x, Node* xParent) {
    while (x != _root && (x == nullptr || x->_color == BLACK)) {
      if (x == xParent->_left) {
        // Note: the text points out that w cannot be null-> The reason is not
        // obvious from simply looking at the code; it comes about from the
        // properties of the red-black tree.
        Node* w = xParent->_right;
        if (w->_color == RED) {
          // Case 1
          w->_color = BLACK;
          xParent->_color = RED;
          leftRotate(xParent);
          w = xParent->_right;
        }
        if ((w->_left == nullptr || w->_left->_color == BLACK) &&
            (w->_right == nullptr || w->_right->_color == BLACK)) {
          // Case 2
          w->_color = RED;
          x = xParent;
          xParent = x->_parent;
        } else {
          if (w->_right == nullptr || w->_right->_color == BLACK) {
            // Case 3
            w->_left->_color = BLACK;
            w->_color = RED;
            rightRotate(w);
            w = xParent->_right;
          }
          // Case 4
          w->_color = xParent->_color;
          xParent->_color = BLACK;
          if (w->_right != nullptr) {
            w->_right->_color = BLACK;
          }
          leftRotate(xParent);
          x = _root;
          xParent = x->_parent;
        }
      } else {
        // Same as "then" clause with "right" and "left" exchanged.
        Node* w = xParent->_left;
        if (w->_color == RED) {
          // Case 1
          w->_color = BLACK;
          xParent->_color = RED;
          rightRotate(xParent);
          w = xParent->_left;
        }
        if ((w->_right == nullptr || w->_right->_color == BLACK) &&
            (w->_left == nullptr || w->_left->_color == BLACK)) {
          // Case 2
          w->_color = RED;
          x = xParent;
          xParent = x->_parent;
        } else {
          if (w->_left == nullptr || w->_left->_color == BLACK) {
            // Case 3
            w->_right->_color = BLACK;
            w->_color = RED;
            leftRotate(w);
            w = xParent->_left;
          }
          // Case 4
          w->_color = xParent->_color;
          xParent->_color = BLACK;
          if (w->_left != nullptr) {
            w->_left->_color = BLACK;
          }
          rightRotate(xParent);
          x = _root;
          xParent = x->_parent;
        }
      }
    }
    if (x != nullptr) {
      x->_color = BLACK;
    }
  }

 public:
  class Entry {
   public:
    K const key;
    V const value;

    Entry(K key, V value) : key(key), value(value) {}
  };

  RedBlackTree() = default;
  ~RedBlackTree() { delete _root; }

  std::optional<V> put(K key, V value) {
    InsertResult insertionResult = treeInsert(key, value);

    if (!insertionResult.isNewEntry) {
      return insertionResult.oldValue;
    }
    Node* x = insertionResult.newNode;

    while (x != _root && x->_parent->_color == RED) {
      if (x->_parent == x->_parent->_parent->_left) {
        Node* y = x->_parent->_parent->_right;
        if (y != nullptr && y->_color == RED) {
          // Case 1
          x->_parent->_color = BLACK;
          y->_color = BLACK;
          x->_parent->_parent->_color = RED;
          x = x->_parent->_parent;
        } else {
          if (x == x->_parent->_right) {
            // Case 2
            x = x->_parent;
            leftRotate(x);
          }
          // Case 3
          x->_parent->_color = BLACK;
          x->_parent->_parent->_color = RED;
          rightRotate(x->_parent->_parent);
        }
      } else {
        // Same as "then" clause with "right" and "left" exchanged.
        Node* y = x->_parent->_parent->_left;
        if (y != nullptr && y->_color == RED) {
          // Case 1
          x->_parent->_color = BLACK;
          y->_color = BLACK;
          x->_parent->_parent->_color = RED;
          x = x->_parent->_parent;
        } else {
          if (x == x->_parent->_left) {
            // Case 2
            x = x->_parent;
            rightRotate(x);
          }
          // Case 3
          x->_parent->_color = BLACK;
          x->_parent->_parent->_color = RED;
          leftRotate(x->_parent->_parent);
        }
      }
    }
    _root->_color = BLACK;
    return {};
  }

  std::optional<V> remove(K key) {
    Node* z = findNode(key);
    if (z == nullptr) {
      return {};
    }

    // Y is the node to be unlinked from the tree.
    Node* y = nullptr;
    if (z->_left == nullptr || z->_right == nullptr) {
      y = z;
    } else {
      y = z->successor();
    }

    // Y is guaranteed to be non-null at this point.
    Node* x = nullptr;
    if (y->_left != nullptr) {
      x = y->_left;
    } else {
      x = y->_right;
    }
    // X is the child of y which might potentially replace y in the tree. X
    // might be null at this point.
    Node* xParent = nullptr;
    if (x != nullptr) {
      x->_parent = y->_parent;
      xParent = x->_parent;
    } else {
      xParent = y->_parent;
    }
    if (y->_parent == nullptr) {
      _root = x;
    } else {
      if (y == y->_parent->_left) {
        y->_parent->_left = x;
      } else {
        y->_parent->_right = x;
      }
    }

    if (y != z) {
      if (y->_color == BLACK) {
        removeFixup(x, xParent);
      }

      y->_parent = z->_parent;
      y->_color = z->_color;
      y->_left = z->_left;
      y->_right = z->_right;

      if (z->_left != nullptr) {
        z->_left->_parent = y;
      }
      if (z->_right != nullptr) {
        z->_right->_parent = y;
      }
      if (z->_parent != nullptr) {
        if (z->_parent->_left == z) {
          z->_parent->_left = y;
        } else {
          z->_parent->_right = y;
        }
      } else {
        _root = y;
      }
    } else if (y->_color == BLACK) {
      removeFixup(x, xParent);
    }

    return z->_value;
  }

  V* get(K key) {
    Node* node = findNode(key);
    if (node == nullptr) {
      return nullptr;
    }
    return &node->_value;
  }

  void forEach(std::function<void(Entry&)> fn) {
    if (_root == nullptr) {
      return;
    }
    Node* current = treeMinimum(_root);
    while (current != nullptr) {
      Entry entry{current->_key, current->_value};
      fn(entry);
      current = current->successor();
    }
  }

  void destroyValues() {
    if (_root == nullptr) {
      return;
    }
    Node* current = treeMinimum(_root);
    while (current != nullptr) {
      Entry entry{current->_key, current->_value};
      delete entry.value;
      current = current->successor();
    }
  }

  Node* findNode(K key) {
    Node* current = _root;
    while (current != nullptr) {
      const int32_t comparisonResult = key.compareTo(current->_key);
      if (comparisonResult == 0) {
        return current;
      }
      if (comparisonResult < 0) {
        current = current->_left;
      } else {
        current = current->_right;
      }
    }
    return nullptr;
  }
};

class CallSign {
 private:
  int32_t _value;

 public:
  explicit CallSign(int32_t value) : _value(value) {}
  CallSign(const CallSign& other) = default;

  CallSign() = default;

  CallSign& operator=(const CallSign& other) = default;

  [[nodiscard]] int32_t compareTo(const CallSign& other) const {
    return (_value == other._value) ? 0 : ((_value < other._value) ? -1 : 1);
  }
};

class Collision {
 public:
  CallSign aircraftA;
  CallSign aircraftB;
  Vector3D position;

  Collision(const CallSign& aircraftA,
            const CallSign& aircraftB,
            const Vector3D& position)
      : aircraftA(aircraftA), aircraftB(aircraftB), position(position) {}

  explicit Collision() = default;
};

class Constants {
 public:
  constexpr static const double MIN_X = 0.0;
  constexpr static const double MIN_Y = 0.0;
  constexpr static const double MAX_X = 1000.0;
  constexpr static const double MAX_Y = 1000.0;
  constexpr static const double MIN_Z = 0.0;
  constexpr static const double MAX_Z = 10.0;
  constexpr static const double PROXIMITY_RADIUS = 1.0;
  constexpr static const double GOOD_VOXEL_SIZE = PROXIMITY_RADIUS * 2.0;
};

class Motion {
 private:
  [[nodiscard]] Vector3D delta() const { return posTwo.minus(posOne); }

 public:
  CallSign callsign;
  Vector3D posOne;
  Vector3D posTwo;

  Motion(const CallSign& callsign,
         const Vector3D& posOne,
         const Vector3D& posTwo)
      : callsign(callsign), posOne(posOne), posTwo(posTwo) {}

  explicit Motion() = default;
  Motion(const Motion& other) = default;
  Motion& operator=(const Motion& other) = default;

  [[nodiscard]] std::optional<Vector3D> findIntersection(
      const Motion& other) const {
    const Vector3D& init1 = posOne;
    const Vector3D& init2 = other.posOne;
    const Vector3D vec1 = delta();
    const Vector3D vec2 = other.delta();
    const double radius = Constants::PROXIMITY_RADIUS;

    // this test is not geometrical 3-d intersection test, it takes the fact
    // that the aircraft move into account ; so it is more like a 4d test (it
    // assumes that both of the aircraft have a constant speed over the tested
    // interval)

    // we thus have two points, each of them moving on its line segment at
    // constant speed ; we are looking for times when the distance between these
    // two points is smaller than r

    // vec1 is vector of aircraft 1
    // vec2 is vector of aircraft 2

    // a = (V2 - V1)^T * (V2 - V1)
    const double a = vec2.minus(vec1).squaredMagnitude();

    if (a != 0.0) {
      // we are first looking for instances of time when the planes are exactly
      // r from each other at least one plane is moving ; if the planes are
      // moving in parallel, they do not have constant speed

      // if the planes are moving in parallel, then
      //   if the faster starts behind the slower, we can have 2, 1, or 0
      //   solutions if the faster plane starts in front of the slower, we can
      //   have 0 or 1 solutions

      // if the planes are not moving in parallel, then

      // point P1 = I1 + vV1
      // point P2 = I2 + vV2
      //   - looking for v, such that dist(P1,P2) = || P1 - P2 || = r

      // it follows that || P1 - P2 || = sqrt( < P1-P2, P1-P2 > )
      //   0 = -r^2 + < P1 - P2, P1 - P2 >
      //  from properties of dot product
      //   0 = -r^2 + <I1-I2,I1-I2> + v * 2<I1-I2, V1-V2> + v^2 *<V1-V2,V1-V2>
      //   so we calculate a, b, c - and solve the quadratic equation
      //   0 = c + bv + av^2

      // b = 2 * <I1-I2, V1-V2>
      const double b = 2.0 * init1.minus(init2).dot(vec1.minus(vec2));

      // c = -r^2 + (I2 - I1)^T * (I2 - I1)
      const double c = -radius * radius + init2.minus(init1).squaredMagnitude();

      const double discr = b * b - 4.0 * a * c;
      if (discr < 0.0) {
        return {};
      }

      const double v1 = (-b - sqrt(discr)) / (2.0 * a);
      const double v2 = (-b + sqrt(discr)) / (2.0 * a);

      if (v1 <= v2 && ((v1 <= 1.0 && 1.0 <= v2) || (v1 <= 0.0 && 0.0 <= v2) ||
                       (0.0 <= v1 && v2 <= 1.0))) {
        // Pick a good "time" at which to report the collision.
        double v = 0.0;
        if (v1 <= 0.0) {
          // The collision started before this frame. Report it at the start of
          // the frame.
          v = 0.0;
        } else {
          // The collision started during this frame. Report it at that moment.
          v = v1;
        }

        const Vector3D result1 = init1.plus(vec1.times(v));
        const Vector3D result2 = init2.plus(vec2.times(v));

        const Vector3D result = result1.plus(result2).times(0.5);
        if (result.x >= Constants::MIN_X && result.x <= Constants::MAX_X &&
            result.y >= Constants::MIN_Y && result.y <= Constants::MAX_Y &&
            result.z >= Constants::MIN_Z && result.z <= Constants::MAX_Z) {
          return result;
        }
      }

      return {};
    }
    // the planes have the same speeds and are moving in parallel (or they are
    // not moving at all) they  thus have the same distance all the time ; we
    // calculate it from the initial point

    // dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
    const double dist = init2.minus(init1).magnitude();
    if (dist <= radius) {
      return init1.plus(init2).times(0.5);
    }

    return {};
  }
};

class Aircraft {
 public:
  CallSign callsign;
  Vector3D position;

  Aircraft(const CallSign& callsign, const Vector3D& position)
      : callsign(callsign), position(position) {}

  explicit Aircraft() = default;
};

class CollisionDetector {
 private:
  RedBlackTree<CallSign, Vector3D> _state{};
  constexpr static const Vector2D _horizontal{Constants::GOOD_VOXEL_SIZE, 0.0};
  constexpr static const Vector2D _vertical{0.0, Constants::GOOD_VOXEL_SIZE};

  static bool isInVoxel(const Vector2D& voxel, const Motion& motion) {
    if (voxel.x > Constants::MAX_X || voxel.x < Constants::MIN_X ||
        voxel.y > Constants::MAX_Y || voxel.y < Constants::MIN_Y) {
      return false;
    }

    const Vector3D& init = motion.posOne;
    const Vector3D& fin = motion.posTwo;

    const double v_s = Constants::GOOD_VOXEL_SIZE;
    const double r = Constants::PROXIMITY_RADIUS / 2.0;

    const double v_x = voxel.x;
    const double x0 = init.x;
    const double xv = fin.x - init.x;

    const double v_y = voxel.y;
    const double y0 = init.y;
    const double yv = fin.y - init.y;

    double low_x = (v_x - r - x0) / xv;
    double high_x = (v_x + v_s + r - x0) / xv;

    if (xv < 0.0) {
      const double tmp = low_x;
      low_x = high_x;
      high_x = tmp;
    }

    double low_y = (v_y - r - y0) / yv;
    double high_y = (v_y + v_s + r - y0) / yv;

    if (yv < 0.0) {
      const double tmp = low_y;
      low_y = high_y;
      high_y = tmp;
    }

    return (
        ((xv == 0.0 && v_x <= x0 + r &&
          x0 - r <= v_x + v_s) /* no motion in x */
         || (low_x <= 1.0 && 1.0 <= high_x) ||
         (low_x <= 0.0 && 0.0 <= high_x) || (0.0 <= low_x && high_x <= 1.0)) &&
        ((yv == 0.0 && v_y <= y0 + r &&
          y0 - r <= v_y + v_s) /* no motion in y */
         ||
         ((low_y <= 1.0 && 1.0 <= high_y) || (low_y <= 0.0 && 0.0 <= high_y) ||
          (0.0 <= low_y && high_y <= 1.0))) &&
        (xv == 0.0 || yv == 0.0 || /* no motion in x or y or both */
         (low_y <= high_x && high_x <= high_y) ||
         (low_y <= low_x && low_x <= high_y) ||
         (low_x <= low_y && high_y <= high_x)));
  }

  static void putIntoMap(RedBlackTree<Vector2D, Vector<Motion>*>& voxelMap,
                         const Vector2D& voxel,
                         const Motion& motion) {
    Vector<Motion>** array = voxelMap.get(voxel);
    if (array == nullptr) {
      auto* arr = new Vector<Motion>();
      arr->append(motion);
      voxelMap.put(voxel, arr);
    } else {
      (*array)->append(motion);
    }
  }

  void recurse(RedBlackTree<Vector2D, Vector<Motion>*>& voxelMap,
               RedBlackTree<Vector2D, bool>& seen,
               const Vector2D& nextVoxel,
               const Motion& motion) {
    if (!isInVoxel(nextVoxel, motion)) {
      return;
    }
    if (seen.put(nextVoxel, true) == true) {
      return;
    }
    putIntoMap(voxelMap, nextVoxel, motion);

    recurse(voxelMap, seen, nextVoxel.minus(_horizontal), motion);
    recurse(voxelMap, seen, nextVoxel.plus(_horizontal), motion);
    recurse(voxelMap, seen, nextVoxel.minus(_vertical), motion);
    recurse(voxelMap, seen, nextVoxel.plus(_vertical), motion);
    recurse(voxelMap, seen, nextVoxel.minus(_horizontal).minus(_vertical),
            motion);
    recurse(voxelMap, seen, nextVoxel.minus(_horizontal).plus(_vertical),
            motion);
    recurse(voxelMap, seen, nextVoxel.plus(_horizontal).minus(_vertical),
            motion);
    recurse(voxelMap, seen, nextVoxel.plus(_horizontal).plus(_vertical),
            motion);
  }

  Vector<Vector<Motion>*>* reduceCollisionSet(Vector<Motion>& motions) {
    RedBlackTree<Vector2D, Vector<Motion>*> voxelMap{};
    motions.forEach([this, &voxelMap](const Motion& motion) -> void {
      drawMotionOnVoxelMap(voxelMap, motion);
    });

    auto* result = new Vector<Vector<Motion>*>();

    voxelMap.forEach(
        [result](RedBlackTree<Vector2D, Vector<Motion>*>::Entry& e) -> void {
          if (e.value->size() > 1) {
            result->append(e.value);
          } else {
            delete e.value;
          }
        });
    return result;
  }

  Vector2D voxelHash(Vector3D position) {
    const auto xDiv =
        static_cast<int32_t>(position.x / Constants::GOOD_VOXEL_SIZE);
    const auto yDiv =
        static_cast<int32_t>(position.y / Constants::GOOD_VOXEL_SIZE);

    double x = Constants::GOOD_VOXEL_SIZE * xDiv;
    double y = Constants::GOOD_VOXEL_SIZE * yDiv;

    if (position.x < 0) {
      x -= Constants::GOOD_VOXEL_SIZE;
    }
    if (position.y < 0) {
      y -= Constants::GOOD_VOXEL_SIZE;
    }

    return {x, y};
  }

  void drawMotionOnVoxelMap(RedBlackTree<Vector2D, Vector<Motion>*>& voxelMap,
                            const Motion motion) {
    RedBlackTree<Vector2D, bool> seen{};
    recurse(voxelMap, seen, voxelHash(motion.posOne), motion);
  }

 public:
  CollisionDetector() = default;

  Vector<Collision>* handleNewFrame(Vector<Aircraft>& frame) {
    Vector<Motion> motions{};
    RedBlackTree<CallSign, bool> seen{};

    frame.forEach([this, &seen, &motions](const Aircraft& aircraft) -> void {
      std::optional<Vector3D> oldPosition =
          _state.put(aircraft.callsign, aircraft.position);
      const Vector3D newPosition = aircraft.position;
      seen.put(aircraft.callsign, true);
      if (!oldPosition.has_value()) {
        // Treat newly introduced aircraft as if they were stationary.
        oldPosition = newPosition;
      }
      motions.append(Motion(aircraft.callsign, *oldPosition, newPosition));
    });

    Vector<CallSign> toRemove{};
    _state.forEach(
        [&seen,
         &toRemove](const RedBlackTree<CallSign, Vector3D>::Entry& e) -> void {
          bool* const wasSeen = seen.get(e.key);
          if (wasSeen == nullptr || !*wasSeen) {
            toRemove.append(e.key);
          }
        });

    toRemove.forEach([this](const CallSign& e) -> void { _state.remove(e); });

    Vector<Vector<Motion>*>* allReduced = reduceCollisionSet(motions);
    auto* collisions = new Vector<Collision>();

    allReduced->forEach(
        [collisions](const Vector<Motion>* const& reduced) -> void {
          for (size_t i = 0; i < reduced->size(); i += 1) {
            const Motion* motion1 = reduced->at(i);
            for (size_t j = i + 1; j < reduced->size(); j += 1) {
              const Motion* motion2 = reduced->at(j);
              std::optional<Vector3D> collision =
                  motion1->findIntersection(*motion2);
              if (collision.has_value()) {
                collisions->append(Collision(motion1->callsign,
                                             motion2->callsign, *collision));
              }
            }
          }
        });
    allReduced->destroyValues();
    delete allReduced;
    return collisions;
  }
};

class Simulator {
 private:
  Vector<CallSign> _aircraft{};

 public:
  explicit Simulator(int32_t numAircraft) {
    for (int32_t i = 0; i < numAircraft; i += 1) {
      _aircraft.append(CallSign(i));
    }
  }

  Vector<Aircraft> simulate(double time) {
    Vector<Aircraft> frame{};

    for (size_t i = 0; i < _aircraft.size(); i += 2) {
      frame.append(Aircraft(
          *_aircraft.at(i),
          Vector3D(time, cos(time) * 2 + static_cast<double>(i) * 3, 10)));
      frame.append(Aircraft(
          *_aircraft.at(i + 1),
          Vector3D(time, sin(time) * 2 + static_cast<double>(i) * 3, 10)));
    }
    return frame;
  }
};

class CD : public Benchmark {
 private:
  static size_t benchmark(int32_t numAircrafts) {
    const int32_t numFrames = 200;

    Simulator simulator{numAircrafts};
    CollisionDetector detector{};
    size_t actualCollisions = 0;

    for (int32_t i = 0; i < numFrames; i += 1) {
      const double time = i / 10.0;
      Vector<Aircraft> frame = simulator.simulate(time);
      Vector<Collision>* collisions = detector.handleNewFrame(frame);
      actualCollisions += collisions->size();
      delete collisions;
    }

    return actualCollisions;
  }

 public:
  bool inner_benchmark_loop(int32_t innerIterations) override {
    return verify_result(benchmark(innerIterations), innerIterations);
  }

  static bool verify_result(size_t actualCollisions, int32_t numAircrafts) {
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

    std::cout << "No verification result for " << numAircrafts << " found\n";
    std::cout << "Result is: " << actualCollisions << "\n";
    return false;
  }

  std::any benchmark() override { throw Error("Should never be reached"); }
  bool verify_result(std::any) override {
    throw Error("Should never be reached");
  }
};
