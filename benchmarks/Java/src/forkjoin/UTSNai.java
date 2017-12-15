package forkjoin;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import som.Benchmark;

// Parallelized, but no local work.

public final class UTSNai extends Benchmark {

  // TREE TYPE AND SHAPE CONSTANTS
  static final int BIN    = 0; // TYPE: binomial tree
  static final int GEO    = 1; // TYPE: geometric tree
  static final int HYBRID = 2; // TYPE: hybrid tree, start geometric, shift to
                               // binomial
  static final int LINEAR = 0; // SHAPE: linearly decreasing geometric tree
  static final int EXPDEC = 1; // SHAPE: exponentially decreasing geometric tree
  static final int CYCLIC = 2; // SHAPE: cyclic geometric tree
  static final int FIXED  = 3; // SHAPE: fixed branching factor geometric tree

  static final int    UNSETI = -1;   // sentinel for unset integer values
  static final double UNSETD = -1.0; // sentinel for unset double values

  static final String TREES[] = { "Binomial", "Geometric", "Hybrid" };

  static final String SHAPES[] = { "Linear decrease", "Exponential decrease",
      "Cyclic", "Fixed branching factor" };

  // UTS parameters and defaults
  private int    treetype    = GEO;         // UTS Type: Default = GEO
  private double b0          = 4.0;         // branching factor for root node
  private int    rootId      = 0;           // RNG seed for root node
  private int    nonLeafBF   = 4;           // BINOMIAL TREE: branching factor
                                            // for nonLeaf nodes
  private double nonLeafProb = 15.0 / 64.0; // BINOMIAL TREE: probability a node
                                            // is a nonLeaf
  private int    genMax      = 6;           // GEOMETRIC TREE: maximum number of
                                            // generations
  private int    shapeFn     = LINEAR;      // GEOMETRIC TREE: shape function:
                                            // Default = LINEAR
  private double shiftDepth  = 0.5;         // HYBRID TREE: Depth fraction for
                                            // shift from GEO to BIN
  private int    computeGran = 1;           // number of RNG evaluations per
                                            // tree node

  private Node root;

  // UTS Performance Statistics

  private AtomicInteger nNodes;      // total number of nodes discovered in tree
  private AtomicInteger nLeaves;     // total number of leafnodes discovered in
                                     // tree
  private AtomicInteger maxUTSDepth; // maximum tree depth

  private int type;

  private void init(final int treeType) {
    type = treeType;

    // T1 & T3 used in other papers
    if (treeType == 2) {
      // T2="-t 1 -a 2 -d 16 -b 6 -r 502"
      rootId = 502;
      treetype = 1;
      shapeFn = 2;
      b0 = 6;
      genMax = 16;
    } else if (treeType == 3) {
      // T3="-t 0 -b 2000 -q 0.124875 -m 8 -r 42"
      rootId = 42;
      nonLeafProb = 0.124875;
      nonLeafBF = 8;
      treetype = 0;
      b0 = 2000;
    } else if (treeType == 4) {
      // T4="-t 2 -a 0 -d 16 -b 6 -r 1 -q 0.234375 -m 4"
      rootId = 1;
      nonLeafProb = 0.234375;
      nonLeafBF = 4;
      treetype = 2;
      shapeFn = 0;
      b0 = 6;
      genMax = 16;
    } else if (treeType == 5) {
      // T5="-t 1 -a 0 -d 20 -b 4 -r 34"
      rootId = 34;
      treetype = 1;
      shapeFn = 0;
      b0 = 4;
      genMax = 20;
    } else if (treeType == 1) {
      // T1="-t 1 -a 3 -d 10 -b 4 -r 19"
      rootId = 19;
      treetype = 1;
      shapeFn = 3;
      b0 = 4;
      genMax = 10;
    } else if (treeType == 6) {
      // T1L="-t 1 -a 3 -d 13 -b 4 -r 29"
      rootId = 29;
      treetype = 1;
      shapeFn = 3;
      b0 = 4;
      genMax = 13;
    } else {
      throw new RuntimeException("unsupported treeType");
    }
    nNodes = new AtomicInteger(0);
    nLeaves = new AtomicInteger(0);
    maxUTSDepth = new AtomicInteger(0);
  }

  @Override
  public boolean innerBenchmarkLoop(final int treeType) {
    init(treeType);
    compute();

    return status();
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }

