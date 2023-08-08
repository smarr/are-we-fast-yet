/* This code is derived from the SOM benchmarks, see AUTHORS.md file.
 *
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
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

#include "Bounce.h"
#include "som/Random.h"
#include <algorithm>
using namespace som;

int Bounce::benchmark()
{
    class Ball {
        int x;
        int y;
        int xVel;
        int yVel;
    public:
        Ball():x(0),y(0),xVel(0),yVel(0) {
            x = Random::next() % 500;
            y = Random::next() % 500;
            xVel = (Random::next() % 300) - 150;
            yVel = (Random::next() % 300) - 150;
        }

        bool bounce() {
            const int xLimit = 500;
            const int yLimit = 500;
            bool bounced = false;

            x += xVel;
            y += yVel;
            if (x > xLimit) { x = xLimit; xVel = 0 - abs(xVel); bounced = true; }
            if (x < 0)      { x = 0;      xVel = abs(xVel);     bounced = true; }
            if (y > yLimit) { y = yLimit; yVel = 0 - abs(yVel); bounced = true; }
            if (y < 0)      { y = 0;      yVel = abs(yVel);     bounced = true; }
            return bounced;
        }
    };

    Random::reset();
    int ballCount = 100;
    int bounces   = 0;
    Ball* balls = new Ball[ballCount];

    for (int i = 0; i < 50; i++) {
        for (int j = 0; j < ballCount; j++) {
            if (balls[j].bounce()) {
                bounces += 1;
            }
        }
    }
    delete[] balls;
    return bounces;
}
