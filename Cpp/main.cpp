/* Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch>
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

#include <iostream>
#include "Run.h"
#include "som/Random.h"
#include <assert.h>

static void run( const std::string& what, int numIterations, int innerIterations )
{
    Run r(what);
    r.setNumIterations(numIterations);
    r.setInnerIterations(innerIterations);
    try
    {
        r.runBenchmark();
        r.printTotal();
    }catch( const char* msg )
    {
        std::cerr << msg << std::endl;
    }catch( const std::string& str )
    {
        std::cerr << str << std::endl;
    }catch(const std::exception& e)
    {
        std::cerr << e.what() << std::endl;
    }catch(...)
    {
        std::cerr << "unexpected exception" << std::endl;
    }
}

static void runAll()
{
    run("DeltaBlue", 12000, 1 );
    run("Richards", 100, 1);
    run("Json", 100, 1);
    run("Havlak", 10, 1 );
    run("CD", 250, 2);
    run("Bounce", 1500, 1);
    run("List", 1500, 1);
    run("Mandelbrot", 500, 1);
    run("NBody", 250000, 1);
    run("Permute", 1000, 1);
    run("Queens", 1000, 1);
    run("Sieve", 3000, 1);
    run("Storage", 1000, 1);
    run("Towers", 600, 1);
}

static void runOnce()
{
    run("DeltaBlue", 1, 1 );
    run("Richards", 1, 1);
    run("Json", 1, 1);
    run("Havlak", 1, 1 );
    run("CD", 1, 2);
    run("Bounce", 1, 1);
    run("List", 1, 1);
    run("Mandelbrot", 1, 1);
    run("NBody", 1, 1);
    run("Permute", 1, 1);
    run("Queens", 1, 1);
    run("Sieve", 1, 1);
    run("Storage", 1, 1);
    run("Towers", 1, 1);
}

int main(int argc, char *argv[])
{
    runAll();
    //runOnce();

    return 0;
}
