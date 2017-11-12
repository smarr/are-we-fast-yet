////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 
// UTS	-- Unbalanced UTS Search Benchmark
// Source: http://www.csm.ornl.gov/essc/x10/x10/
// Primary Algorithm: Sequential, Recursive Depth First
// 
// Comments:
// 
// Despite this being a tree search, there is no stored "tree", per se,
// traversed by the search. Rather, the tree nodes are generated on the fly by
// identifying each node with a random number in a very large sequence. Thus at
// any given time, only a small number of nodes are "live" and thus retained
// in memory. This allows for very large tree searches to be configured and
// tested even with limit memory.
// 
// Since there is no "tree" and no parent-child pointers, the "link" from parent
// to child is obtained by creating the identifier for each chi ld based on
// the identifier of the parent. The parent's identifier is the state of a
// split-able random number generator.  The parent's children are obtained by
// reseeding the random number generator using the parent's local identifier
// for each child and the parent's node idetifier.
// 
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.lang.Math;
import java.util.concurrent.atomic.AtomicInteger;

public class UTS {
	// TREE TYPE AND SHAPE CONSTANTS
	static final int BIN    = 0;				// TYPE: binomial tree
	static final int GEO    = 1;				// TYPE: geometric tree
	static final int HYBRID = 2;				// TYPE: hybrid tree, start geometric, shift to binomial
	static final int LINEAR = 0;				// SHAPE: linearly decreasing geometric tree
	static final int EXPDEC = 1;				// SHAPE: exponentially decreasing geometric tree
	static final int CYCLIC = 2;				// SHAPE: cyclic geometric tree
	static final int FIXED  = 3;				// SHAPE: fixed branching factor geometric tree
	static final int    UNSETI =-1;				// sentinel for unset integer values
	static final double UNSETD =-1.0;			// sentinel for unset double values
	static final String TREES[]  = {"Binomial","Geometric","Hybrid" };
	static final String SHAPES[] = {"Linear decrease","Exponential decrease","Cyclic","Fixed branching factor"};
	// UTS parameters and defaults
	private int     treetype     = GEO;			// UTS Type: Default = GEO
	private double  b_0          = 4.0;			// branching factor for root node
	private int     rootId       = 0;			// RNG seed for root node
	private int     nonLeafBF    = 4;			// BINOMIAL TREE: branching factor for nonLeaf nodes
	private double  nonLeafProb  = 15.0/64.0;		// BINOMIAL TREE: probability a node is a nonLeaf
	private int     gen_mx       = 6;			// GEOMETRIC TREE: maximum number of generations
	private int     shape_fn     = LINEAR;			// GEOMETRIC TREE: shape function: Default = LINEAR
	private double  shiftDepth   = 0.5;			// HYBRID TREE: Depth fraction for shift from GEO to BIN
	private int     computeGran  = 1;			// number of RNG evaluations per tree node
	private Node	root;
	// output parameters
	private int     debug        = 0;			// debug level
	private int     verbose      = 1;			// output verbosity level
	private int     stats        = 0;			// keep stats 
	// UTS Performance Statistics
	private double  searchTimer  = 0;			// search timer
	private AtomicInteger 	  nNodes;				// total number of nodes discovered in tree
	private AtomicInteger     nLeaves;			// total number of leafnodes discovered in tree
	private AtomicInteger     maxUTSDepth;		// maximum tree depth
	private String type;

