#include <memory>
#include <functional>
#include <iostream>

using namespace std;

namespace CD {

    template <typename K, typename V>
    class RedBlackTree {

        private:

            enum Color {RED = 0, BLACK = 1};

            class Node : public enable_shared_from_this<Node>{

                public:
                    // Moved to public 
                    K _key;
                    V _value;
                    shared_ptr<Node> _left;
                    shared_ptr<Node> _right;
                    shared_ptr<Node> _parent;
                    Color _color;

                    shared_ptr<Node> successor(void) {
                        shared_ptr<Node> x = this->shared_from_this();
                        if (x->_right != nullptr) {
                            return treeMinimum(x->_right);
                        }
                        shared_ptr<Node> y = x->_parent;
                        while (y != nullptr && x == y->_right) {
                            x = y;
                            y = y->_parent;
                        }
                        return y;
                    }

                    // Moved to public 

                    Node(K key, V value) {
                        _key = key;
                        _value = value;
                        _left = nullptr;
                        _right = nullptr;
                        _left = nullptr;
                        _parent = nullptr;
                        _color = RED;
                    }
            };

            class InsertResult {
                public: 
                    bool _isNewEntry;
                    shared_ptr<Node> _newNode;
                    V _oldValue;

                    InsertResult(bool isNewEntry, shared_ptr<Node> newNode, V oldValue) {
                        _isNewEntry = isNewEntry;
                        _newNode = newNode;
                        _oldValue = oldValue;
                    }
            };

            shared_ptr<Node> _root;

            static shared_ptr<Node> treeMinimum(shared_ptr<Node> x) {
                shared_ptr<Node> current = x;
                while (current->_left != nullptr) {
                    current = current->_left;
                }
                return current;
            }

            shared_ptr<InsertResult> treeInsertPtr(K key, V value) {
                shared_ptr<Node> y = nullptr;
                shared_ptr<Node> x = _root;

                while (x != nullptr) {
                    y = x;

                    int comparisonResult = key->compareTo(x->_key);
                    if (comparisonResult < 0) {
                        x = x->_left;
                    } else if (comparisonResult > 0) {
                        x = x->_right;
                    } else {
                        V oldValue = x->_value;
                        x->_value = value;
                        return make_shared<InsertResult>(false, nullptr, oldValue);
                    }
                }

                shared_ptr<Node> z = make_shared<Node>(key, value);
                z->_parent = y;
                if (y == nullptr) {
                    _root = z;
                } else {
                    if (key->compareTo(y->_key) < 0) {
                        y->_left = z;
                    } else {
                        y->_right = z;
                    }
                }
                return make_shared<InsertResult>(true, z, nullptr);
            }

            shared_ptr<InsertResult> treeInsert(K key, V value) {
                shared_ptr<Node> y = nullptr;
                shared_ptr<Node> x = _root;

                while (x != nullptr) {
                    y = x;
                    int comparisonResult = key->compareTo(x->_key);
                    if (comparisonResult < 0) {
                        x = x->_left;
                    } else if (comparisonResult > 0) {
                        x = x->_right;
                    } else {
                        V oldValue = x->_value;
                        x->_value = value;
                        return make_shared<InsertResult>(false, nullptr, oldValue);
                    }
                }

                shared_ptr<Node> z = make_shared<Node>(key, value);
                z->_parent = y;
                if (y == nullptr) {
                    _root = z;
                } else {
                    if (key->compareTo(y->_key) < 0) {
                        y->_left = z;
                    } else {
                        y->_right = z;
                    }
                }
                return make_shared<InsertResult>(true, z, V());
            }

