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

/**
 * @author Mohammad Ansari
 */
public class Grid {

  // Simple grid attributes and container
  private final int            width;
  private final int            height;
  private final int            depth;

  private final GridCell[][][] grid;

  public Grid(final int width, final int height, final int depth) {
    // Set up PCB grid
    this.width  = width;
    this.height = height;
    this.depth  = depth;

    grid = new GridCell[width][height][depth];
    instantiateGrid();
  }

  public void occupy(final int loX, final int loY, final int upX, final int upY) {
    for (int x = loX; x <= upX; x++) {
      for (int y = loY; y <= upY; y++) {
        for (int z = 0; z < depth; z++) {
          grid[x][y][z].setRouteID(LeeRouter.OCC);
        }
      }
    }
  }

  private void setEmptyToMaxWeight(final int x, final int y, final int z) {
    if (getPoint(x, y, z) == LeeRouter.EMPTY) {
      setPoint(x, y, z, LeeRouter.MAX_WEIGHT);
    }
  }

  private void setEmptyTo(final int x, final int y, final int z, final int weight) {
    if (getPoint(x, y, z) == LeeRouter.EMPTY) {
      setPoint(x, y, z, weight);
    }
  }

  public void addWeights() {
    for (int i = 0; i < LeeRouter.MAX_WEIGHT; i++) {
      for (int z = 0; z < depth; z++) {
        for (int x = 1; x < width - 1; x++) {
          for (int y = 1; y < height - 1; y++) {
            int weight = getPoint(x, y, z);
            if (weight == LeeRouter.OCC) {
              setEmptyToMaxWeight(x,     y + 1, z);
              setEmptyToMaxWeight(x + 1, y,     z);
              setEmptyToMaxWeight(x,     y - 1, z);
              setEmptyToMaxWeight(x - 1, y,     z);
            } else if (weight != LeeRouter.EMPTY) {
              setEmptyTo(x,     y + 1, z, weight);
              setEmptyTo(x + 1, y,     z, weight);
              setEmptyTo(x,     y - 1, z, weight);
              setEmptyTo(x - 1, y,     z, weight);
            }
          }
        }
      }
    }
  }

  public int getPoint(final int x, final int y, final int z) {
    return grid[x][y][z].getRouteID();
  }

  public void setPoint(final int x, final int y, final int z, final int val) {
    grid[x][y][z].setRouteID(val);
  }

  private void instantiateGrid() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        for (int k = 0; k < depth; k++) {
          grid[i][j][k] = new GridCell(LeeRouter.EMPTY);
        }
      }
    }
  }
}