	UTS(int tree_type){
		// T1 & T3 used in other papers
		if(tree_type == 2) {
			// T2="-t 1 -a 2 -d 16 -b 6 -r 502"
			rootId = 502;
			treetype = 1;
			shape_fn =2;
			b_0 = 6;
			gen_mx = 16;
			type = "T2";
		}
		else if(tree_type == 3) {
			// T3="-t 0 -b 2000 -q 0.124875 -m 8 -r 42"
//			rootId = 42;
//			nonLeafProb = 0.124875;
//			nonLeafBF = 8;
//			treetype = 0;
//			b_0 = 2000;
			type = "T3";
			System.out.println("WARNING: UTS(T3) does not works on JikesRVM");
		}
		else if(tree_type == 4) {
			// T4="-t 2 -a 0 -d 16 -b 6 -r 1 -q 0.234375 -m 4"
			rootId = 1;
			nonLeafProb = 0.234375;
			nonLeafBF = 4;
			treetype = 2;
			shape_fn =0;
			b_0 = 6;
			gen_mx = 16;
			type = "T4";
		}
		else if(tree_type == 5) {
			// T5="-t 1 -a 0 -d 20 -b 4 -r 34"
			rootId = 34;
			treetype = 1;
			shape_fn =0;
			b_0 = 4;
			gen_mx = 20;
			type = "T5";
		}
		else if(tree_type == 1){
			// T1="-t 1 -a 3 -d 10 -b 4 -r 19"
			rootId = 19;
			treetype = 1;
			shape_fn =3;
			b_0 = 4;
			gen_mx = 10;
			type = "T1";
		}
		else if(tree_type == 6) {
			// T1L="-t 1 -a 3 -d 13 -b 4 -r 29"
			rootId = 29;
			treetype = 1;
			shape_fn =3;
			b_0 = 4;
			gen_mx = 13;
			type = "T1L";
		}
		else {
			type = "default";
		}
		nNodes = new AtomicInteger(0);
		nLeaves = new AtomicInteger(0);
		maxUTSDepth = new AtomicInteger(0);
	}

	public static void main(String args[]){
		int tree_type = 0;
		if(args.length > 0) {
			tree_type = Integer.parseInt(args[0]);
			if(tree_type < 0 || tree_type > 6) {
				System.out.println("Unsupported tree type");
				System.exit(-1);
			}
		}
		System.out.println("Tree type = "+tree_type);
		
		int l_start=1;
		int inner = 5;
		int outter = 3;
		if(args.length > l_start) inner = Integer.parseInt(args[l_start]);
		if(args.length > (l_start+1)) outter = Integer.parseInt(args[l_start+1]);
		
		boolean harnessStarted = false;
		final long start = System.nanoTime();
		for(int i=0;i <outter; i++) {
			if(i+1 == outter) {
				harnessStarted = true;
				org.mmtk.plan.Plan.harnessBegin();
				org.jikesrvm.scheduler.RVMThread.perfEventStart();
			}
			for(int j=0; j<inner; j++) {
				System.out.println("========================== ITERATION ("+i+"."+j+") ==================================");
				UTS tree = new UTS(tree_type);
				tree.compute();
				if(harnessStarted) {
					tree.showStats_short();
				}
				org.jikesrvm.scheduler.WS.dumpWSStatistics();
			}
		}

		System.out.println("Test Kernel under harness passed successfully....");
		
		org.jikesrvm.scheduler.RVMThread.perfEventStop();
		org.mmtk.plan.Plan.harnessEnd();

		final double duration = (((double)(System.nanoTime() - start))/((double)(1.0E9))) * 1000;
		System.out.printf("===== Test PASSED in %d msec =====\n",(int)duration);
	}

	public final void compute() {
		initRoot();
		startTimer();
		finish {
			search(root);
		}
		stopTimer();
	}
	
	final void search(Node parent){
		nNodes.incrementAndGet();
		maxUTSDepth.set(Math.max(parent.height, maxUTSDepth.get()));
		int numChildren = parent.numChildren();
		if (numChildren > 0) {
			async {
				for(int i = 0; i < numChildren; i++) {
					core(parent, i);
				}
			}
		} else {
			nLeaves.incrementAndGet();
		}
		return;
	}

	final void core(final Node parent, final int i) {
		search((new Node(parent,i)));
	}

	void initRoot(){
		root = new Node(rootId);
		nNodes.set(0);
		nLeaves.set(0);
		maxUTSDepth.set(0);
	}

