/*
 * BSD License
 *
 * Copyright (c) 2007, The University of Manchester (UK)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     - Neither the name of the University of Manchester nor the names
 *       of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leetm;


/*
 * Grid.java
 *
 * Created on 11 April 2007, 23:02
 *
 * Represents the PCB on which tracks are laid
 *
 */
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

/**
 *
 * @author Mohammad Ansari
 */
public class Grid {

	// Simple grid attributes and container
	private final int width, height, depth;

	private final GridCell[][][] grid;

	// Variables used in debugging only
	private final GridCell[][][] debugGrid;

	private static int debugCount = 0;

	private static int divisor = 100000;

	/** Creates a new instance of Grid */
	public Grid(int gridWidth, int gridHeight, int gridDepth, boolean rel) {
		// Set up PCB grid
		width = gridWidth;
		height = gridHeight;
		depth = gridDepth;

		grid = new GridCell[width][height][depth];
		instantiateGrid(grid);
		resetGrid(grid);

		if (LeeRouter.DEBUG) {
			debugGrid = new GridCell[width][height][depth];
			instantiateGrid(debugGrid);
			resetGrid(debugGrid);
		} else {
			debugGrid = null;
		}
	}

	boolean isValidTrackID(int i) {
		return !(i == LeeRouter.EMPTY || i == LeeRouter.OCC);
	}

	public void occupy(int loX, int loY, int upX, int upY) {
		int x = 0;
		int y = 0;
		for (x = loX; x <= upX; x++) {
			for (y = loY; y <= upY; y++) {
				for (int z = 0; z < depth; z++) {
					grid[x][y][z].setRouteID(LeeRouter.OCC);
					if (LeeRouter.DEBUG)
						debugGrid[x][y][z].setRouteID(LeeRouter.OCC);
				}
			}
		}
	}

	public void addweights() {
		for (int i = 0; i < LeeRouter.MAX_WEIGHT; i++)
			for (int z = 0; z < depth; z++)
				for (int x = 1; x < width - 1; x++)
					for (int y = 1; y < height - 1; y++)
						if (grid[x][y][z].getRouteID() == LeeRouter.OCC) {
							if (grid[x][y + 1][z].getRouteID() == LeeRouter.EMPTY)
								grid[x][y + 1][z]
										.setRouteID(LeeRouter.MAX_WEIGHT);
							if (grid[x + 1][y][z].getRouteID() == LeeRouter.EMPTY)
								grid[x + 1][y][z]
										.setRouteID(LeeRouter.MAX_WEIGHT);
							if (grid[x][y - 1][z].getRouteID() == LeeRouter.EMPTY)
								grid[x][y - 1][z]
										.setRouteID(LeeRouter.MAX_WEIGHT);
							if (grid[x - 1][y][z].getRouteID() == LeeRouter.EMPTY)
								grid[x - 1][y][z]
										.setRouteID(LeeRouter.MAX_WEIGHT);
						} else if (grid[x][y][z].getRouteID() != LeeRouter.EMPTY) {
							if (grid[x][y + 1][z].getRouteID() == LeeRouter.EMPTY)
								grid[x][y + 1][z].setRouteID(grid[x][y][z]
										.getRouteID() - 1);
							if (grid[x + 1][y][z].getRouteID() == LeeRouter.EMPTY)
								grid[x + 1][y][z].setRouteID(grid[x][y][z]
										.getRouteID() - 1);
							if (grid[x][y - 1][z].getRouteID() == LeeRouter.EMPTY)
								grid[x][y - 1][z].setRouteID(grid[x][y][z]
										.getRouteID() - 1);
							if (grid[x - 1][y][z].getRouteID() == LeeRouter.EMPTY)
								grid[x - 1][y][z].setRouteID(grid[x][y][z]
										.getRouteID() - 1);
						}
	}

	public void printLayout(boolean toFile) {
		String fileName = "output.txt";
		PrintStream ps;

		try {
			if (toFile)
				ps = new PrintStream(new FileOutputStream(fileName));
			else
				ps = System.out;
			for (int k = 0; k < depth; k++) {
				for (int j = 0; j < height; j++) {
					for (int i = 0; i < width; i++) {
						if (!isValidTrackID(grid[i][j][k].getRouteID())) {
							ps.print(".");
						} else {
							if (LeeRouter.DEBUG)
								ps.print(debugGrid[i][j][k].getRouteID());
							else
								ps.print("X");
						}
					}
					ps.println();
				}
				ps.println();
			}
		} catch (FileNotFoundException exception) {
			System.out.println("Cannot open file: " + fileName);
			System.exit(1);
		}
	}

