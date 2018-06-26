import "harness" as harness

def size: Number = 9.asInteger
def komi: Number = 7.5
def empty: Number = 1.asInteger
def white: Number = 2.asInteger
def black: Number = 3.asInteger
def pass: Number = -1.asInteger
def maxmoves: Number = size * size * 3

var globalTimestamp: Number := 0.asInteger
var globalMoves: Number := 0.asInteger
var random: Random := Done

method toPos(x: Number, y: Number) -> Number {
  y * size + x
}

method toXY(pos: Number) -> List {
  def y: Number = (pos / size).asInteger
  def x: Number = (pos % size).asInteger
  [y, x]
}

type Square = interface {
  zobristStrings
  color
  setNeighbours
  used
  move(_)
  pos
  neighbours
}

type Board = interface {
  move(_)
  reset
  useful(_)
}

type EmptySet = interface {
  randomChoice
  add(_)
  remove(_)
  set(_, _)
}

type ZobristHash = interface {
  update(_, _)
  add
  dupe
}

class newSquare(board: Board, pos: Number) -> Square {
  var timestamp: Number := globalTimestamp
  var removestamp: Number := globalTimestamp
  def zobristStrings: List = [random.next, random.next, random.next]
  var neighbours: List := Done
  var color: Number := 0.asInteger
  var reference: Square := Done
  var ledges: Number := 0.asInteger
  var used: Boolean := false
  var tempLedges: Number := 0.asInteger

  method setNeighbours -> Done {
    def x: Number = pos % size
    def y: Number = pos / size

    neighbours := platform.kernel.Vector.new()
    [ [ -1, 0 ], [ 1, 0 ], [ 0, -1 ], [ 0, 1 ] ].do { d: List ->
      def dx: Number = d.at(1.asInteger)
      def dy: Number = d.at(2.asInteger)
      def newX: Number = x + dx
      def newY: Number = y + dy
      ((0 <= newX) && (newX < size) && (0 <= newY) && (newY < size)).ifTrue {
        neighbours.append(
          board.squares.at(toPos(newX.asInteger, newY.asInteger) + 1.asInteger))
      }
    }
  }

  method move(color': Number) -> Done {
    globalTimestamp := globalTimestamp + 1.asInteger
    globalMoves := globalMoves + 1.asInteger

    board.zobrist.update(self, color')
    color := color'
    reference := self
    ledges := 0.asInteger
    used := true

    neighbours.do { neighbour: Square ->
      def neighcolor: Number = neighbour.color
      // print("S.move nc: " + (neighcolor - 1.asInteger) + " ledges: " + ledges)
      (neighcolor == empty).ifTrue {
        ledges := ledges + 1.asInteger
      } ifFalse {
        def neighbourRef: Square = neighbour.find(true)
        // print("found ref: " + neighbourRef.pos)
        (neighcolor == color').ifTrue {
          (neighbourRef.reference.pos != pos).ifTrue {
            ledges := ledges + neighbourRef.ledges
            neighbourRef.reference := self
          }
          ledges := ledges - 1.asInteger
          // print("ledges: " + ledges)
        } ifFalse {
          neighbourRef.ledges := neighbourRef.ledges - 1.asInteger
          (neighbourRef.ledges == 0.asInteger).ifTrue {
            // print("ledges == 0")
            neighbour.remove(neighbourRef, true)
          }
        }
      }
    }
    board.zobrist.add
  }

  method remove(reference: Square, update: Boolean) -> Done {
    board.zobrist.update(self, empty)
    removestamp := globalTimestamp
    update.ifTrue {
      color := empty
      // print("add empty " + pos)
      board.emptyset.add(pos)
    }

    neighbours.do { neighbour: Square ->
      ((neighbour.color != empty) &&
          (neighbour.removestamp != globalTimestamp)).ifTrue {
        def neighbourRef: Square = neighbour.find(update)
        (neighbourRef.pos == reference.pos).ifTrue {
          neighbour.remove(reference, update)
        } ifFalse {
          update.ifTrue {
            neighbourRef.ledges := neighbourRef.ledges + 1.asInteger
          }
        }
      }
    }
  }

  method find(update: Boolean) -> Square {
    var reference': Square := reference
    (reference'.pos != pos).ifTrue {
      reference' := reference'.find(update)
      update.ifTrue {
        reference := reference'
      }
    }
    return reference'
  }
}

class newEmptySet(board: Board) -> EmptySet {
  def empties: List = platform.kernel.Vector.new(size * size)
  def emptyPos: List = platform.kernel.Vector.new(size * size)
  empties.appendAll(0.asInteger.to((size * size) - 1.asInteger))
  emptyPos.appendAll(0.asInteger.to((size * size) - 1.asInteger))