            shared_ptr<Node> leftRotate(shared_ptr<Node> x) {
                shared_ptr<Node> y = x->_right;

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

            shared_ptr<Node> rightRotate(shared_ptr<Node> y) {
                shared_ptr<Node> x = y->_left;

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

            void removeFixup(shared_ptr<Node> x, shared_ptr<Node> xParent) {
                while (x != _root && (x == nullptr || x->_color == BLACK)) {
                    if (x == xParent->_left) {
                        // Note: the text points out that w cannot be null-> The reason is not obvious from
                        // simply looking at the code; it comes about from the properties of the red-black
                        // tree.
                        shared_ptr<Node> w = xParent->_right;
                        if (w->_color == RED) {
                            // Case 1
                            w->_color = BLACK;
                            xParent->_color = RED;
                            leftRotate(xParent);
                            w = xParent->_right;
                        }
                        if ((w->_left == nullptr || w->_left->_color == BLACK)
                            && (w->_right == nullptr || w->_right->_color == BLACK)) {
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
                        shared_ptr<Node> w = xParent->_left;
                        if (w->_color == RED) {
                            // Case 1
                            w->_color = BLACK;
                            xParent->_color = RED;
                            rightRotate(xParent);
                            w = xParent->_left;
                        }
                        if ((w->_right == nullptr || w->_right->_color == BLACK)
                            && (w->_left == nullptr || w->_left->_color == BLACK)) {
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
            // moved public
            class Entry {
                public:
                    K _key;
                    V _value;

                    Entry(K key, V value) {
                        _key = key;
                        _value = value;
                    }
            };
            // moved public

            RedBlackTree() {
                _root = nullptr;
            };

            V putPtr(K key, V value) {
                shared_ptr<InsertResult> insertionResult = treeInsertPtr(key, value);

                if (!insertionResult->_isNewEntry) {
                   return insertionResult->_oldValue;
                }
                shared_ptr<Node> x = insertionResult->_newNode;

                while (x != _root && x->_parent->_color == RED) {
                    if (x->_parent == x->_parent->_parent->_left) {
                        shared_ptr<Node> y = x->_parent->_parent->_right;
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
                        shared_ptr<Node> y = x->_parent->_parent->_left;
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
                return nullptr;
            }

            V put(K key, V value) {
                shared_ptr<InsertResult> insertionResult = treeInsert(key, value);

                if (!insertionResult->_isNewEntry) {
                   return insertionResult->_oldValue;
                }
                shared_ptr<Node> x = insertionResult->_newNode;

                while (x != _root && x->_parent->_color == RED) {
                    if (x->_parent == x->_parent->_parent->_left) {
                        shared_ptr<Node> y = x->_parent->_parent->_right;
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
                        shared_ptr<Node> y = x->_parent->_parent->_left;
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
                return V();
            }

            V remove(K key) {
                shared_ptr<Node> z = findNode(key);
                if (z == nullptr) {
                    return nullptr;
                }

                // Y is the node to be unlinked from the tree.
                shared_ptr<Node> y;
                if (z->_left == nullptr || z->_right == nullptr) {
                    y = z;
                } else {
                    y = z->successor();
                }

                // Y is guaranteed to be non-null at this point.
                shared_ptr<Node> x;
                if (y->_left != nullptr) {
                    x = y->_left;
                } else {
                    x = y->_right;
                }
                // X is the child of y which might potentially replace y in the tree. X might be null at
                // this point.
                shared_ptr<Node> xParent;
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

            V getPtr(K key) {
                shared_ptr<Node> node = findNode(key);
                if (node == nullptr) {
                    return nullptr;
                }
                return node->_value;
            }

            V get(K key) {
                shared_ptr<Node> node = findNode(key);
                if (node == nullptr) {
                    return V();
                }
                return node->_value;
            }

            void forEach(function<void(shared_ptr<Entry>)> fn) { 
                if (_root == nullptr) {
                    return;
                }
                shared_ptr<Node> current = treeMinimum(_root);
                while (current != nullptr) {
                    fn(make_shared<Entry>(current->_key, current->_value));
                    current = current->successor();
                }
            }

            shared_ptr<Node> findNode(K key) {
                shared_ptr<Node> current = _root;
                while (current != nullptr) {
                    int comparisonResult = key->compareTo(current->_key);
                    if (comparisonResult == 0) {
                        return current;
                    }
                    if (comparisonResult < 0) {
                        current = current->_left;
                    }
                    else {
                        current = current->_right;
                    }
                }
                return nullptr;
            }



        

    };
};