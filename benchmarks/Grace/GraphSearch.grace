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

import "harness" as harness

def MinEdges: Number     = 2.asInteger
def MaxInitEdges: Number = 4.asInteger
def MinWeight: Number    = 1.asInteger
def MaxWeight: Number    = 1.asInteger

def ExpectedNoOfNodes: Number = 3000000.asInteger
def ExpectedTotalCost: Number = 26321966.asInteger

type Edge = interface {
  dest
  weight
}

type Node = interface {
  starting
  noOfEdges
}

class newEdge (dest: Number) and (weight: Number) -> Edge {}
class newNode (starting: Number) and (noOfEdges: Number) -> Node {}

class newGraphSearch -> Benchmark {
  inherit harness.newBenchmark

  var graphNodes: List
  var graphMask: List
  var updatingGraphMask: List
  var graphVisited: List
  var cost: List
  var graphEdges: List

  method innerBenchmarkLoop(innerIterations: Number) -> Boolean {
    def random: Random = harness.newJenkins(49734321.asInteger)
    def noOfNodes: Number = (ExpectedNoOfNodes / 1000.asInteger) * innerIterations

    initializeGraph (noOfNodes) with (random)
    breadthFirstSearch (noOfNodes)

    return verify(innerIterations)
  }

  method verify(innerIterations: Number) -> Boolean {
    (cost.size == ((ExpectedNoOfNodes / 1000.asInteger) * innerIterations)).ifFalse {
      return false
    }

    var totalCost: Number := 0.asInteger
    cost.do { c: Number ->
      totalCost := totalCost + c
    }

    (cost.size == ExpectedNoOfNodes).ifTrue { return totalCost == ExpectedTotalCost }
    (cost.size == 60000).ifTrue { return totalCost == 392786 } // innerIterations==20
    (cost.size == 120000).ifTrue { return totalCost == 862279 } // innerIterations==40

    print("No verification result for {innerIterations} found")
    print("Cost size: {cost.size}")
    print("totalCost: {totalCost}")

    false
  }

  method initializeGraph (noOfNodes: Number) with (random: Random) -> Done {
    graphNodes         := platform.kernel.Array.new (noOfNodes)
    graphMask          := platform.kernel.Array.new (noOfNodes) withAll(false)
    updatingGraphMask  := platform.kernel.Array.new (noOfNodes) withAll(false)
    graphVisited       := platform.kernel.Array.new (noOfNodes) withAll(false)
    cost               := platform.kernel.Array.new (noOfNodes) withAll(-1)

    var source: Number := 1.asInteger
    var graph: List := platform.kernel.Array.new (noOfNodes) withAll { platform.kernel.Vector.new }

    graph.doIndexes { i: Number ->
      var noOfEdges: Number := random.next.rem(MaxInitEdges - MinEdges + 1.asInteger).abs + MinEdges

      1.asInteger.to (noOfEdges) do { j: Number ->
        var nodeId: Number := (random.next.rem(noOfNodes)).abs + 1.asInteger
        var weight: Number := (random.next.rem(MaxWeight - MinWeight + 1.asInteger)).abs + MinWeight
        graph.at(i).append(newEdge (nodeId) and (weight))
        graph.at(nodeId).append(newEdge (i) and (weight))
      }
    }

    var totalEdges: Number := 0.asInteger
    graph.doIndexes { i: Number ->
      var noOfEdges: Number := graph.at(i).size
      graphNodes.at (i) put (newNode (totalEdges + 1.asInteger) and (noOfEdges))
      totalEdges := totalEdges + noOfEdges
    }

    graphMask.at (source) put (true)
    graphVisited.at (source) put (true)

    graphEdges := platform.kernel.Array.new (totalEdges) withAll (0.asInteger)

    var k: Number := 1.asInteger
    graph.do { i: Number ->
      i.do { j: Number ->
        graphEdges.at (k) put (j.dest)
        k := k + 1.asInteger
      }
    }
    cost.at (source) put (0.asInteger)
  }

  method breadthFirstSearch (noOfNodes: Number) -> Done {
    var stop: Boolean := true

    { stop }.whileTrue {
      stop := false

      1.asInteger.to (noOfNodes) do { tid: Number ->
        graphMask.at(tid).ifTrue {
          graphMask.at(tid)put (false)
          graphNodes.at (tid).starting.to (graphNodes.at(tid).noOfEdges + graphNodes.at(tid).starting - 1.asInteger)
              do { i: Number ->
            var id: Number := graphEdges.at (i)
            graphVisited.at (id). ifFalse {
              cost. at (id) put (cost.at(tid) + 1.asInteger)
              updatingGraphMask.at (id) put (true)
            }
          }
        }
      }

      1.asInteger.to(noOfNodes) do { tid: Number ->
        updatingGraphMask.at(tid). ifTrue {
          graphMask.at (tid) put (true)
          graphVisited.at (tid) put (true)
          stop := true
          updatingGraphMask.at (tid) put (false)
        }
      }
    }
  }
}

method newInstance -> Benchmark { newGraphSearch }