  method randomChoice -> Number {
    def choices: Number = empties.size
    {choices > 0.asInteger}.whileTrue {
      // print("choices " + choices)
      def i: Number = (random.next % choices)
      def pos: Number = empties.at(i + 1.asInteger)
      (board.useful(pos)).ifTrue {
        // print("randomChoice useful " + pos)
        return pos
      }
      // print("randomChoice not useful")
      choices := choices - 1.asInteger
      set(i, empties.at(choices + 1.asInteger))
      set(choices, pos)
    }
    return pass
  }

  method add(pos: Number) -> Done {
    emptyPos.at(pos + 1.asInteger)put(empties.size)
    empties.append(pos)
  }

  method remove(pos: Number) -> Done {
    // print("emptyPos.size: " + emptyPos.size)
    set(emptyPos.at(pos + 1.asInteger), empties.at(empties.size))
    empties.remove
  }

  method set(i: Number, pos: Number) -> Done {
    empties.at(i + 1.asInteger)put(pos)
    emptyPos.at(pos + 1.asInteger)put(i)
  }
}

class newZobristHash(board: Board) -> ZobristHash {
  def hashSet: Done = platform.collections.Set.new
  var hash: Number := 0.asInteger
  board.squares.do { square: Square ->
    hash := hash.bitXor(square.zobristStrings.at(empty))
  }
  hashSet.removeAll
  hashSet.add(hash)

  method update(square: Square, color: Number) -> Done {
    hash := hash.bitXor(square.zobristStrings.at(square.color))
    hash := hash.bitXor(square.zobristStrings.at(color))
  }

  method add -> Done {
    hashSet.add(hash)
  }

  method dupe -> Boolean {
    return hashSet.contains(hash)
  }
}

class newBoard -> Board {
  var emptyset: EmptySet := Done
  var zobrist: ZobristHash := Done
  var color: Number := empty
  var finished: Boolean := false
  var lastmove: Number := -2.asInteger
  var history: List := Done
  var whiteDead: Number := 0.asInteger
  var blackDead: Number := 0.asInteger

  def squares: List = platform.kernel.Array.new(size * size)
  1.asInteger.to(size * size).do { pos: Number ->
    // print(pos)
    squares.at(pos)put(newSquare(self, (pos - 1.asInteger)))
  }
  squares.do { square: Square -> square.setNeighbours }
  reset()

  method reset -> Done {
    squares.do { square: Square ->
      square.color := empty
      square.used := false
    }
    emptyset := newEmptySet(self)
    zobrist := newZobristHash(self)
    color := black
    finished := false
    lastmove := -2.asInteger
    history := platform.kernel.Vector.new
    whiteDead := 0.asInteger
    blackDead := 0.asInteger
  }

