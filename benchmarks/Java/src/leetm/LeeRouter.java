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

//Simple Lee's Routing Algorithm
//Author: IW
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class LeeRouter {
    final static int cyan = 0x00FFFF;

    public static final long MAX_SAMPLE_THRESHOLD = 60000;

    final static int magenta = 0xFF00FF;

    final static int yellow = 0xFFFF00;

    final static int green = 0x00FF00;

    final static int red = 0xFF0000;

    final static int blue = 0x0000FF;

    final int GRID_SIZE;

    final static int EMPTY = 0;

    final static int TEMP_EMPTY = 10000;

    final static int OCC = 5120;

    final static int VIA = 6000;

    final static int BVIA = 6001;

    final static int TRACK = 8192;

    final static int GOAL = 1024;

    final static int MAX_WEIGHT = 1;

    final Grid grid;

    final Object gridLock = new Object();

    private static Document doc;

    static int netNo = 0;

//	private static int printDest = 0; // 1 for file, 0 for screen.

    // note these very useful arrays
    final static int dx[][] = { { -1, 1, 0, 0 }, { 0, 0, -1, 1 } };

    // to help look NSEW.
    final static int dy[][] = { { 0, 0, -1, 1 }, { -1, 1, 0, 0 } };

    static Viewer view;

    static int failures = 0;

    static int num_vias = 0;

    static int forced_vias = 0;

    static BufferedReader inputFile;

    static String input_line;

    static int linepos = 0;

    final Object queueLock = new Object();

    final WorkQueue work;

    final WorkQueue debugQueue;

    public static boolean TEST = true;

    public static boolean DEBUG = false;

    private static final boolean XML_REPORT = true;


    public LeeRouter(String file, boolean test, boolean debug, boolean rel) {
        TEST = test;
        DEBUG = debug;
        if(DEBUG) view = new Viewer();
        if (TEST) GRID_SIZE = 10;
        else GRID_SIZE = 600;
        if(DEBUG) System.out.println("Creating grid...");
        grid = new Grid(GRID_SIZE, GRID_SIZE, 2, rel); //the Lee 3D Grid;
        if(DEBUG) System.out.println("Done creating grid");
        work = new WorkQueue(); // empty
        if(DEBUG) System.out.println("Parsing data...");
        if (!TEST) parseDataFile(file);
        else fakeTestData(); //WARNING: Needs grid at least 10x10x2
        if(DEBUG) System.out.println("Done parsing data");
        if(DEBUG) System.out.println("Adding weights...");
        grid.addweights();
        if(DEBUG) System.out.println("Done adding weights");
        work.sort();
        if(DEBUG)
            debugQueue = new WorkQueue();
        else
            debugQueue = null;

    }

    public LeeRouter(String file) {
        this(file, false, false, false);
    }

    public LeeRouter(String file, boolean rel) {
        this(file, false, false, rel);
    }

    private void fakeTestData() {
        netNo++;
        grid.occupy(7, 3, 7, 3);grid.occupy(7, 7, 7, 7);
        work.next = work.enQueue(7, 3, 7, 7, netNo);

        netNo++;
        grid.occupy(3, 6, 3, 6);grid.occupy(8, 6, 8, 6);
        work.next = work.enQueue(3, 6, 8, 6, netNo);

        netNo++;
        grid.occupy(5, 3, 5, 3);grid.occupy(8, 5, 8, 5);
        work.next = work.enQueue(5, 3, 8, 5, netNo);

        netNo++;
        grid.occupy(8, 3, 8, 3);grid.occupy(2, 6, 2, 6);
        work.next = work.enQueue(8, 3, 2, 6, netNo);

        netNo++;
        grid.occupy(4, 3, 4, 3);grid.occupy(6, 7, 6, 7);
        work.next = work.enQueue(4, 3, 6, 7, netNo);

        netNo++;
        grid.occupy(3, 8, 3, 8);grid.occupy(8, 3, 8, 3);
        work.next = work.enQueue(3, 8, 8, 3, netNo);
    }

    private void parseDataFile(String fileName) {
        // Read very simple HDL file
        try {

            inputFile = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName)));
            int i = 0;
            while (true) {
                nextLine();
                char c = readChar();
                if (c == 'E')
                    break; // end of file
                if (c == 'C') // chip bounding box
                {
                    int x0 = readInt();
                    int y0 = readInt();
                    int x1 = readInt();
                    int y1 = readInt();
                    grid.occupy(x0, y0, x1, y1);
                }
                if (c == 'P') // pad
                {
                    int x0 = readInt();
                    int y0 = readInt();
                    grid.occupy(x0, y0, x0, y0);
                }
                if (c == 'J') // join connection pts
                {
                    i++;
                    int x0 = readInt();
                    int y0 = readInt();
                    int x1 = readInt();
                    int y1 = readInt();
                    netNo++;
                    work.next = work.enQueue(x0, y0, x1, y1, netNo);
                }
            }
        } catch (FileNotFoundException exception) {
            System.out.println("Cannot open file: " + fileName);
            System.exit(1);
        } catch (IOException exception) {
            System.out.println(exception);
            exception.printStackTrace();
        }

    }

    public WorkQueue getNextTrack() {
        synchronized(queueLock) {
            if(work.next != null) {
                return work.deQueue();
            }
        }
        return null;
    }

    public boolean layNextTrack(WorkQueue q, int [][][]tempg) {
        // start transaction
        boolean done = false;
        synchronized(gridLock) {
            done = connect(q.x1, q.y1, q.x2, q.y2, q.nn, tempg, grid);
            if(DEBUG && done) {
                debugQueue.next = debugQueue.enQueue(q);
            }
        }
        return done;
        // end transaction
    }

    private static void nextLine() throws IOException {
        input_line = inputFile.readLine();
        linepos = 0;
    }

    private static char readChar() {
        while ((input_line.charAt(linepos) == ' ')
        && (input_line.charAt(linepos) == '\t'))
            linepos++;
        char c = input_line.charAt(linepos);
        if (linepos < input_line.length() - 1)
            linepos++;
        return c;
    }

    private static int readInt() {
        while ((input_line.charAt(linepos) == ' ')
        || (input_line.charAt(linepos) == '\t'))
            linepos++;
        int fpos = linepos;
        while ((linepos < input_line.length())
        && (input_line.charAt(linepos) != ' ')
        && (input_line.charAt(linepos) != '\t'))
            linepos++;
        int n = Integer.parseInt(input_line.substring(fpos, linepos));
        return n;
    }

    public boolean ok(int x, int y) {
        // checks that point is actually within the bounds
        // of grid array
        return (x > 0 && x < GRID_SIZE - 1 && y > 0 && y < GRID_SIZE - 1);
    }

    public boolean expandFromTo(int x, int y, int xGoal, int yGoal,
            int num, int tempg[][][], Grid grid) {
        // this method should use Lee's expansion algorithm from
        // coordinate (x,y) to (xGoal, yGoal) for the num iterations
        // it should return true if the goal is found and false if it is not
        // reached within the number of iterations allowed.

        // g[xGoal][yGoal][0] = EMPTY; // set goal as empty
        // g[xGoal][yGoal][1] = EMPTY; // set goal as empty
        Vector<Frontier> front = new Vector<Frontier>();
        Vector<Frontier> tmp_front = new Vector<Frontier>();
        tempg[x][y][0] = 1; // set grid (x,y) as 1
        tempg[x][y][1] = 1; // set grid (x,y) as 1
        boolean trace1 = false;
        front.addElement(new Frontier(x, y, 0, 0));
        front.addElement(new Frontier(x, y, 1, 0)); // we can start from either
        // side
        if(DEBUG) System.out.println("Expanding " + x + " " + y + " " + xGoal + " "
                + yGoal);
        int extra_iterations = 50;
        boolean reached0 = false;
        boolean reached1 = false;
        while (!front.isEmpty()) {
            while (!front.isEmpty()) {
                int weight, prev_val;
                Frontier f = front.elementAt(0);
                front.removeElementAt(0);
                if (f.dw > 0) {
                    tmp_front.addElement(new Frontier(f.x, f.y, f.z, f.dw - 1));
                } else {
                    if (trace1)
                        if(DEBUG)
                            System.out.println("X " + f.x + " Y " + f.y + " Z "
                                    + f.z + " DW " + f.dw + " processing - val "
                                    + tempg[f.x][f.y][f.z]);
//					int dir_weight = 1;
                    weight = grid.getPoint(f.x,f.y + 1,f.z) + 1;
                    prev_val = tempg[f.x][f.y + 1][f.z];
                    boolean reached = (f.x == xGoal) && (f.y + 1 == yGoal);
                    if ((prev_val > tempg[f.x][f.y][f.z] + weight)
                    && (weight < OCC) || reached) {
                        if (ok(f.x, f.y + 1)) {
                            tempg[f.x][f.y + 1][f.z] = tempg[f.x][f.y][f.z]
                                    + weight; // looking north
                            if (!reached)
                                tmp_front.addElement(new Frontier(f.x, f.y + 1,
                                        f.z, 0));
                        }
                    }
                    weight = grid.getPoint(f.x + 1,f.y,f.z) + 1;
                    prev_val = tempg[f.x + 1][f.y][f.z];
                    reached = (f.x + 1 == xGoal) && (f.y == yGoal);
                    if ((prev_val > tempg[f.x][f.y][f.z] + weight)
                    && (weight < OCC) || reached) {
                        if (ok(f.x + 1, f.y)) {
                            tempg[f.x + 1][f.y][f.z] = tempg[f.x][f.y][f.z]
                                    + weight; // looking east
                            if (!reached)
                                tmp_front.addElement(new Frontier(f.x + 1, f.y,
                                        f.z, 0));
                        }
                    }
                    weight = grid.getPoint(f.x,f.y - 1,f.z) + 1;
                    prev_val = tempg[f.x][f.y - 1][f.z];
                    reached = (f.x == xGoal) && (f.y - 1 == yGoal);
                    if ((prev_val > tempg[f.x][f.y][f.z] + weight)
                    && (weight < OCC) || reached) {
                        if (ok(f.x, f.y - 1)) {
                            tempg[f.x][f.y - 1][f.z] = tempg[f.x][f.y][f.z]
                                    + weight; // looking south
                            if (!reached)
                                tmp_front.addElement(new Frontier(f.x, f.y - 1,
                                        f.z, 0));
                        }
                    }
                    weight = grid.getPoint(f.x - 1,f.y,f.z) + 1;
                    prev_val = tempg[f.x - 1][f.y][f.z];
                    reached = (f.x - 1 == xGoal) && (f.y == yGoal);
                    if ((prev_val > tempg[f.x][f.y][f.z] + weight)
                    && (weight < OCC) || reached) {
                        if (ok(f.x - 1, f.y)) {
                            tempg[f.x - 1][f.y][f.z] = tempg[f.x][f.y][f.z]
                                    + weight; // looking west
                            if (!reached)
                                tmp_front.addElement(new Frontier(f.x - 1, f.y,
                                        f.z, 0));
                        }
                    }
                    if (f.z == 0) {
                        weight = grid.getPoint(f.x,f.y,1) + 1;
                        if ((tempg[f.x][f.y][1] > tempg[f.x][f.y][0])
                        && (weight < OCC)) {
                            tempg[f.x][f.y][1] = tempg[f.x][f.y][0];
                            tmp_front.addElement(new Frontier(f.x, f.y, 1, 0));
                        }
                    } else {
                        weight = grid.getPoint(f.x,f.y,0) + 1;
                        if ((tempg[f.x][f.y][0] > tempg[f.x][f.y][1])
                        && (weight < OCC)) {
                            tempg[f.x][f.y][0] = tempg[f.x][f.y][1];
                            tmp_front.addElement(new Frontier(f.x, f.y, 0, 0));
                        }
                    }
                    // must check if found goal, if so return TRUE
                    reached0 = tempg[xGoal][yGoal][0] != TEMP_EMPTY;
                    reached1 = tempg[xGoal][yGoal][1] != TEMP_EMPTY;
                    if ((reached0 && !reached1) || (!reached0 && reached1))
                        extra_iterations = 100;
                    if ((extra_iterations == 0) && (reached0 || reached1)
                    || (reached0 && reached1)) {
                        return true; // if (xGoal, yGoal) can be found in
                        // time
                    } else
                        extra_iterations--;
                }
            }
            Vector<Frontier> tf;
            tf = front;
            front = tmp_front;
            tmp_front = tf;
        }
//		 view.pad(x,y,red);
//		 view.pad(xGoal,yGoal,red);
        return false;
    }

    private boolean pathFromOtherSide(int[][][] g, int X, int Y, int Z) {
        boolean ok;
        int Zo;
        Zo = 1 - Z; // other side
        int sqval = g[X][Y][Zo];
        if ((sqval == VIA) || (sqval == BVIA))
            return false;
        ok = (g[X][Y][Zo] <= g[X][Y][Z]);
        if (ok)
            ok = (g[X - 1][Y][Zo] < sqval) || (g[X + 1][Y][Zo] < sqval)
            || (g[X][Y - 1][Zo] < sqval) || (g[X][Y + 1][Zo] < sqval);
        return ok;
    }

    private int tlength(int x1, int y1, int x2, int y2) {
        int sq = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        return (int) Math.sqrt(sq);
    }

