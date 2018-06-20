// Copyright (c) 2001-2018 see AUTHORS file
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the 'Software'), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
//
// Adapted for Grace by Richard Roberts
//   2018, June
//

import "random" as random

class Ball {
  var x := random.next % 500.asInteger
  var y := random.next % 500.asInteger
  var xVel := (random.next % 300.asInteger) - 150.asInteger
  var yVel := (random.next % 300.asInteger) - 150.asInteger
    
  method bounce {
    var xLimit := 500.asInteger
    var yLimit := 500.asInteger
    var bounced := false

    x := x + xVel
    y := y + yVel
    
    (x > xLimit).ifTrue {
      x := xLimit
      xVel := 0 - xVel.abs
      bounced := true
    }
    
    (x < 0).ifTrue {
      x := 0
      xVel := xVel.abs
      bounced := true
    }
    
    (y > yLimit).ifTrue {
      y := yLimit
      yVel := 0 - yVel.abs
      bounced := true
    }
    
    (y < 0).ifTrue {
      y := 0
      yVel := yVel.abs
      bounced := true
    }
    
    bounced
  }
}

method Bounce {
  random.resetSeed
  var ballCount := 100.asInteger
  var bounces := 0.asInteger
  var balls := platform.kernel.Array.new (ballCount) withAll {
    Ball
  }

  1.asInteger.to(50.asInteger) do { i ->
    balls.do { ball ->
      ball.bounce.ifTrue {
        bounces := bounces + 1.asInteger
      }
    }
  }

  bounces
}

method asString {"Bounce.grace"}

method benchmark(innerIterations) {
  1.asInteger.to(innerIterations) do { i ->
    def result = Bounce
    (result == 1331.asInteger).ifFalse {
      error("{self} failed, {result} != 1331")
    }
  }
}