  public final void compute() {
    initRoot();
    Core[] tasks = search(root);

    joinAll(tasks);
  }

  private void joinAll(final Core[] tasks) {
    if (tasks != null) {
      for (Core task : tasks) {
        joinAll(task.join());
      }
    }
  }

  private Core[] search(final Node parent) {
    nNodes.incrementAndGet();
    maxUTSDepth.set(Math.max(parent.height, maxUTSDepth.get()));
    int numChildren = parent.numChildren();
    if (numChildren > 0) {
      Core[] children = new Core[numChildren];

      for (int i = 0; i < numChildren; i++) {
        children[i] = new Core(this, parent, i);
        children[i].fork();
      }
      return children;
    } else {
      nLeaves.incrementAndGet();
      return null;
    }
  }

  private static class Core extends RecursiveTask<Core[]> {

    private static final long serialVersionUID = 2813249622277043197L;

    private final UTSNai  uTSNai;
    private final Node parent;
    private final int  i;

    Core(final UTSNai uTSNai, final Node parent, final int i) {
      this.uTSNai = uTSNai;
      this.parent = parent;
      this.i = i;
    }

    @Override
    protected Core[] compute() {
      return uTSNai.search(uTSNai.new Node(parent, i));
    }
  }

  void initRoot() {
    root = new Node(rootId);
    nNodes.set(0);
    nLeaves.set(0);
    maxUTSDepth.set(0);
  }

  final boolean status() {
    int nNodes = 0;
    int maxUTSDepth = 0;
    int nLeaves = 0;

    if (type == 1) {
      nNodes = 4130071;
      maxUTSDepth = 10;
      nLeaves = 3305118;
    } else if (type == 2) {
      nNodes = 4117769;
      maxUTSDepth = 81;
      nLeaves = 2342762;
    } else if (type == 3) {
      nNodes = 1732;
      maxUTSDepth = 6;
      nLeaves = 1050;
    } else if (type == 4) {
      nNodes = 4132453;
      maxUTSDepth = 134;
      nLeaves = 3108986;
    } else if (type == 5) {
      nNodes = 4147582;
      maxUTSDepth = 20;
      nLeaves = 2181318;
    } else {
      nNodes = 1732;
      maxUTSDepth = 6;
      nLeaves = 1050;
    }

    String s1 = "", s2 = "", s3 = "";

    final int nNodes_r = this.nNodes.get();
    final int maxUTSDepth_r = this.maxUTSDepth.get();
    final int nLeaves_r = this.nLeaves.get();

    if (nNodes_r != nNodes) {
      s1 = ("Tree Size Expected = " + nNodes + " but Obtained = " + nNodes_r);
    }
    if (maxUTSDepth_r != maxUTSDepth) {
      s2 = (" Tree Depth Expected = " + maxUTSDepth + " but Obtained = "
          + maxUTSDepth_r);
    }
    if (nLeaves_r != nLeaves) {
      s3 = (" Tree Num_Leaves Expected = " + nLeaves + " but Obtained = "
          + nLeaves_r);
    }

    if (s1.length() == 0 && s2.length() == 0 && s3.length() == 0) {
      return true;
    } else {
      System.out.println(s1 + s2 + s3);
      return false;
    }
  }

  public class Node {

    // Node State
    private final SHA1Generator state;

    private final int type;
    private final int height;
    private int nChildren;

    // misc constants
    static final double TWO_PI         = 2.0 * Math.PI;
    static final int    MAXNUMCHILDREN = 100;          // max number of children
                                                       // for BIN tree

    /** root constructor: count the nodes as they are created. */
    Node(final int rootID) {
      state = new SHA1Generator(rootID);
      type = treetype;
      height = 0;
      nChildren = -1;
    }

    /** child constructor: count the nodes as they are created. */
    Node(final Node parent, final int spawn) {
      SHA1Generator s = null;
      for (int j = 0; j < computeGran; j++) {
        s = new SHA1Generator(parent.state, spawn);
      }
      state = s;

      type = parent.childType();
      height = parent.height + 1;
      nChildren = -1;
    }