	final boolean status() {
		int nNodes = 0;
		int maxUTSDepth = 0;
		int nLeaves = 0;

		if(type.equals("T1")) {
			nNodes = 4130071;
			maxUTSDepth = 10;
			nLeaves = 3305118;
		}
		else if(type.equals("T2")) {
			nNodes = 4117769;
			maxUTSDepth = 81;
			nLeaves = 2342762;
		} 
		else if(type.equals("T3")) {
			nNodes = 1732;
			maxUTSDepth = 6;
			nLeaves = 1050;
		}
		else if(type.equals("T4")) {
			nNodes = 4132453;
			maxUTSDepth = 134;
			nLeaves = 3108986;
		}
		else if(type.equals("T5")) {
			nNodes = 4147582;
			maxUTSDepth = 20;
			nLeaves = 2181318;
		}
		else {
			nNodes = 1732;
			maxUTSDepth = 6;
			nLeaves = 1050;
		}
		
		String s1="", s2="", s3="";
		
		final int nNodes_r = this.nNodes.get();
		final int maxUTSDepth_r = this.maxUTSDepth.get();
		final int nLeaves_r = this.nLeaves.get();
		
		if(nNodes_r != nNodes) {
			s1 = ("Tree Size Expected = " + nNodes + " but Obtained = " + nNodes_r);
		}
		if(maxUTSDepth_r != maxUTSDepth) {
			s2 = (" Tree Depth Expected = " + maxUTSDepth + " but Obtained = " + maxUTSDepth_r);
		}
		if(nLeaves_r != nLeaves) {
			s3 = (" Tree Num_Leaves Expected = " + nLeaves + " but Obtained = " + nLeaves_r);
		}
		
		if(s1.length() == 0 && s2.length() == 0 && s3.length() == 0) {
			return true;
		}
		else {
			System.out.println(s1 + s2 + s3);
			return false; 
		}
	}


	void showStats_short(){
		boolean pass = status();
		System.out.print("UTS ("+type+") : passed="+ pass);
		System.out.print(". Performance = " + (int)((double)nNodes.get()/searchTimer) + " nodes/sec");
		System.out.println(". Time = "+fourdeci(searchTimer)+" secs");
		if(!pass) {
			System.out.println("TEST FAILED UNDER HARNESS... Exiting");
			System.exit(-1);
		}
	}
	
	void showStats(){
		System.out.println("UTS(" + type +") size = " + nNodes.get() + ", tree depth = " + maxUTSDepth.get() + ", num leaves = " + nLeaves.get() + " (" + (100.*fourdeci((double)nLeaves.get()/(double)nNodes.get())) + "%)    "
				+ "Wallclock time = " + fourdeci(searchTimer) + " sec, performance = " + (int)((double)nNodes.get()/searchTimer) + " nodes/sec");
	}
	
	double fourdeci(double val) {
		return  Math.floor(val*10000. + 0.5)/10000.0;
	}
	void startTimer(){
		searchTimer = -(double)System.nanoTime();
	}
	void stopTimer(){
		searchTimer += (double)System.nanoTime();
		searchTimer /= 1.0e+9;
	}
	void showParams(){
		System.out.println("UTS - Unbalanced UTS Search (Parallel implementation)");
		System.out.println("UTS Type:    " + treetype + " (" + TREES[treetype] + ") (="+type+")");
		System.out.println("UTS shape parameters:");
		System.out.println("  root branching factor b_0 = " + b_0 + ", root seed = " + rootId);
		switch (treetype) {
		case BIN:
			System.out.println("  BIN parameters:  q = " + nonLeafProb
					+ ", m = " + nonLeafBF
					+ ", E(n) = " + nonLeafProb*nonLeafBF
					+ ", E(s) = " + (1.0 / (1.0 - nonLeafProb * nonLeafBF)));
			break;
		case GEO:
			System.out.println("  GEO parameters: gen_mx = " + gen_mx
					+ ", shape function = " + shape_fn
					+ "(" + SHAPES[shape_fn]
							+ ")");
			break;
		case HYBRID:
			System.out.println("  GEO parameters: gen_mx = " + gen_mx
					+ ", shape function = " + shape_fn
					+ " (" + SHAPES[shape_fn]
							+ ")");
			System.out.println("  BIN parameters:  q = " + nonLeafProb
					+ ", m = " + nonLeafBF
					+ ", E(n) = " + nonLeafProb*nonLeafBF
					+ ", E(s) = " + (1.0 / (1.0 - nonLeafProb * nonLeafBF)));
			System.out.println("  HYBRID:  GEO from root to depth "
					+ (int) Math.ceil(shiftDepth * gen_mx)
					+ ", then BIN");
			break;
		default:
			break;
		}	
		System.out.println("Compute granularity: " + computeGran);
		System.out.println("Execution strategy: " + getName());
		System.out.println("");
	}
	String getName(){
		return "Sequential Recursive Search";
	}
	String getType(){
		return TREES[treetype];
	}
	String getShape(){
		return SHAPES[shape_fn];
	}