//	private static int ratio(int x1, int y1, int x2, int y2) {
//		int xdiff = x2 - x1;
//		int ydiff = y2 - y1;
//		if (xdiff < 0)
//			xdiff = -xdiff;
//		if (ydiff < 0)
//			ydiff = -ydiff;
//		if (xdiff > ydiff) {
//			if (ydiff == 0)
//				return 100000;
//			else
//				return xdiff / ydiff;
//		} else {
//			if (xdiff == 0)
//				return 100000;
//			else
//				return ydiff / xdiff;
//		}
//	}

    private static int deviation(int x1, int y1, int x2, int y2) {
        int xdiff = x2 - x1;
        int ydiff = y2 - y1;
        if (xdiff < 0)
            xdiff = -xdiff;
        if (ydiff < 0)
            ydiff = -ydiff;
        if (xdiff < ydiff)
            return xdiff;
        else
            return ydiff;
    }

    public void backtrackFrom(int xGoal, int yGoal, int xStart,
            int yStart, int trackNo, int[][][] tempg, Grid grid) {
        // this method should backtrack from the goal position (xGoal, yGoal)
        // back to the starting position (xStart, yStart) filling in the
        // grid array g with the specified track number trackNo ( + TRACK).

        // ***
        // CurrentPos = Goal
        // Loop
        // Find dir to start back from current position
        // Loop
        // Keep going in current dir and Fill in track (update currentPos)
        // Until box number increases in this current dir
        // Until back at starting point
        // ***
//		int count = 100;
        if(DEBUG)System.out.println("Track " + trackNo + " backtrack " + "Length "
                + tlength(xStart, yStart, xGoal, yGoal));
//		boolean trace = false;
        int zGoal;
        int distsofar = 0;
        if (Math.abs(xGoal - xStart) > Math.abs(yGoal - yStart))
            zGoal = 0;
        else
            zGoal = 1;
        if (tempg[xGoal][yGoal][zGoal] == TEMP_EMPTY) {
            if(DEBUG) System.out.println("Preferred Layer not reached " + zGoal);
            zGoal = 1 - zGoal;
        }
        int tempY = yGoal;
        int tempX = xGoal;
        int tempZ = zGoal;
        int lastdir = -10;
        while ((tempX != xStart) || (tempY != yStart)) { // PDL: until back

            // at starting point
            boolean advanced = false;
            int mind = 0;
            int dir = 0;
            int min_square = 100000;
            int d;
            for (d = 0; d < 4; d++) { // PDL: Find dir to start back from
                // current position
                if ((tempg[tempX + dx[tempZ][d]][tempY + dy[tempZ][d]][tempZ] < tempg[tempX][tempY][tempZ])
                && (tempg[tempX + dx[tempZ][d]][tempY + dy[tempZ][d]][tempZ] != TEMP_EMPTY)) {
                    if (tempg[tempX + dx[tempZ][d]][tempY + dy[tempZ][d]][tempZ] < min_square) {
                        min_square = tempg[tempX + dx[tempZ][d]][tempY
                                + dy[tempZ][d]][tempZ];
                        mind = d;
                        dir = dx[tempZ][d] * 2 + dy[tempZ][d]; // hashed dir
                        if (lastdir < -2)
                            lastdir = dir;
                        advanced = true;
                    }
                }
            }
            if (advanced)
                distsofar++;
            if(DEBUG)
                System.out.println("Backtracking "+tempX+" "+tempY+" "+tempZ+
                        " "+tempg[tempX][tempY][tempZ]+" "+advanced+" "+mind);
            if (pathFromOtherSide(tempg, tempX, tempY, tempZ)
            && ((mind > 1)
            && // not preferred dir for this layer
                    (distsofar > 15)
                    && (tlength(tempX, tempY, xStart, yStart) > 15) ||
                    // (deviation(tempX,tempY,xStart,yStart) > 3) ||
                    (!advanced && ((grid.getPoint(tempX,tempY,tempZ) != VIA)
                    && (grid.getPoint(tempX,tempY,tempZ) != BVIA))))) {
                int tZ = 1 - tempZ; // 0 if 1, 1 if 0
                int viat;
                if (advanced)
                    viat = VIA;
                else
                    viat = BVIA; // BVIA is nowhere else to go
                // mark via
                tempg[tempX][tempY][tempZ] = viat;
                grid.setPoint(tempX,tempY,tempZ,viat);
                if(DEBUG)grid.setDebugPoint(tempX,tempY,tempZ,trackNo);
                tempZ = tZ;
                // and the other side
                tempg[tempX][tempY][tempZ] = viat;
                grid.setPoint(tempX,tempY,tempZ,viat);
                if(DEBUG)grid.setDebugPoint(tempX,tempY,tempZ,trackNo);
                num_vias++;
                if (!advanced)
                    forced_vias++;
                if (advanced)
                    if(DEBUG)
                        System.out.println("Via " + distsofar + " "
                                + tlength(tempX, tempY, xStart, yStart) + " "
                                + deviation(tempX, tempY, xStart, yStart));
                distsofar = 0;
            } else {
                if (grid.getPoint(tempX,tempY,tempZ) < OCC) {
                    // PDL: fill in track unless connection point
                    grid.setPoint(tempX,tempY,tempZ,TRACK);
                    if(DEBUG)grid.setDebugPoint(tempX,tempY,tempZ,trackNo);
                } else if (grid.getPoint(tempX,tempY,tempZ) == OCC) {
                    if(DEBUG)grid.setDebugPoint(tempX,tempY,tempZ,OCC);
                    if(DEBUG)grid.setDebugPoint(tempX,tempY,1-tempZ,OCC);
                }
                tempX = tempX + dx[tempZ][mind]; // PDL: updating current
                // position on x axis
                tempY = tempY + dy[tempZ][mind]; // PDL: updating current
                // position on y axis
            }
            lastdir = dir;
        }
        if(DEBUG) System.out.println("Track " + trackNo + " completed");
    }

    public boolean connect(int xs, int ys, int xg, int yg, int netNo, int[][][] tempg,
            Grid grid) {
        // calls expandFrom and backtrackFrom to create connection
        // This is the only real change needed to make the program
        // transactional.
        // Instead of using the grid 'in place' to do the expansion, we take a
        // copy
        // but the backtrack writes to the original grid.
        // This is not a correctness issue. The transactions would still
        // complete eventually without it.
        // However the expansion writes are only temporary and do not logically
        // conflict.
        // There is a question as to whether a copy is really necessary as a
        // transaction will anyway create
        // its own copy. if we were then to distinguish between writes not to be
        // committed (expansion) and
        // those to be committed (backtrack), we would not need an explicit
        // copy.
        // Taking the copy is not really a computational(time) overhead because
        // it avoids the grid 'reset' phase
        // needed if we do the expansion in place.
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int z = 0; z < 2; z++)
                    tempg[x][y][z] = TEMP_EMPTY;
            }
        }
        // call the expansion method to return found/not found boolean
        boolean found = expandFromTo(xs, ys, xg, yg, GRID_SIZE * 5, tempg, grid);
        if (found) {
            if(DEBUG) System.out.println("Target (" + xg + ", " + yg + ")... FOUND!");
            backtrackFrom(xg, yg, xs, ys, netNo, tempg, grid); // call the
            // backtrack method
        } // print outcome of expansion method
        else {
            if(DEBUG) System.out.println("Failed to route " + xs + " " + ys + " to " + xg
                    + "  " + yg);
            failures++;
        }
        if(DEBUG) {
            dispGrid(grid, 0); // print the grid to screen
            dispGrid(grid, 1); // print the grid to screen
            view.repaint();
        }
        return found;
    }

    public void dispGrid(Grid g, int z) {
        int laycol;
        if (z==0) laycol = magenta; else laycol=green;
        for (int y = GRID_SIZE-1; y>=0; y--) {
            for (int x = 0; x<GRID_SIZE; x++) {
                int gg = g.getPoint(x, y, z);
                if (gg==OCC) { view.point(x,y,cyan); continue; }
                if (gg==VIA) { view.point(x,y,yellow); continue; }
                if (gg==BVIA) { view.point(x,y,red); continue; }
                if (gg==TRACK) {view.point(x,y,laycol); continue; } //
            }
        }
    }


    public LeeThread createThread() {
        return createThread(0);
    }
    public LeeThread createThread(int which) {
        try {
            LeeThread leeThread = new LeeThread(this);
            return leeThread;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
    }

    public void report() {
        //Open GUI view of PCB
        //view.display();
        //Print the PCB in ASCII, output to file
        //grid.printLayout(true);
        System.out.println("Total Tracks " + netNo + " Failures " + failures
                + " Vias " + num_vias + " Forced Vias " + forced_vias);
    }

    public void sanityCheck() {
        int found = 0, missing = 0;
        // Check debugGrid that the routes in debugQueue have been laid
        if(DEBUG) {
            System.out.println("DEBUG: Starting sanity check");
            while(debugQueue.next!=null) {
                WorkQueue n = debugQueue.deQueue();
                if(!grid.findTrack(n.x1, n.y1, n.x2, n.y2, n.nn)) {
                    System.out.println("ERROR: Missing track " +n.nn);
                    missing++;
                } else {
                    found++;
                }
            }
            System.out.println("DEBUG: found "+found+" missing "+missing);
        }

    }

    public static void xmlReport(Document doc) {
        Element root = doc.getDocumentElement();
        Element element = doc.createElement("BenchmarkSpecific");
        root.appendChild(element);

        Element total = doc.createElement("Tracks");
        total.setTextContent("0");
        element.appendChild(total);

        Element tracks = doc.createElement("Laid");
        tracks.setTextContent("0");
        element.appendChild(tracks);

        Element subnode = doc.createElement("Failures");
        subnode.setTextContent("0");
        element.appendChild(subnode);
    }

    public static void main(String [] args) {
        if(args.length!=2) {
            System.out.println("Params: [numthreads] [input-file]");
            System.exit(-1);
        }
        int numThreads = Integer.parseInt(args[0]);
        String filename = args[1];
        LeeRouter lr = new LeeRouter(filename, false, false, false);

        int numMillis = 600000;

//		 Set up the benchmark
        long startTime = 0;
        long currentTime = 0;
        long lastSample = 0;
        long maxSampleThreshold = MAX_SAMPLE_THRESHOLD;
        boolean waitingForSample = false;
        long watchdogInterval = 1000;
        boolean exitByTimeout = false;
        int sampleInterval = 10000;

        if (XML_REPORT) {
            XMLHelper.initializeXMLReport(numThreads, 100, sampleInterval, "0", "leeroutercoarse","0");
        } else {
           // System.out.println("Benchmark: " + benchmarkClassName);
           // System.out.println("Adapter: " + adapterClassName);
           // System.out.println("Contention manager: " + managerClassName);
           // System.out.println("Threads: " + numThreads);
           // System.out.println("Mix: " + experiment + "% updates");
        }

        LeeThread[] thread = new LeeThread[numThreads];

        try {
            for (int i = 0; i < numThreads; i++)
                thread[i] = lr.createThread();
            startTime = System.currentTimeMillis();
            lastSample = startTime;
            for (int i = 0; i < numThreads; i++)
                thread[i].start();
            currentTime = System.currentTimeMillis();
            exitByTimeout = monitorBenchmarkToEnd(numMillis, sampleInterval, startTime, currentTime, lastSample, maxSampleThreshold, watchdogInterval, exitByTimeout, waitingForSample, thread);

            LeeThread.stop = true; // notify threads to stop
            for (int i = 0; i < numThreads; i++) {
                thread[i].join();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
        long elapsedTime = startTime - currentTime;
       // int throughput = (int) (LeeRouter.netNo / elapsedTime);
        System.out.println("Numthreads: " + numThreads);
        //System.out.println("Throughput:  " + throughput);
        System.out.println("ElapsedTime: " + elapsedTime);
        lr.report(startTime,exitByTimeout,true);
        lr.sanityCheck();
    }


    private static boolean monitorBenchmarkToEnd(int numMillis, int sampleInterval, long startTime, long currentTime, long lastSample, long maxSampleThreshold, long watchdogInterval, boolean exitByTimeout, boolean waitingForSample, LeeThread[] thread) throws InterruptedException {
        while (!exitByTimeout) {
            boolean exit = true;
            Thread.sleep(watchdogInterval);

            // Any threads with work left?
            for (LeeThread i : thread)
                if (i.finished != true) {
                exit = false;
                break;
                }
            if (!exit)
                currentTime = System.currentTimeMillis();
            else
                break;

            // Timeout?
            if (currentTime - startTime > numMillis) {
                exitByTimeout = true;
            }

            // Sample?
            // Warning: watchdogInterval should remain
            // short to prevent this figures going sloppy
            if (currentTime - lastSample > sampleInterval && !waitingForSample) {
                waitingForSample = true;
                for (LeeThread t : thread) {
                    t.resetMyStatistics();
                    t.doneSample = false;
                    t.sampleNow = true;
                }
            }
            // Get results from all threads
            // Note: avoiding synchronisation (costs), hence
            // poor method of waiting for sample updates from threads
            // and hoping they are done by the time we go to check
            if(waitingForSample) {
                boolean statsUpdated = true;
                for (LeeThread t : thread) {
                    if(!t.finished && !t.doneSample) {
                        statsUpdated = false;
                        break;
                    }
                }

                if(statsUpdated || currentTime - lastSample > maxSampleThreshold) {
                    waitingForSample = false;
                    lastSample = currentTime;
                    double elapsed = (lastSample - startTime) / 1000.0;
                    obtainStats(thread, elapsed, XML_REPORT);
                }
            }
        }
        return exitByTimeout;
    }

    private static void report( long startTime,
            boolean timeout, boolean xmlreport) {

        long stopTime = System.currentTimeMillis();
        double elapsed = (stopTime - startTime) / 1000.0;
        if (xmlreport) {
            XMLHelper.generateXMLReportSummary(timeout, xmlreport, elapsed);

        } else {

            //benchmark.report();
            System.out.println("Elapsed time: " + elapsed + " seconds.");
            System.out.println("----------------------------------------");
        }
    }

    static void obtainStats(LeeThread[] thread, double time, boolean xmlreport) {
        //long commits = 0;
        //long transactions = 0;
        //long commitMemRefs = 0;
        //long totalMemRefs = 0;
        long laidTracks=0;
        //long totalTracks=0;
        String hardware = null;
        boolean finalStats = false;

        if (thread == null) {
            // This means we are going to exit
            finalStats = true;
            laidTracks=LeeThread.totalLaidTracks;
        } else {
            for (LeeThread t : thread) {
                //commits += t.myCommits;
                //transactions += t.myTransactions;
                //commitMemRefs += t.myCommitMemRefs;
                //totalMemRefs += t.myTotalMemRefs;
                hardware = t.hardware;
                laidTracks +=t.myLaidTracks;
                if (hardware == null)
                    hardware = "";
            }
        }

      //  if (xmlreport) {
            XMLHelper.generateXMLIntervalSample(time,laidTracks,0,0,0,hardware,finalStats);//generateXMLIntervalSampleCoarse(time,laidTracks, hardware, finalStats);
       // } else {
           // System.out.println("Timestamp: " + time);
           // if (transactions > 0) {
               // System.out.println("Committed: " + commits);
               // System.out.println("Total transactions: " + transactions);
               // System.out.println("Percent committed: " + (100 * commits)
               // / transactions);
               // System.out.println("Commit MemRefs: " + commitMemRefs);
          //      System.out.println("Total MemRefs " + totalMemRefs);
          //      System.out.println("laidTracks " + laidTracks);
           // } else {
           //     System.out.println("No transactions executed!");
           // }
       // }
    }
}