    int numChildren() { // generic
      switch (treetype) {
        case BIN:
          nChildren = numChildren_bin();
          break;
        case GEO:
          nChildren = numChildren_geo();
          break;
        case HYBRID:
          if (height < shiftDepth * genMax) {
            nChildren = numChildren_geo();
          } else {
            nChildren = numChildren_bin();
          }
          break;
        default:
          error("Node:numChildren(): Unknown tree type");
      }
      if (height == 0 && type == BIN) { // only BIN root can have more than
                                        // MAXNUMCHILDREN
        int rootBF = (int) b0;
        if (nChildren > rootBF) {
          System.out.println("*** Number of children of root truncated from "
              + nChildren + " to " + rootBF);
          nChildren = rootBF;
        }
      } else {
        if (nChildren > MAXNUMCHILDREN) {
          System.out.println("*** Number of children truncated from "
              + nChildren + " to " + MAXNUMCHILDREN);
          nChildren = MAXNUMCHILDREN;
        }
      }
      return nChildren;
    }

    /** Binomial: distribution is identical below root */
    int numChildren_bin() {
      int nc;
      if (height == 0) {
        nc = (int) b0;
      } else if (rng_toProb(state.rand()) < nonLeafProb) {
        nc = nonLeafBF;
      } else {
        nc = 0;
      }
      return nc;
    }

    /** Geometric: distribution controlled by shape and height. */
    int numChildren_geo() {
      double b_i = b0;
      if (height > 0) {
        switch (shapeFn) { // use shape function to compute target b_i
          case EXPDEC: // expected size polynomial in height
            b_i = b0 * Math.pow(height, -Math.log(b0) / Math.log(genMax));
            break;
          case CYCLIC: // cyclic tree
            if (height > 5 * genMax) {
              b_i = 0.0;
              break;
            }
            b_i = Math.pow(b0, Math.sin(TWO_PI * height / genMax));
            break;
          case FIXED: // identical distribution at all nodes up to max height
            b_i = (height < genMax) ? b0 : 0;
            break;
          case LINEAR: // linear decrease in b_i
          default:
            b_i = b0 * (1.0 - ((double) height / (double) genMax));
            break;
        }
      }

      double p = 1.0 / (1.0 + b_i); // probability corresponding to target b_i
      int h = state.rand();
      double u = rng_toProb(h); // get uniform random number on [0,1)
      int nChildren = (int) (Math.log(1.0 - u) / Math.log(1.0 - p));
      // return max number of children at this cumulative probability
      return nChildren;
    }

    int childType() { // determine what kind of children this node will have
      switch (type) {
        case BIN:
          return BIN;
        case GEO:
          return GEO;
        case HYBRID:
          if (height < shiftDepth * genMax) {
            return GEO;
          } else {
            return BIN;
          }
        default:
          error("uts_get_childtype(): Unknown tree type");
          return -1;
      }
    }

    /** convert a random number on [0,2^31) to one on [0.1). */
    double rng_toProb(final int n) {
      return n < 0 ? 0.0 : n / 2147483648.0;
    }

    void error(final String data) { // bailout with error message
      System.out.println(data);
      System.exit(1);
    }
  }

  private static class SHA1Generator {

    // internal constants
    private final static int POS_MASK         = 0x7fffffff;
    private final static int LOWBYTE          = 0xFF;
    private final static int SHA1_DIGEST_SIZE = 20;

    // internal rng state
    private final byte[] state = new byte[SHA1_DIGEST_SIZE]; // 160 bit output
                                                       // representation

    // new rng from seed
    public SHA1Generator(final int seedarg) {
      byte[] seedstate = new byte[20];
      seedstate[16] = (byte) (LOWBYTE & (seedarg >>> 24));
      seedstate[17] = (byte) (LOWBYTE & (seedarg >>> 16));
      seedstate[18] = (byte) (LOWBYTE & (seedarg >>> 8));
      seedstate[19] = (byte) (LOWBYTE & (seedarg));
      SHA1Compiler sha1 = new SHA1Compiler();
      sha1.hash(seedstate, 20);
      sha1.digest(state);
    }

    // New rng from existing rng
    public SHA1Generator(final SHA1Generator parent, final int spawnnumber) {
      byte[] seedstate = new byte[4];
      seedstate[0] = (byte) (LOWBYTE & (spawnnumber >>> 24));
      seedstate[1] = (byte) (LOWBYTE & (spawnnumber >>> 16));
      seedstate[2] = (byte) (LOWBYTE & (spawnnumber >>> 8));
      seedstate[3] = (byte) (LOWBYTE & (spawnnumber));
      SHA1Compiler sha1 = new SHA1Compiler();
      sha1.hash(parent.state, 20);
      sha1.hash(seedstate, 4);
      sha1.digest(state);
    }