	public class Node{
		// Node State
		SHA1Generator state;
		int           type;
		int           height;
		int           nChildren;
		// misc constants
		static final double TWO_PI = 2.0*Math.PI;
		static final int MAXNUMCHILDREN = 100;   		// max number of children for BIN tree

		Node(int rootID) {					// root constructor: count the nodes as they are created
			state     = new SHA1Generator(rootID);
			type      = treetype;
			height    = 0;
			nChildren = -1;
		}


		Node(Node parent, int spawn) {				// child constructor: count the nodes as they are created
			for(int j=0; j<computeGran; j++) {
				state     = new SHA1Generator(parent.state,spawn);
			}
			type      = parent.childType();
			height    = parent.height + 1;
			nChildren = -1;
		}

		int numChildren(){ // generic
			switch (treetype) {
			case BIN:
				nChildren = numChildren_bin();
				break;
			case GEO:
				nChildren = numChildren_geo();
				break;
			case HYBRID:
				if (height < shiftDepth * gen_mx)
					nChildren = numChildren_geo();
				else
					nChildren = numChildren_bin();
				break;
			default:
				error("Node:numChildren(): Unknown tree type");
			}
			if (height == 0 && type == BIN) {	// only BIN root can have more than MAXNUMCHILDREN
				int rootBF = (int) Math.ceil(b_0);
				if (nChildren > rootBF) {
					System.out.println("*** Number of children of root truncated from "
							+nChildren+" to "+rootBF);
					nChildren = rootBF;
				}
			} else {
				if (nChildren > MAXNUMCHILDREN) {
					System.out.println("*** Number of children truncated from "
							+nChildren+" to "+MAXNUMCHILDREN);
					nChildren = MAXNUMCHILDREN;
				}
			}
			return nChildren;
		}
		int numChildren_bin(){			// Binomial: distribution is identical below root
			int nc;
			if (height == 0)
				nc = (int)Math.floor(b_0);
			else if (rng_toProb(state.rand()) < nonLeafProb) 
				nc = nonLeafBF;
			else 
				nc = 0;
			return nc;
		}
		int numChildren_geo(){			// Geometric: distribution controlled by shape and height
			double b_i = b_0;
			if (height > 0){
				switch (shape_fn) {	// use shape function to compute target b_i
				case EXPDEC:		// expected size polynomial in height
					b_i = b_0*Math.pow((double)height,-Math.log(b_0)/Math.log((double)gen_mx));
					break;
				case CYCLIC:		// cyclic tree
					if (height > 5 * gen_mx) {
						b_i = 0.0;
						break;
					}
					b_i = Math.pow(b_0,Math.sin(TWO_PI*(double)height/(double)gen_mx));
					break;
				case FIXED:		// identical distribution at all nodes up to max height
					b_i = (height < gen_mx)? b_0 : 0;
					break;
				case LINEAR:		// linear decrease in b_i
				default:
					b_i =  b_0 * ( 1.0 - ((double)height/(double)gen_mx) );
					break;
				}
			}
			double p = 1.0 / (1.0 + b_i);			// probability corresponding to target b_i
			int h = state.rand();
			double u = rng_toProb(h);		// get uniform random number on [0,1)
			int nChildren = (int)Math.floor(Math.log(1.0 - u) / Math.log(1.0 - p));
			// return max number of children at this cumulative probability
			return nChildren;
		}
		int childType(){			 // determine what kind of children this node will have
			switch (type) {
			case BIN:	return BIN;
			case GEO:	return GEO;
			case HYBRID:	if (height < shiftDepth * gen_mx)
				return GEO;
			else
				return BIN;
			default:	error("uts_get_childtype(): Unknown tree type");
			return -1;
			}
		}
		double rng_toProb(int n){		 // convert a random number on [0,2^31) to one on [0.1)
			return ((n<0)? 0.0 : ((double) n)/2147483648.0);
		}
		void error(String data){				 // bailout with error message
			System.out.println(data);
			System.exit(1);
		}
	} // Node
} // UTS

