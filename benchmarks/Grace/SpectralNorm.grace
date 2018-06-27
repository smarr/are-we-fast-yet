// The Computer Language Benchmarks Game
//  https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
//  contributed by Isaac Gouy
//
// <reference path="./Include/node/index.d.ts" />
//
//
// Translated to Grace by Richard Roberts, 28/05/2018
//

import "harness" as harness

class newSpectralNorm -> Benchmark {
  inherit harness.newBenchmark


  method approximate(n: Number) -> Number {
    def u: List = platform.kernel.Vector.new
    def v: List = platform.kernel.Vector.new

    1.asInteger.to(n) do { i: Number -> u.append(1) }
    1.asInteger.to(n) do { i: Number -> v.append(0) }

    1.asInteger.to(10.asInteger) do { i: Number ->
        multiplyAtAv(n,u,v)
        multiplyAtAv(n,v,u)
    }

    var vBv: Number := 0
    var  vv: Number := 0
    1.asInteger.to(10.asInteger) do { i: Number ->
        vBv := vBv + u.at(i) * v.at(i)
        vv  := vv  + v.at(i) * v.at(i)
    }

    (vBv / vv).sqrt
  }

  method a(i: Number, j: Number) -> Number {
    1 / ( (i + j) * ((i + j) + 1) / 2 + i + 1 )
  }

  method multiplyAv(n: Number, v: List, av: List) -> Done {
    0.asInteger.to(n - 2.asInteger) do { i: Number ->
        av. at (i + 1.asInteger) put (0)
        0.asInteger.to(n - 2.asInteger) do { j: Number ->
          av.at(i + 1.asInteger) put ( av.at(i + 1.asInteger) + a(i, j) * v.at(j + 1.asInteger) )
        }
    }

    Done
  }

  method multiplyAtv(n: Number, v: List, atv: List) -> Done {
    0.asInteger.to(n - 2.asInteger) do { i: Number ->
        atv. at (i + 1.asInteger) put (0)
        0.asInteger.to(n - 2.asInteger) do { j: Number ->
          atv. at (i + 1.asInteger) put ( atv.at(i + 1.asInteger) + a(j, i) * v.at(j + 1.asInteger) )
        }
    }

    Done
  }

  method multiplyAtAv(n: Number, v: List, atAv: List) -> Done {
    def u: List = platform.kernel.Vector.new
    1.asInteger.to(n) do { i: Number -> u.append(0) }
    multiplyAv(n,v,u)
    multiplyAtv(n,u,atAv)
    Done
  }


  method verify (innerIterations: Number) resultFor (result: Number) -> Boolean {
    (innerIterations ==   10). ifTrue { return result == 1.2711145200047260 }
    (innerIterations ==  500). ifTrue { return result == 1.2742241157332803 }
    print("No verification result for {innerIterations} found (result was {result}).")
    false
  }

  method innerBenchmarkLoop(innerIterations: Number) -> Boolean {
    var result: Number := approximate(innerIterations)
    return verify (innerIterations) resultFor (result)
  }
}

method newInstance -> Benchmark { newSpectralNorm }