    // return current random number (no advance)
    public final int rand() {
      return POS_MASK
          & (((LOWBYTE & state[16]) << 24) | ((LOWBYTE & state[17]) << 16)
              | ((LOWBYTE & state[18]) << 8) | ((LOWBYTE & state[19])));
    }
  }

  private static enum Functions {
    CH, MAJ, PARITY
  }

  private static final class SHA1Compiler {

    // internal constants
    private final static int SHA1_DIGEST_SIZE = 20;
    private final static int SHA1_BLOCK_SIZE  = 64;
    private final static int SHA1_MASK        = SHA1_BLOCK_SIZE - 1;

    // internal rng state
    private final int[] digest   = new int[SHA1_DIGEST_SIZE / 4]; // 160 bit internal
                                                            // representation
    private final int[] msgblock = new int[SHA1_BLOCK_SIZE / 4];  // 64 byte internal
                                                            // working buffer
    private long  count    = 0;                             // 64 bit counter of
                                                            // bytes processed

    SHA1Compiler() {
      digest[0] = 0x67452301;
      digest[1] = 0xefcdab89;
      digest[2] = 0x98badcfe;
      digest[3] = 0x10325476;
      digest[4] = 0xc3d2e1f0;
    }

    public final void hash(final byte[] data, final int length) {
      int bp = 0; // byte position in data[]
      int pos = (int) (count & SHA1_MASK); // byte position in msgblock
      int wpos = pos >>> 2; // word position in msgblock
      int space = SHA1_BLOCK_SIZE - pos; // bytes left in msgblock
      int len = length; // number of bytes left to process in data
      count += len; // total number of bytes processed since begin
      while (len >= space) {
        for (; wpos < (SHA1_BLOCK_SIZE >>> 2); bp += 4) { // "int" aligned
                                                          // (byte)memory to
                                                          // (int)memory copy
          msgblock[wpos++] = ((data[bp] & 0xFF) << 24)
              | ((data[bp + 1] & 0xFF) << 16) | ((data[bp + 2] & 0xFF) << 8)
              | (data[bp + 3] & 0xFF);
        }
        compile();
        len -= space;
        space = SHA1_BLOCK_SIZE;
        wpos = 0;
      }
      for (; bp < length; bp += 4) { // this is the "int" aligned (byte)memory
                                     // to
                                     // (int)memory copy
        msgblock[wpos++] = ((data[bp] & 0xFF) << 24)
            | ((data[bp + 1] & 0xFF) << 16) | ((data[bp + 2] & 0xFF) << 8)
            | (data[bp + 3] & 0xFF);
      }
    }

    public final void digest(final byte[] output) {
      // how many bytes already in msgblock[]?
      int i = (int) (count & SHA1_MASK);

      msgblock[i >> 2] &= 0xffffff80 << 8 * (~i & 3);
      msgblock[i >> 2] |= 0x00000080 << 8 * (~i & 3);

      if (i > SHA1_BLOCK_SIZE - 9) {
        if (i < 60) {
          msgblock[15] = 0;
        }
        compile();
        i = 0;
      } else {
        i = (i >> 2) + 1;
      }

      while (i < 14) {
        msgblock[i++] = 0;
      }

      msgblock[14] = (int) (count >> 29);
      msgblock[15] = (int) (count << 3);

      compile(); // THIS call accounts for 50% of the program execution time...

      for (i = 0; i < SHA1_DIGEST_SIZE; ++i) {
        output[i] = (byte) (digest[i >> 2] >> (8 * (~i & 3)));
      }
    }

    private static final int rotl32(final int x, final int n) {
      return (x << n) | (x >>> (32 - n));
    }

    private static final int rotr32(final int x, final int n) {
      return (x >>> n) | (x << (32 - n));
    }

    private static final int ch(final int x, final int y, final int z) {
      return (z ^ (x & (y ^ z)));
    }

    private static final int parity(final int x, final int y, final int z) {
      return (x ^ y ^ z);
    }