  method move(pos: Number) -> Done {
    // print("B.move: " + pos)
    (pos != pass).ifTrue {
      var square': Square := squares.at(pos + 1.asInteger)
      // print("B.move square: " + square'.pos)
      square'.move(color)
      emptyset.remove(square'.pos)
    } ifFalse {
      // print("lastmove: " + lastmove)
      (lastmove == pass).ifTrue {
        // print("set finished")
        finished := true
      }
    }

    (color == black).ifTrue {
      color := white
    } ifFalse {
      color := black
    }
    lastmove := pos
    history.append(pos)
  }

  method randomMove -> Done {
    emptyset.randomChoice
  }

  method usefulFast(square: Square) -> Boolean {
    // print(square.used)
    (!square.used).ifTrue {
      // print("square.neighbours.size")
      // print(square.neighbours.size)
      square.neighbours.do { neighbour: Square ->
        // print(neighbour.color)
        (neighbour.color == empty).ifTrue {
          return true
        }
      }
    }
    return false
  }

  method useful(pos: Number) -> Boolean {
    globalTimestamp := globalTimestamp + 1.asInteger
    var square: Square := squares.at(pos + 1.asInteger)
    (usefulFast(square)).ifTrue {
      return true
    }

    // print("useful: not fast")
    def oldHash: Number = zobrist.hash
    zobrist.update(square, color)
    var empties: Number    := 0.asInteger
    var opps: Number       := 0.asInteger
    var weakOpps: Number   := 0.asInteger
    var neighs: Number     := 0.asInteger
    var weakNeighs: Number := 0.asInteger

    square.neighbours.do { neighbour: Square ->
      def neighcolor: Number = neighbour.color
      (neighcolor == empty).ifTrue {
        empties := empties + 1.asInteger
      } ifFalse {
        def neighbourRef: Square = neighbour.find(false)
        (neighbourRef.timestamp != globalTimestamp).ifTrue {
          (neighcolor == color).ifTrue {
            neighs := neighs + 1.asInteger
          } ifFalse {
            opps := opps + 1.asInteger
          }
          neighbourRef.timestamp := globalTimestamp
          neighbourRef.tempLedges := neighbourRef.ledges
        }
        neighbourRef.tempLedges := neighbourRef.tempLedges - 1.asInteger
        (neighbourRef.tempLedges == 0).ifTrue {
          (neighcolor == color).ifTrue {
            weakNeighs := weakNeighs + 1.asInteger
          } ifFalse {
            weakOpps := weakOpps + 1.asInteger
            neighbourRef.remove(neighbourRef, false)
          }
        }
      }
    }
    def dupe: Boolean = zobrist.dupe()
    zobrist.hash := oldHash
    def strongNeighs: Number = neighs - weakNeighs
    def strongOpps: Number = opps - weakOpps
    // print("return: ")
    // print(!dupe && ((empties != 0) || (weakOpps != 0) || (
    //  (strongNeighs != 0) && ((strongOpps != 0) || (weakNeighs != 0)))))
    return !dupe && ((empties != 0) || (weakOpps != 0) || (
      (strongNeighs != 0) && ((strongOpps != 0) || (weakNeighs != 0))))
  }

  method usefulMoves -> List {
    // print("usefulMoves")
    // print(emptyset.empties.size)
    return emptyset.empties.select { pos: Number -> useful(pos) }
  }

  method replay(history: List) -> Done {
    // print("Replay: " + history.size)
    history.do { pos: Number -> move(pos) }
  }

  method score(color: Number) -> Number {
    var count: Number
    (color == white).ifTrue {
      count := komi + blackDead
    } ifFalse {
      count := whiteDead
    }
    squares.do { square: Square ->
      def squarecolor: Number = square.color
      (squarecolor == color).ifTrue {
        count := count + 1.asInteger
      } ifFalse {
        (squarecolor == empty).ifTrue {
          var surround: Number := 0.asInteger
          square.neighbours.do { neighbour: Square ->
            (neighbour.color == color).ifTrue {
              surround := surround + 1.asInteger
            }
          }
          (surround == square.neighbours.size).ifTrue {
            count := count + 1.asInteger
          }
        }
      }
    }
    return count
  }

  method check -> Done {
    squares.do { square: Square ->
      if (square.color != empty) then {
        def members1: Done = platform.collections.Set.new
        members1.add(square)

        var changed: Boolean := true
        { changed }.whileTrue {
          changed := false
          def copy: Done = platform.collections.Set.new
          copy.addAll(members1)
          copy.do { member: Square ->
            member.neighbours.do { neighbour: Square ->
              if ((neighbour.color == square.color) && !members1.contains(neighbour)) then {
                changed := true
                members1.add(neighbour)
              }
            }
          }
        }

        var ledges1: Number := 0.asInteger
        members1.do { member: Square ->
          member.neighbours.do { neighbour: Square ->
            if (neighbour.color == empty) then {
              ledges1 := ledges1 + 1.asInteger
            }
          }
        }

        def root: Square = square.find()

        // print 'members1', square, root, members1
        // print 'ledges1', square, ledges1

        def members2: Done = platform.collections.Set.new
        squares.do { square2: Square ->
          if ((square2.color != empty) && (square2.find() == root)) then {
            members2.add(square2)
          }
        }

        def ledges2: Number  = root.ledges
        // print 'members2', square, root, members1
        // print 'ledges2', square, ledges2

        def size1: Number = members1.size
        members1.addAll(members2)
        if (size1 != members1.size) then {
          error("members1 and members2 do not contain the same elements")
        }
        if (ledges1 != ledges2) then {
          error("ledges differ at " + square + " " + ledges1 + " " + ledges2)
        }
      }
    }
  }
}

type Node = interface {
  play(_)
  select
  playLoop(_, _, _)
}

class newUCTNode -> Node {
  var bestchild: Node := Done
  var pos: Number := -1.asInteger
  var wins: Number := 0.asInteger
  var losses: Number := 0.asInteger
  var parent: Node := Done
  def posChild: List = platform.kernel.Array.new(size * size)
  var unexplored: List := Done

  method playLoop(board: Board, node': Node, path: List) -> Done {
    var node: Node := node'
    true.whileTrue {
      def pos: Number = node.select()
      (pos == pass).ifTrue {
        return
      }
      board.move(pos)
      var child: Node := node.posChild.at(pos + 1.asInteger)
      (child == Done).ifTrue {
        child := newUCTNode()
        node.posChild.at(pos + 1.asInteger)put(child)
        child.unexplored := board.usefulMoves()
        child.pos := pos
        child.parent := node
        path.append(child)
        return
      }
      path.append(child)
      node := child
    }
  }