final class SHA1Generator {
	// internal constants
	private final static int POS_MASK         = 0x7fffffff;
	private final static int LOWBYTE          = 0xFF;
	private final static int SHA1_DIGEST_SIZE = 20;

	// internal rng state
	private byte[]  state = new byte[SHA1_DIGEST_SIZE];	// 160 bit output representation

	// new rng from seed
	public SHA1Generator(int seedarg) {
		byte[] seedstate = new byte[20];
		for (int i=0; i<16; i++) seedstate[i]=0;
		seedstate[16] = (byte)(LOWBYTE & (seedarg >>> 24));
		seedstate[17] = (byte)(LOWBYTE & (seedarg >>> 16));
		seedstate[18] = (byte)(LOWBYTE & (seedarg >>> 8));
		seedstate[19] = (byte)(LOWBYTE & (seedarg));
		SHA1compiler sha1 = new SHA1compiler();
		sha1.hash(seedstate,20);
		sha1.digest(state);
	}

	// New rng from existing rng
	public SHA1Generator(SHA1Generator parent, int spawnnumber) {
		byte[]  seedstate = new byte[4];
		seedstate[0] = (byte)(LOWBYTE & (spawnnumber >>> 24));
		seedstate[1] = (byte)(LOWBYTE & (spawnnumber >>> 16));
		seedstate[2] = (byte)(LOWBYTE & (spawnnumber >>> 8));
		seedstate[3] = (byte)(LOWBYTE & (spawnnumber));
		SHA1compiler sha1 = new SHA1compiler();
		sha1.hash(parent.state,20);
		sha1.hash(seedstate,4);
		sha1.digest(state);
	}

	// Return next random number
	public final int nextrand() {
		int d;
		SHA1compiler sha1 = new SHA1compiler();
		sha1.hash(state,20);
		sha1.digest(state);
		return POS_MASK & (((LOWBYTE & (int)state[16])<<24) | ((LOWBYTE & (int)state[17])<<16)
				| ((LOWBYTE & (int)state[18])<< 8) | ((LOWBYTE & (int)state[19])));
	}

	// return current random number (no advance)
	public final int rand() {
		int d;
		return POS_MASK & (((LOWBYTE & (int)state[16])<<24) | ((LOWBYTE & (int)state[17])<<16)
				| ((LOWBYTE & (int)state[18])<< 8) | ((LOWBYTE & (int)state[19])));
	}

	// describe the state of the RNG
	public String showstate() {
		String[] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
		String sha1state = "SHA1 state=|";
		for (int i=0; i<20; i++) {
			sha1state += hex[((state[i]>>4)&0x0F)];
			sha1state += hex[((state[i]>>0)&0x0F)];
			sha1state += "|";
		}
		return sha1state;
	}

	// describe the RNG
	public String showtype() {
		return ("SHA-1 160 bits");
	}
}

final class SHA1compiler {
	// internal constants
	private final static int SHA1_DIGEST_SIZE = 20;
	private final static int SHA1_BLOCK_SIZE  = 64;
	private final static int SHA1_MASK        = SHA1_BLOCK_SIZE - 1;
	// internal rng state
	private int[]   digest  = new int[SHA1_DIGEST_SIZE/4];	// 160 bit internal representation
	private int[]   msgblock  = new int[SHA1_BLOCK_SIZE/4];	// 64 byte internal working buffer
	private long    count = 0;         			// 64 bit counter of bytes processed

