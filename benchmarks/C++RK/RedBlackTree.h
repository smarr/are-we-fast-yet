#ifndef _REDBLACKTREE_H
#define _REDBLACKTREE_H

#include <som/Interfaces.h>

template <class K, class V, class C>
class RedBlackTree {

    enum Color {
        RED, BLACK
    };

    template <class KK, class VV>
    class Node {
        Node();
    public:
        KK    key;
        VV value;
        Node<KK, VV>* left;
        Node<KK, VV>* right;
        Node<KK, VV>* parent;
        Color color;
        Node(const KK& key, const VV& value) {
            this->key = key;
            this->value = value;
            this->left = 0;
            this->right = 0;
            this->parent = 0;
            this->color = RED;
        }

        ~Node()
        {
            // this is apparently enough to get rid of all Node
            if( left )
                delete left;
            if( right )
                delete right;
        }

        Node<KK, VV>* successor() {
            Node<KK, VV>* x = this;
            if (x->right != 0) {
                return treeMinimum(x->right);
            }
            Node<KK, VV>* y = x->parent;
            while (y != 0 && x == y->right) {
                x = y;
                y = y->parent;
            }
            return y;
        }
    };

    template <class KK, class VV>
    static Node<KK, VV>* treeMinimum(Node<KK, VV>* x) {
        Node<KK, VV>* current = x;
        while (current->left != 0) {
            current = current->left;
        }
        return current;
    }


    Node<K, V>* root;
    bool isNewEntry;
    Node<K, V>* newNode;
    V oldValue;
public:
    RedBlackTree() {
        root = 0;
    }

    ~RedBlackTree()
    {
        if( root )
            delete root;
    }

    V* put(const K& key, const V& value) {
        treeInsert(key, value);
        if (!isNewEntry) {
            return &oldValue;
        }
        Node<K, V>* x = newNode;

        while (x != root && x->parent->color == RED) {
            if (x->parent == x->parent->parent->left) {
                Node<K, V>* y = x->parent->parent->right;
                if (y != 0 && y->color == RED) {
                    // Case 1
                    x->parent->color = BLACK;
                    y->color = BLACK;
                    x->parent->parent->color = RED;
                    x = x->parent->parent;
                } else {
                    if (x == x->parent->right) {
                        // Case 2
                        x = x->parent;
                        leftRotate(x);
                    }
                    // Case 3
                    x->parent->color = BLACK;
                    x->parent->parent->color = RED;
                    rightRotate(x->parent->parent);
                }
            } else {
                // Same as "then" clause with "right" and "left" exchanged.
                Node<K, V>* y = x->parent->parent->left;
                if (y != 0 && y->color == RED) {
                    // Case 1
                    x->parent->color = BLACK;
                    y->color = BLACK;
                    x->parent->parent->color = RED;
                    x = x->parent->parent;
                } else {
                    if (x == x->parent->left) {
                        // Case 2
                        x = x->parent;
                        rightRotate(x);
                    }
                    // Case 3
                    x->parent->color = BLACK;
                    x->parent->parent->color = RED;
                    leftRotate(x->parent->parent);
                }
            }
        }

        root->color = BLACK;
        return 0;
    }

    V remove(const K& key) {
        // NOTE: apparently we don't get here during CD, otherwise we
        // should take care of Node dealloc
        Node<K, V>* z = findNode(key);
        if (z == 0) {
            return V();
        }

        // Y is the node to be unlinked from the tree.
        Node<K, V>* y;
        if (z->left == 0 || z->right == 0) {
            y = z;
        } else {
            y = z->successor();
        }

        // Y is guaranteed to be non-null at this point.
        Node<K, V>* x;
        if (y->left != 0) {
            x = y->left;
        } else {
            x = y->right;
        }

        // X is the child of y which might potentially replace y in the tree. X might be null at
        // this point.
        Node<K, V>* xParent;
        if (x != 0) {
            x->parent = y->parent;
            xParent = x->parent;
        } else {
            xParent = y->parent;
        }
        if (y->parent == 0) {
            root = x;
        } else {
            if (y == y->parent->left) {
                y->parent->left = x;
            } else {
                y->parent->right = x;
            }
        }

        if (y != z) {
            if (y->color == BLACK) {
                removeFixup(x, xParent);
            }

            y->parent = z->parent;
            y->color = z->color;
            y->left = z->left;
            y->right = z->right;

            if (z->left != 0) {
                z->left->parent = y;
            }
            if (z->right != 0) {
                z->right->parent = y;
            }
            if (z->parent != 0) {
                if (z->parent->left == z) {
                    z->parent->left = y;
                } else {
                    z->parent->right = y;
                }
            } else {
                root = y;
            }
        } else if (y->color == BLACK) {
            removeFixup(x, xParent);
        }

        return z->value;
    }

    V* get(const K& key) {
        Node<K, V>* node = findNode(key);
        if (node == 0) {
            return 0;
        }
        return &node->value;
    }

    template <class KK, class VV>
    class Entry {
    public:
        KK key;
        VV value;
        Entry(const KK& key, const VV& value) {
            this->key = key;
            this->value = value;
        }
    };