  method play(board: Board) -> Done {
    // uct tree search
    def color: Number = board.color
    def node: Node = self
    def path: List = platform.kernel.Vector.with(node)

    playLoop(board, node, path)

    randomPlayout(board)
    updatePath(board, color, path)
  }

  method select() -> Number {
    // select move; unexplored children first, then according to uct value
    ((unexplored ~= Done) && (!unexplored.isEmpty)).ifTrue {
        // print("unexplored.size " + unexplored.size)
        def i: Number = (random.next % unexplored.size) + 1.asInteger
        def pos: Number = unexplored.at(i)
        unexplored.at(i)put(unexplored.at(unexplored.size))
        unexplored.remove()
        return pos
    } ifFalse {
      (bestchild ~= Done).ifTrue {
        return bestchild.pos
      } ifFalse {
        return pass
      }
    }
  }

  method randomPlayout(board: Board) -> Done {
    // random play until both players pass
    // XXX while not self.finished?
    1.asInteger.to(maxmoves.asInteger)do { i: Number ->
      board.finished.ifTrue {
        // print("random_playout finished")
        return
      }
      board.move(board.randomMove())
    }
  }

  method updatePath(board: Board, color': Number, path: List) -> Done {
    var color: Number := color'
    // update win/loss count along path
    def wins: Boolean = board.score(black) >= board.score(white)
    path.do { node: Node ->
      (color == black).ifTrue {
          color := white
      } ifFalse {
          color := black
      }

      (wins == (color == black)).ifTrue {
        node.wins := node.wins + 1.asInteger
      } ifFalse {
        node.losses := node.losses + 1.asInteger
      }

      (node.parent == Done).ifFalse {
        node.parent.bestchild := node.parent.bestChild()
      }
    }
  }

  method score -> Number {
    def winrate: Number = wins / (wins + losses)
    def parentvisits: Number = parent.wins + parent.losses
    (parentvisits == 0.asInteger).ifTrue {
      return winrate
    }
    def nodevisits: Number = wins + losses
    return winrate + ((parentvisits + 0).log / (5 * nodevisits)).sqrt
  }

  method bestChild -> Node {
    var maxscore: Number := -1.asInteger
    var maxchild: Node := Done
    posChild.do { child: Node ->
      (child == Done).ifFalse {
        (child.score() > maxscore).ifTrue {
          maxchild := child
          maxscore := child.score()
        }
      }
    }
    return maxchild
  }

  method bestVisited -> Node {
    var maxvisits: Number := -1.asInteger
    var maxchild: Node := Done
    posChild.do { child: Node ->
      // if child:
      //   print to_xy(child.pos), child.wins, child.losses, child.score()
      (child == Done).ifFalse {
        ((child.wins + child.losses) > maxvisits).ifTrue {
          maxvisits := child.wins + child.losses
          maxchild := child
        }
      }
    }
    return maxchild
  }
}

class newGo -> Benchmark {
  inherit harness.newBenchmark

  method innerBenchmarkLoop(innerIterations: Number) -> Boolean {
    def result: Number = versusCpu(innerIterations)
    return verify (innerIterations) resultFor (result)
  }

  method computerMove(board: Board, games: Number) -> Number {
    def pos: Number = board.randomMove()
    // print("randomMove " + pos)
    (pos == pass).ifTrue {
      return pass
    }
    def tree: Node = newUCTNode
    tree.unexplored := board.usefulMoves()
    def nboard: Board = newBoard

    0.asInteger.to(games - 1.asInteger)do { game: Number ->
      // print("new game " + game)
      def node: Node = tree
      nboard.reset()
      nboard.replay(board.history)
      node.play(nboard)
    }
    return tree.bestVisited().pos
  }

  method versusCpu(games: Number) -> Done {
    random := harness.newRandom
    def board: Board = newBoard
    return computerMove(board, games)
  }

  method verify (innerIterations: Number) resultFor (result: Number) -> Boolean {
    (innerIterations ==   10) .ifTrue { return result ==  8 }
    (innerIterations ==  100) .ifTrue { return result ==  1 }
    (innerIterations ==  200) .ifTrue { return result == 37 }
    (innerIterations ==  500) .ifTrue { return result == 79 }
    (innerIterations == 1000) .ifTrue { return result == 79 }
    print("No verification result for {innerIterations} found (results was {result}).")
    return false
  }
}

method newInstance -> Benchmark { newGo }