    private static final int maj(final int x, final int y, final int z) {
      return ((x & y) | (z & (x ^ y)));
    }

    private static final int hf(final int[] w, final int i,
        final boolean hf_basic) {
      if (hf_basic) {
        return w[i];
      } else {
        int x = i & 15;
        w[x] = rotl32(w[((i) + 13) & 15] ^ w[((i) + 8) & 15] ^ w[((i) + 2) & 15]
            ^ w[(i) & 15], 1);
        return w[x];
      }
    }

    private static final void oneCycle(final int[] v, final int a, final int b,
        final int c, final int d, final int e, final Functions f, final int k,
        final int h) {

      switch (f) {
        case CH:
          v[e] += (rotr32(v[a], 27) + ch(v[b], v[c], v[d]) + k + h);
          break;
        case MAJ:
          v[e] += (rotr32(v[a], 27) + maj(v[b], v[c], v[d]) + k + h);
          break;
        case PARITY:
          v[e] += (rotr32(v[a], 27) + parity(v[b], v[c], v[d]) + k + h);
          break;
      }

      v[b] = rotr32(v[b], 2);
    }

    private static final void fiveCycle(final int[] w, final boolean hf_basic,
        final int[] v, final Functions f, final int k, final int i) {
      oneCycle(v, 0, 1, 2, 3, 4, f, k, hf(w, i, hf_basic));
      oneCycle(v, 4, 0, 1, 2, 3, f, k, hf(w, i + 1, hf_basic));
      oneCycle(v, 3, 4, 0, 1, 2, f, k, hf(w, i + 2, hf_basic));
      oneCycle(v, 2, 3, 4, 0, 1, f, k, hf(w, i + 3, hf_basic));
      oneCycle(v, 1, 2, 3, 4, 0, f, k, hf(w, i + 4, hf_basic));
    }

    private final void compile() {
      int[] v = new int[5];
      System.arraycopy(digest, 0, v, 0, 5);

      fiveCycle(msgblock, true, v, Functions.CH, 0x5a827999, 0);
      fiveCycle(msgblock, true, v, Functions.CH, 0x5a827999, 5);
      fiveCycle(msgblock, true, v, Functions.CH, 0x5a827999, 10);
      oneCycle(v, 0, 1, 2, 3, 4, Functions.CH, 0x5a827999,
          hf(msgblock, 15, true));

      oneCycle(v, 4, 0, 1, 2, 3, Functions.CH, 0x5a827999,
          hf(msgblock, 16, false));
      oneCycle(v, 3, 4, 0, 1, 2, Functions.CH, 0x5a827999,
          hf(msgblock, 17, false));
      oneCycle(v, 2, 3, 4, 0, 1, Functions.CH, 0x5a827999,
          hf(msgblock, 18, false));
      oneCycle(v, 1, 2, 3, 4, 0, Functions.CH, 0x5a827999,
          hf(msgblock, 19, false));

      fiveCycle(msgblock, false, v, Functions.PARITY, 0x6ed9eba1, 20);
      fiveCycle(msgblock, false, v, Functions.PARITY, 0x6ed9eba1, 25);
      fiveCycle(msgblock, false, v, Functions.PARITY, 0x6ed9eba1, 30);
      fiveCycle(msgblock, false, v, Functions.PARITY, 0x6ed9eba1, 35);

      fiveCycle(msgblock, false, v, Functions.MAJ, 0x8f1bbcdc, 40);
      fiveCycle(msgblock, false, v, Functions.MAJ, 0x8f1bbcdc, 45);
      fiveCycle(msgblock, false, v, Functions.MAJ, 0x8f1bbcdc, 50);
      fiveCycle(msgblock, false, v, Functions.MAJ, 0x8f1bbcdc, 55);

      fiveCycle(msgblock, false, v, Functions.PARITY, 0xca62c1d6, 60);
      fiveCycle(msgblock, false, v, Functions.PARITY, 0xca62c1d6, 65);
      fiveCycle(msgblock, false, v, Functions.PARITY, 0xca62c1d6, 70);
      fiveCycle(msgblock, false, v, Functions.PARITY, 0xca62c1d6, 75);

      digest[0] += v[0];
      digest[1] += v[1];
      digest[2] += v[2];
      digest[3] += v[3];
      digest[4] += v[4];
    }
  }
}