	SHA1compiler() {
		digest[0] = (int)0x67452301l;
		digest[1] = (int)0xefcdab89l;
		digest[2] = (int)0x98badcfel;
		digest[3] = (int)0x10325476l;
		digest[4] = (int)0xc3d2e1f0l;
	}

	public final void hash(byte[] data, int length) {
		int bp    = 0;				// byte position in data[]
		int pos   = (int)(count & SHA1_MASK);	// byte position in msgblock
		int wpos  = pos>>>2;			// word position in msgblock
		int space = SHA1_BLOCK_SIZE - pos;	// bytes left in msgblock
		int len   = length;			// number of bytes left to process in data
		count     += len;			// total number of bytes processed since begin
		while(len >= space) {
			for(; wpos < (SHA1_BLOCK_SIZE>>>2); bp+=4) {	// "int" aligned (byte)memory to (int)memory copy
				msgblock[wpos++] = (((int)data[bp  ]&0xFF)<<24) | (((int)data[bp+1]&0xFF)<<16) | (((int)data[bp+2]&0xFF)<< 8) | ((int)data[bp+3]&0xFF) ;
			}
			compile();
			len -= space;
			space = SHA1_BLOCK_SIZE;
			wpos = 0;
		}
		for(; bp < length; bp+=4) {		// this is the "int" aligned (byte)memory to (int)memory copy
			msgblock[wpos++] = (((int)data[bp  ]&0xFF)<<24) | (((int)data[bp+1]&0xFF)<<16)
					| (((int)data[bp+2]&0xFF)<< 8) | (((int)data[bp+3]&0xFF));
		}
	}

	public final void digest(byte[] output) {
		int    i = (int)(count & SHA1_MASK);	// how many bytes already in msgblock[]?
		msgblock[i >> 2] &= (int)(0xffffff80l << 8 * (~i & 3));
		msgblock[i >> 2] |= (int)(0x00000080l << 8 * (~i & 3));
		if(i > SHA1_BLOCK_SIZE - 9) {
			if(i < 60) msgblock[15] = 0;
			compile();
			i = 0;
		} else {
			i = (i >> 2) + 1;
		}
		while(i < 14) msgblock[i++] = 0;
		msgblock[14] = (int)((count >> 29));
		msgblock[15] = (int)((count << 3));
		compile(); // THIS call accounts for 50% of the program execution time...
		for(i = 0; i < SHA1_DIGEST_SIZE; ++i) output[i] = (byte)(digest[i >> 2] >> (8 * (~i & 3)));
	}

	private static final int rotl32(final int x, final int n) {
		return ((x) << n) | ((x) >>> (32 - n));
	}

	private static final int rotr32(final int x, final int n) {
		return ((x) >>> n) | ((x) << (32 - n));
	}

	private static final int bswap_32(final int x) {
		return ((rotr32((x), 24) & (int)0x00ff00ff) | (rotr32((x), 8) & (int)0xff00ff00));
	}

	private static final void bsw_32(final int[] p, final int n) {
		int _i = n;
		while(_i-- != 0) {
			p[_i] = bswap_32(p[_i]);
		}
	}

	private static final int ch(final int x, final int y, final int z) {
		return ((z) ^ ((x) & ((y) ^ (z))));
	}

	private static final int parity(final int x, final int y, final int z) {
		return ((x) ^ (y) ^ (z));
	}

	private static final int maj(final int x, final int y, final int z) {
		return (((x) & (y)) | ((z) & ((x) ^ (y))));
	}

	private static final int hf(final int[] w, final int i, final boolean hf_basic) {
		if(hf_basic) {
			return w[i];
		}
		else {
			int x = i & 15;
			w[x] = rotl32(w[((i) + 13) & 15] ^ w[((i) + 8) & 15] ^ w[((i) +  2) & 15] ^ w[(i) & 15], 1);
			return w[x];
		}
	}