	public boolean findTrack(int x1, int y1, int x2, int y2, int nn) {

		int x = x1;
		int y = y1;
		int z = 0;
		boolean found = false;
		LinkedList<Triplet> trackSoFar = new LinkedList<Triplet>();

		trackSoFar.addFirst(new Triplet(x, y, z));

		// Start search
		while (true) {
			x = trackSoFar.getFirst().val1;
			y = trackSoFar.getFirst().val2;
			z = trackSoFar.getFirst().val3;
			// See if there is a surrounding cell with same id, but hasn't been
			// visited yet
			if (isNeighbouringCellNN(x, y, z, nn, trackSoFar))
				continue;
			else if (isNeighbouringCellNN(x, y, z, LeeRouter.OCC, trackSoFar))
				continue;

			// No node with same id found, check if we are at dest and exit
			if (x == x2 && y == y2) {
				found = true;
			}
			break;
		}
		return found;
	}

	private boolean isNeighbouringCellNN(int x, int y, int z, int nn,
			LinkedList<Triplet> trackSoFar) {
		boolean retval = false;
		if (x + 1 < width && debugGrid[x + 1][y][z].getRouteID() == nn
				&& !trackSoFar.contains(new Triplet(x + 1, y, z))) {
			trackSoFar.addFirst(new Triplet(x + 1, y, z));
			x = x + 1;
			retval = true;
		} else if (y + 1 < height && debugGrid[x][y + 1][z].getRouteID() == nn
				&& !trackSoFar.contains(new Triplet(x, y + 1, z))) {
			trackSoFar.addFirst(new Triplet(x, y + 1, z));
			y = y + 1;
			retval = true;
		} else if (z + 1 < depth && debugGrid[x][y][z + 1].getRouteID() == nn
				&& !trackSoFar.contains(new Triplet(x, y, z + 1))) {
			trackSoFar.addFirst(new Triplet(x, y, z + 1));
			z = z + 1;
			retval = true;
		} else if (x - 1 >= 0 && debugGrid[x - 1][y][z].getRouteID() == nn
				&& !trackSoFar.contains(new Triplet(x - 1, y, z))) {
			trackSoFar.addFirst(new Triplet(x - 1, y, z));
			x = x - 1;
			retval = true;
		} else if (y - 1 >= 0 && debugGrid[x][y - 1][z].getRouteID() == nn
				&& !trackSoFar.contains(new Triplet(x, y - 1, z))) {
			trackSoFar.addFirst(new Triplet(x, y - 1, z));
			y = y - 1;
			retval = true;
		} else if (z - 1 >= 0 && debugGrid[x][y][z - 1].getRouteID() == nn
				&& !trackSoFar.contains(new Triplet(x, y, z - 1))) {
			trackSoFar.addFirst(new Triplet(x, y, z - 1));
			z = z - 1;
			retval = true;
		}

		return retval;

	}

	private class Triplet {
		int val1;

		int val2;

		int val3;

		Triplet(int v1, int v2, int v3) {
			val1 = v1;
			val2 = v2;
			val3 = v3;
		}

		@Override
		public boolean equals(Object o) {
			Triplet t = (Triplet) o;
			return (t.val1 == val1) && (t.val2 == val2) && (t.val3 == val3);

		}
	}

	public int getPoint(int x, int y, int z) {
		GridCell retCell = grid[x][y][z];
		int ret = retCell.getRouteID();
		return ret;
	}

	public int getDebugPoint(int x, int y, int z) {
		GridCell retCell = debugGrid[x][y][z];
		int ret = retCell.getRouteID();
		return ret;
	}

	public void setPoint(int x, int y, int z, int val) {
		grid[x][y][z].setRouteID(val);
	}

	public void setDebugPoint(int x, int y, int z, int val) {
		debugGrid[x][y][z].setRouteID(val);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDepth() {
		return depth;
	}

	public void resetGrid(GridCell[][][] g) {
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				for (int k = 0; k < depth; k++)
					g[i][j][k].setRouteID(LeeRouter.EMPTY);
	}

	private void instantiateGrid(GridCell[][][] g) {
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				for (int k = 0; k < depth; k++) {
					g[i][j][k] = new GridCell();
					if (LeeRouter.DEBUG) {
						if (debugCount++ == divisor) {
							System.out.println(debugCount);
							debugCount = 0;
						}
					}
				}
	}

}
