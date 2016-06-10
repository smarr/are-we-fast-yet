package cd;

import som.ForEachInterface;


public final class RedBlackTree<K extends Comparable<K>, V> {

  Node<K, V> root;

  public RedBlackTree() {
    root = null;
  }

  private enum Color {
    RED, BLACK
  }

  private static <K, V> Node<K, V> treeMinimum(final Node<K, V> x) {
    Node<K, V> current = x;
    while (current.left != null) {
      current = current.left;
    }
    return current;
  }

  private static final class Node<K, V> {

    private final K    key;
    private V value;
    private Node<K, V> left;
    private Node<K, V> right;
    private Node<K, V> parent;
    private Color      color;

    Node(final K key, final V value) {
      this.key = key;
      this.value = value;
      this.left = null;
      this.right = null;
      this.parent = null;
      this.color = Color.RED;
    }

    private Node<K, V> successor() {
      Node<K, V> x = this;
      if (x.right != null) {
        return treeMinimum(x.right);
      }
      Node<K, V> y = x.parent;
      while (y != null && x == y.right) {
        x = y;
        y = y.parent;
      }
      return y;
    }
  }

  public V put(final K key, final V value) {
    InsertResult<K, V> insertionResult = treeInsert(key, value);
    if (!insertionResult.isNewEntry) {
      return insertionResult.oldValue;
    }
    Node<K, V> x = insertionResult.newNode;

    while (x != root && x.parent.color == Color.RED) {
      if (x.parent == x.parent.parent.left) {
        Node<K, V> y = x.parent.parent.right;
        if (y != null && y.color == Color.RED) {
          // Case 1
          x.parent.color = Color.BLACK;
          y.color = Color.BLACK;
          x.parent.parent.color = Color.RED;
          x = x.parent.parent;
        } else {
          if (x == x.parent.right) {
            // Case 2
            x = x.parent;
            leftRotate(x);
          }
          // Case 3
          x.parent.color = Color.BLACK;
          x.parent.parent.color = Color.RED;
          rightRotate(x.parent.parent);
        }
      } else {
        // Same as "then" clause with "right" and "left" exchanged.
        Node<K, V> y = x.parent.parent.left;
        if (y != null && y.color == Color.RED) {
          // Case 1
          x.parent.color = Color.BLACK;
          y.color = Color.BLACK;
          x.parent.parent.color = Color.RED;
          x = x.parent.parent;
        } else {
          if (x == x.parent.left) {
            // Case 2
            x = x.parent;
            rightRotate(x);
          }
          // Case 3
          x.parent.color = Color.BLACK;
          x.parent.parent.color = Color.RED;
          leftRotate(x.parent.parent);
        }
      }
    }

    root.color = Color.BLACK;
    return null;
  }

  public V remove(final K key) {
    Node<K, V> z = findNode(key);
    if (z == null) {
      return null;
    }

    // Y is the node to be unlinked from the tree.
    Node<K, V> y;
    if (z.left == null || z.right == null) {
      y = z;
    } else {
      y = z.successor();
    }

    // Y is guaranteed to be non-null at this point.
    Node<K, V> x;
    if (y.left != null) {
      x = y.left;
    } else {
      x = y.right;
    }

    // X is the child of y which might potentially replace y in the tree. X might be null at
    // this point.
    Node<K, V> xParent;
    if (x != null) {
      x.parent = y.parent;
      xParent = x.parent;
    } else {
      xParent = y.parent;
    }
    if (y.parent == null) {
      root = x;
    } else {
      if (y == y.parent.left) {
        y.parent.left = x;
      } else {
        y.parent.right = x;
      }
    }

    if (y != z) {
      if (y.color == Color.BLACK) {
        removeFixup(x, xParent);
      }

      y.parent = z.parent;
      y.color = z.color;
      y.left = z.left;
      y.right = z.right;

      if (z.left != null) {
        z.left.parent = y;
      }
      if (z.right != null) {
        z.right.parent = y;
      }
      if (z.parent != null) {
        if (z.parent.left == z) {
          z.parent.left = y;
        } else {
          z.parent.right = y;
        }
      } else {
        root = y;
      }
    } else if (y.color == Color.BLACK) {
      removeFixup(x, xParent);
    }

    return z.value;
  }

  public V get(final K key) {
    Node<K, V> node = findNode(key);
    if (node == null) {
      return null;
    }
    return node.value;
  }

  public static final class Entry<K, V> {
    public final K key;
    public final V value;
    public Entry(final K key, final V value) {
      this.key = key;
      this.value = value;
    }
  }

  public void forEach(final ForEachInterface<Entry<K, V>> fn) {
    if (root == null) {
      return;
    }
    Node<K, V> current = treeMinimum(root);
    while (current != null) {
      fn.apply(new Entry<>(current.key, current.value));
      current = current.successor();
    }
  }

