package som;

public class Random {
	private int seed = 74755;

	public int next() {
		seed = ((seed * 1309) + 13849) & 65535;
		return seed;
	}

	public static void main(final String[] args) {
		System.out.println("Testing random number generator ...");
		Random rnd = new Random();

		try {
			if (rnd.next() != 22896) { throw new RuntimeException(); }
			if (rnd.next() != 34761) { throw new RuntimeException(); }
			if (rnd.next() != 34014) { throw new RuntimeException(); }
			if (rnd.next() != 39231) { throw new RuntimeException(); }
			if (rnd.next() != 52540) { throw new RuntimeException(); }
			if (rnd.next() != 41445) { throw new RuntimeException(); }
			if (rnd.next() !=  1546) { throw new RuntimeException(); }
			if (rnd.next() !=  5947) { throw new RuntimeException(); }
			if (rnd.next() != 65224) { throw new RuntimeException(); }
		} catch (RuntimeException e) {
			System.err.println("FAILED");
			return;
		}
	}
}