    void forEach(som::ForEachInterface<Entry<K, V> >& fn) {
        if (root == 0) {
            return;
        }
        Node<K, V>* current = treeMinimum(root);
        while (current != 0) {
            fn.apply(Entry<K, V>(current->key, current->value));
            current = current->successor();
        }
    }

private:
    Node<K, V>* findNode(const K& key) {
        Node<K, V>* current = root;
        C compareTo;
        while (current != 0) {
            int comparisonResult = compareTo(key, current->key);
            if (comparisonResult == 0) {
                return current;
            }
            if (comparisonResult < 0) {
                current = current->left;
            } else {
                current = current->right;
            }
        }
        return 0;
    }

    void InsertResult(bool isNewEntry, Node<K, V>* newNode, const V& oldValue) {
        this->isNewEntry = isNewEntry;
        this->newNode = newNode;
        this->oldValue = oldValue;
    }

    void treeInsert(const K& key, const V& value) {
        Node<K, V>* y = 0;
        Node<K, V>* x = root;
        C compareTo;

        while (x != 0) {
            y = x;
            const int comparisonResult = compareTo(key, x->key);
            if (comparisonResult < 0) {
                x = x->left;
            } else if (comparisonResult > 0) {
                x = x->right;
            } else {
                V oldValue = x->value;
                x->value = value;
                InsertResult(false, 0, oldValue);
                return;
            }
        }

        Node<K, V>* z = new Node<K, V>(key, value);
        z->parent = y;
        if (y == 0) {
            root = z;
        } else {
            if ( compareTo(key, y->key) < 0) {
                y->left = z;
            } else {
                y->right = z;
            }
        }
        InsertResult(true, z, V());
    }

    Node<K, V>* leftRotate(Node<K, V>* x) {
        Node<K, V>* y = x->right;

        // Turn y's left subtree into x's right subtree.
        x->right = y->left;
        if (y->left != 0) {
            y->left->parent = x;
        }

        // Link x's parent to y.
        y->parent = x->parent;
        if (x->parent == 0) {
            root = y;
        } else {
            if (x == x->parent->left) {
                x->parent->left = y;
            } else {
                x->parent->right = y;
            }
        }

        // Put x on y's left.
        y->left = x;
        x->parent = y;

        return y;
    }

    Node<K, V>* rightRotate(Node<K, V>* y) {
        Node<K, V>* x = y->left;

        // Turn x's right subtree into y's left subtree.
        y->left = x->right;
        if (x->right != 0) {
            x->right->parent = y;
        }

        // Link y's parent to x;
        x->parent = y->parent;
        if (y->parent == 0) {
            root = x;
        } else {
            if (y == y->parent->left) {
                y->parent->left = x;
            } else {
                y->parent->right = x;
            }
        }

        x->right = y;
        y->parent = x;

        return x;
    }

    void removeFixup(Node<K, V>* x, Node<K, V>* xParent) {
        while (x != root && (x == 0 || x->color == BLACK)) {
            if (x == xParent->left) {
                // Note: the text points out that w cannot be null. The reason is not obvious from
                // simply looking at the code; it comes about from the properties of the red-black
                // tree.
                Node<K, V>* w = xParent->right;
                if (w->color == RED) {
                    // Case 1
                    w->color = BLACK;
                    xParent->color = RED;
                    leftRotate(xParent);
                    w = xParent->right;
                }
                if ((w->left == 0 || w->left->color == BLACK)
                        && (w->right == 0 || w->right->color == BLACK)) {
                    // Case 2
                    w->color = RED;
                    x = xParent;
                    xParent = x->parent;
                } else {
                    if (w->right == 0 || w->right->color == BLACK) {
                        // Case 3
                        w->left->color = BLACK;
                        w->color = RED;
                        rightRotate(w);
                        w = xParent->right;
                    }
                    // Case 4
                    w->color = xParent->color;
                    xParent->color = BLACK;
                    if (w->right != 0) {
                        w->right->color = BLACK;
                    }
                    leftRotate(xParent);
                    x = root;
                    xParent = x->parent;
                }
            } else {
                // Same as "then" clause with "right" and "left" exchanged.
                Node<K, V>* w = xParent->left;
                if (w->color == RED) {
                    // Case 1
                    w->color = BLACK;
                    xParent->color = RED;
                    rightRotate(xParent);
                    w = xParent->left;
                }
                if ((w->right == 0 || w->right->color == BLACK)
                        && (w->left == 0 || w->left->color == BLACK)) {
                    // Case 2
                    w->color = RED;
                    x = xParent;
                    xParent = x->parent;
                } else {
                    if (w->left == 0 || w->left->color == BLACK) {
                        // Case 3
                        w->right->color = BLACK;
                        w->color = RED;
                        leftRotate(w);
                        w = xParent->left;
                    }
                    // Case 4
                    w->color = xParent->color;
                    xParent->color = BLACK;
                    if (w->left != 0) {
                        w->left->color = BLACK;
                    }
                    rightRotate(xParent);
                    x = root;
                    xParent = x->parent;
                }
            }
        }
        if (x != 0) {
            x->color = BLACK;
        }
    }
};

#endif // _REDBLACKTREE_H
