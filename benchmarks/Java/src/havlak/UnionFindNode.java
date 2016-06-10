package havlak;

import som.Vector;


/**
 * class UnionFindNode
 *
 * The algorithm uses the Union/Find algorithm to collapse
 * complete loops into a single node. These nodes and the
 * corresponding functionality are implemented with this class
 */
final class UnionFindNode {

  private UnionFindNode parent;
  private BasicBlock    bb;
  private SimpleLoop    loop;
  private int           dfsNumber;

  UnionFindNode() { }

  // Initialize this node.
  //
  public void initNode(final BasicBlock bb, final int dfsNumber) {
    this.parent     = this;
    this.bb         = bb;
    this.dfsNumber  = dfsNumber;
    this.loop       = null;
  }

  // Union/Find Algorithm - The find routine.
  //
  // Implemented with Path Compression (inner loops are only
  // visited and collapsed once, however, deep nests would still
  // result in significant traversals).
  //
  public UnionFindNode findSet() {
    Vector<UnionFindNode> nodeList = new Vector<>();

    UnionFindNode node = this;
    while (node != node.parent) {
      if (node.parent != node.parent.parent) {
        nodeList.append(node);
      }
      node = node.parent;
    }

    // Path Compression, all nodes' parents point to the 1st level parent.
    nodeList.forEach(iter -> iter.union(parent));
    return node;
  }

  // Union/Find Algorithm - The union routine.
  //
  // Trivial. Assigning parent pointer is enough,
  // we rely on path compression.
  //
  public void union(final UnionFindNode basicBlock) {
    parent = basicBlock;
  }

  // Getters/Setters
  //
  public BasicBlock getBb() {
    return bb;
  }

  public SimpleLoop getLoop() {
    return loop;
  }

  public int getDfsNumber() {
    return dfsNumber;
  }

  public void setLoop(final SimpleLoop loop) {
    this.loop = loop;
  }
}
