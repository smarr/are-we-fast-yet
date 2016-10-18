/* This code is based on the SOM class library.
 *
 * Copyright (c) 2001-2016 see AUTHORS.md file
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import java.util.Arrays;

import som.Random;


public final class Bounce extends Benchmark {

  private static class Ball {
    private int x;
    private int y;
    private int xVel;
    private int yVel;

    Ball(final Random random) {
      x = random.next() % 500;
      y = random.next() % 500;
      xVel = (random.next() % 300) - 150;
      yVel = (random.next() % 300) - 150;
    }

    public boolean bounce() {
      int xLimit = 500;
      int yLimit = 500;
      boolean bounced = false;

      x += xVel;
      y += yVel;
      if (x > xLimit) { x = xLimit; xVel = 0 - Math.abs(xVel); bounced = true; }
      if (x < 0)      { x = 0;      xVel = Math.abs(xVel);     bounced = true; }
      if (y > yLimit) { y = yLimit; yVel = 0 - Math.abs(yVel); bounced = true; }
      if (y < 0)      { y = 0;      yVel = Math.abs(yVel);     bounced = true; }
      return bounced;
    }
  }

  @Override
  public Object benchmark() {
    Random random = new Random();

    int ballCount = 100;
    int bounces   = 0;
    Ball[] balls  = new Ball[ballCount];

    Arrays.setAll(balls, v -> new Ball(random));

    for (int i = 0; i < 50; i++) {
      for (Ball ball : balls) {
        if (ball.bounce()) {
          bounces += 1;
        }
      }
    }
    return bounces;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 1331 == (int) result;
  }
}
