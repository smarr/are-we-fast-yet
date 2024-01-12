#ifndef _STORAGE_H
#define _STORAGE_H

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

#include "Benchmark.h"

//#define _USE_STD_VECTOR_
#ifdef _USE_STD_VECTOR_

// NOTE: this is four times slower than the other variant without std::vector!!!
#include <vector>

class Storage : public Benchmark {

  int count;

public:
  int benchmark();

  bool verifyResult(int result) {
    return 5461 == result;
  }

private:
  struct Tree
  {
      ~Tree()
      {
          for( std::vector<Tree*>::const_iterator i = subs.begin(); i != subs.end(); ++i )
              delete (*i);
      }
      std::vector<Tree*> subs;
  };

  void buildTreeDepth(int depth, Tree *out);

};
#else

class Storage : public Benchmark {

  int count;

public:
  int benchmark();

  bool verifyResult(int result) {
    return 5461 == result;
  }

private:
  struct Tree
  {
      Tree():sub(0) {}
      ~Tree()
      {
          if( sub )
              delete[] sub;
      }
      Tree* sub;
  };

  Tree* buildTreeDepth(int depth);

};

#endif

#endif // _STORAGE_H
