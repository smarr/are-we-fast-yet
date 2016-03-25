package havlak;

import java.util.ArrayList;
import java.util.List;


/**
 * class UnionFindNode
 *
 * The algorithm uses the Union/Find algorithm to collapse
 * complete loops into a single node. These nodes and the
 * corresponding functionality are implemented with this class
 */
class UnionFindNode {

  private UnionFindNode parent;
  private BasicBlock    bb;
  private SimpleLoop    loop;
  private int           dfsNumber;

  UnionFindNode() {}

  // Initialize this node.
  //
  void initNode(final BasicBlock bb, final int dfsNumber) {
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
  UnionFindNode findSet() {

    List<UnionFindNode> nodeList = new ArrayList<UnionFindNode>();

    UnionFindNode node = this;
    while (node != node.getParent()) {
      if (node.getParent() != node.getParent().getParent()) {
        nodeList.add(node);
      }
      node = node.getParent();
    }

    // Path Compression, all nodes' parents point to the 1st level parent.
    for (UnionFindNode iter : nodeList) {
      iter.setParent(node.getParent());
    }
    return node;
  }

  // Union/Find Algorithm - The union routine.
  //
  // Trivial. Assigning parent pointer is enough,
  // we rely on path compression.
  //
  void union(final UnionFindNode basicBlock) {
    setParent(basicBlock);
  }

  // Getters/Setters
  //
  UnionFindNode getParent() {
    return parent;
  }

  BasicBlock getBb() {
    return bb;
  }

  SimpleLoop getLoop() {
    return loop;
  }

  int getDfsNumber() {
    return dfsNumber;
  }

  void setParent(final UnionFindNode parent) {
    this.parent = parent;
  }

  void setLoop(final SimpleLoop loop) {
    this.loop = loop;
  }
}