	private static final void one_cycle(final int[] v, final int a, final int b, final int c, 
			final int d, final int e, final String f, final int k, final int h) {

		if(f.equals("ch")) {
			v[e] += (rotr32(v[a],27) + ch(v[b],v[c],v[d]) + k + h);
		}
		else if(f.equals("maj")) {
			v[e] += (rotr32(v[a],27) + maj(v[b],v[c],v[d]) + k + h);
		}
		else if(f.equals("parity")) {
			v[e] += (rotr32(v[a],27) + parity(v[b],v[c],v[d]) + k + h);
		}
		else {
			System.out.println("one_cycle(): error as unknown function type -->"+f);
			System.exit(-1);
		}
		v[b] = rotr32(v[b], 2);
	}

	private static final void five_cycle(final int[] w, final boolean hf_basic, final int[] v, 
			final String f, final int k, final int i) {
		one_cycle(v, 0,1,2,3,4, f,k,hf(w, i, hf_basic));
		one_cycle(v, 4,0,1,2,3, f,k,hf(w, i+1, hf_basic));
		one_cycle(v, 3,4,0,1,2, f,k,hf(w, i+2, hf_basic));
		one_cycle(v, 2,3,4,0,1, f,k,hf(w, i+3, hf_basic));
		one_cycle(v, 1,2,3,4,0, f,k,hf(w, i+4, hf_basic));
	}

	private final void compile() {  
		int    v0, v1, v2, v3, v4;
		int[] v = new int[5];
		System.arraycopy(digest, 0, v, 0, 5);

		five_cycle(msgblock, true, v, "ch", (int)0x5a827999,  0);
		five_cycle(msgblock, true, v, "ch", (int)0x5a827999,  5);
		five_cycle(msgblock, true, v, "ch", (int)0x5a827999, 10);
		one_cycle(v,0,1,2,3,4, "ch", (int)0x5a827999, hf(msgblock, 15, true)); 

		one_cycle(v,4,0,1,2,3, "ch", (int)0x5a827999, hf(msgblock, 16, false));
		one_cycle(v,3,4,0,1,2, "ch", (int)0x5a827999, hf(msgblock, 17, false));
		one_cycle(v,2,3,4,0,1, "ch", (int)0x5a827999, hf(msgblock, 18, false));
		one_cycle(v,1,2,3,4,0, "ch", (int)0x5a827999, hf(msgblock, 19, false));

		five_cycle(msgblock, false, v, "parity", (int)0x6ed9eba1,  20);
		five_cycle(msgblock, false, v, "parity", (int)0x6ed9eba1,  25);
		five_cycle(msgblock, false, v, "parity", (int)0x6ed9eba1,  30);
		five_cycle(msgblock, false, v, "parity", (int)0x6ed9eba1,  35);

		five_cycle(msgblock, false, v, "maj", (int)0x8f1bbcdc,  40);
		five_cycle(msgblock, false, v, "maj", (int)0x8f1bbcdc,  45);
		five_cycle(msgblock, false, v, "maj", (int)0x8f1bbcdc,  50);
		five_cycle(msgblock, false, v, "maj", (int)0x8f1bbcdc,  55);

		five_cycle(msgblock, false, v, "parity", (int)0xca62c1d6,  60);
		five_cycle(msgblock, false, v, "parity", (int)0xca62c1d6,  65);
		five_cycle(msgblock, false, v, "parity", (int)0xca62c1d6,  70);
		five_cycle(msgblock, false, v, "parity", (int)0xca62c1d6,  75);
		digest[0] += v[0];
		digest[1] += v[1];
		digest[2] += v[2];
		digest[3] += v[3];
		digest[4] += v[4];
	}

	static String toHex(int data) {
		String result = java.lang.Integer.toHexString(data);
		for (int i = result.length(); i<8 ; i++) result ="0"+result;
		return result;
	}
	static String toHex(long data) {
		String result = java.lang.Long.toHexString(data);
		for (int i = result.length(); i<16 ; i++) result ="0"+result;
		return result;
	}
	private void showdigest() {
		for (int i=0; i<5; i++) System.out.print(toHex(digest[i])+" ");
		System.out.println(" ");
	}
	private void showmsgblock() {
		for (int i=0; i<16; i++) System.out.print(toHex(msgblock[i])+" ");
		System.out.println(" ");
	}
}