  private Node<K, V> findNode(final K key) {
    Node<K, V> current = root;
    while (current != null) {
      int comparisonResult = key.compareTo(current.key);
      if (comparisonResult == 0) {
        return current;
      }
      if (comparisonResult < 0) {
        current = current.left;
      } else {
        current = current.right;
      }
    }
    return null;
  }

  private static final class InsertResult<K, V> {
    public final boolean isNewEntry;
    public final Node<K, V> newNode;
    public final V oldValue;

    InsertResult(final boolean isNewEntry, final Node<K, V> newNode, final V oldValue) {
      this.isNewEntry = isNewEntry;
      this.newNode = newNode;
      this.oldValue = oldValue;
    }
  }

  private InsertResult<K, V> treeInsert(final K key, final V value) {
    Node<K, V> y = null;
    Node<K, V> x = root;

    while (x != null) {
      y = x;
      int comparisonResult = key.compareTo(x.key);
      if (comparisonResult < 0) {
        x = x.left;
      } else if (comparisonResult > 0) {
        x = x.right;
      } else {
        V oldValue = x.value;
        x.value = value;
        return new InsertResult<>(false, null, oldValue);
      }
    }

    Node<K, V> z = new Node<>(key, value);
    z.parent = y;
    if (y == null) {
      root = z;
    } else {
      if (key.compareTo(y.key) < 0) {
        y.left = z;
      } else {
        y.right = z;
      }
    }
    return new InsertResult<>(true, z, null);
  };

  private Node<K, V> leftRotate(final Node<K, V> x) {
    Node<K, V> y = x.right;

    // Turn y's left subtree into x's right subtree.
    x.right = y.left;
    if (y.left != null) {
      y.left.parent = x;
    }

    // Link x's parent to y.
    y.parent = x.parent;
    if (x.parent == null) {
      root = y;
    } else {
      if (x == x.parent.left) {
        x.parent.left = y;
      } else {
        x.parent.right = y;
      }
    }

    // Put x on y's left.
    y.left = x;
    x.parent = y;

    return y;
  }

  private Node<K, V> rightRotate(final Node<K, V> y) {
    Node<K, V> x = y.left;

    // Turn x's right subtree into y's left subtree.
    y.left = x.right;
    if (x.right != null) {
      x.right.parent = y;
    }

    // Link y's parent to x;
    x.parent = y.parent;
    if (y.parent == null) {
      root = x;
    } else {
      if (y == y.parent.left) {
        y.parent.left = x;
      } else {
        y.parent.right = x;
      }
    }

    x.right = y;
    y.parent = x;

    return x;
  }

  private void removeFixup(Node<K, V> x, Node<K, V> xParent) {
    while (x != root && (x == null || x.color == Color.BLACK)) {
      if (x == xParent.left) {
        // Note: the text points out that w cannot be null. The reason is not obvious from
        // simply looking at the code; it comes about from the properties of the red-black
        // tree.
        Node<K, V> w = xParent.right;
        if (w.color == Color.RED) {
          // Case 1
          w.color = Color.BLACK;
          xParent.color = Color.RED;
          leftRotate(xParent);
          w = xParent.right;
        }
        if ((w.left == null || w.left.color == Color.BLACK)
            && (w.right == null || w.right.color == Color.BLACK)) {
          // Case 2
          w.color = Color.RED;
          x = xParent;
          xParent = x.parent;
        } else {
          if (w.right == null || w.right.color == Color.BLACK) {
            // Case 3
            w.left.color = Color.BLACK;
            w.color = Color.RED;
            rightRotate(w);
            w = xParent.right;
          }
          // Case 4
          w.color = xParent.color;
          xParent.color = Color.BLACK;
          if (w.right != null) {
            w.right.color = Color.BLACK;
          }
          leftRotate(xParent);
          x = root;
          xParent = x.parent;
        }
      } else {
        // Same as "then" clause with "right" and "left" exchanged.
        Node<K, V> w = xParent.left;
        if (w.color == Color.RED) {
          // Case 1
          w.color = Color.BLACK;
          xParent.color = Color.RED;
          rightRotate(xParent);
          w = xParent.left;
        }
        if ((w.right == null || w.right.color == Color.BLACK)
            && (w.left == null || w.left.color == Color.BLACK)) {
          // Case 2
          w.color = Color.RED;
          x = xParent;
          xParent = x.parent;
        } else {
          if (w.left == null || w.left.color == Color.BLACK) {
            // Case 3
            w.right.color = Color.BLACK;
            w.color = Color.RED;
            leftRotate(w);
            w = xParent.left;
          }
          // Case 4
          w.color = xParent.color;
          xParent.color = Color.BLACK;
          if (w.left != null) {
            w.left.color = Color.BLACK;
          }
          rightRotate(xParent);
          x = root;
          xParent = x.parent;
        }
      }
    }
    if (x != null) {
      x.color = Color.BLACK;
    }
  }
}
