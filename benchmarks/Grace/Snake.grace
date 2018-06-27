import "harness" as harness


def BOARDHEIGHT: Number = 20.asInteger
def BOARDWIDTH: Number  = 30.asInteger

type Position = interface {
  x
  y
}

class newPosition(x: Number, y: Number) -> Position {
  method asString -> String {
    return "({x}, {y})"
  }
}

method samePosition(a: Position, b: Position) -> Boolean {
  (a.x == b.x) && (a.y == b.y)
}

type Snake = {
  segments
  direction
  head
  collidedWithWall
  collidedWithSelf
  nextHead
  slither
  grow
  asString
}

class newSnake(segments: List) -> Snake {
  var direction: String

  method head -> Position {
    return segments.at(segments.size)
  }

  method collidedWithWall -> Boolean {
    (head.x <= 0.asInteger) || (
      head.x >= BOARDWIDTH)  || (
        head.y <= 0.asInteger) || (
          head.y >= BOARDHEIGHT)
  }

  method collidedWithSelf -> Boolean {
    1.asInteger.to(segments.size) do { i: Number ->
      (i + 1.asInteger).to(segments.size) do { j: Number ->
        (samePosition(segments.at(i), segments.at(j))).ifTrue {
          return true
        }
      }
    }
    return false
  }

  method nextHead -> Position {
    ("right" == direction). ifTrue { return newPosition(head.x + 1.asInteger, head.y              ) }
    ("left"  == direction). ifTrue { return newPosition(head.x - 1.asInteger, head.y              ) }
    ("down"  == direction). ifTrue { return newPosition(head.x,               head.y - 1.asInteger) }
    ("up"    == direction). ifTrue { return newPosition(head.x,               head.y + 1.asInteger) }
    error("{direction} not understood as a direction?")
  }

  method slither -> Done {
    segments.append(nextHead)
    segments.remove(segments.at(1.asInteger))
    Done
  }

  method grow -> Done {
    segments.append(nextHead)
    Done
  }

  method isTouching(position: Position) -> Boolean {
    segments.do { seg: Position ->
      samePosition(seg, position).ifTrue { return true }
    }
    return false
  }

  method asString -> String {
    var s: String := "Snake\n  segs={segments.size}\n"
    segments.do { seg: Position ->
      s := "{s}  {seg}\n"
    }
    return s
  }
}

type World = interface {
  food
  snake
  isGameOver
  tick
}

class newWorld -> World {
  var snake: Snake
  var food: Position
  var moves: Number := 0.asInteger
  var random: Random

  method reset -> Done {
    random := harness.newRandom
    random.seed := 1324.asInteger

    var segments: List := platform.kernel.Vector.new
    segments.append(newPosition(10.asInteger, 15.asInteger))
    snake := newSnake(segments)
    snake.direction := "right"
    food := randomPosition
    moves := 0.asInteger
  }

  method isGameOver -> Boolean {
    snake.collidedWithWall || snake.collidedWithSelf
  }

  method randomDouble -> Number {
    (random.next + 0.0) / 65535.0
  }

  method randomBetween(x: Number)and(y: Number) -> Number {
    (x + ((y + 1) - x) * randomDouble).asInteger
  }

  method randomPosition -> Position {
     newPosition ( randomBetween(1.asInteger)and(BOARDWIDTH - 1.asInteger),
                   randomBetween(1.asInteger)and(BOARDHEIGHT - 1.asInteger) )
  }

  method tick -> Done {
    samePosition(food, snake.head).ifTrue {
      snake.grow
      food := randomPosition
    } ifFalse {
      snake.slither
    }

    moves := moves + 1.asInteger
  }

  method handleKey (key: String) -> Done {
    (key == "w"). ifTrue {
      snake.direction := "up"
      return Done
    }
    (key == "s"). ifTrue {
      snake.direction := "down"
      return Done
    }
    (key == "a"). ifTrue {
      snake.direction := "left"
      return Done
    }
    (key == "d"). ifTrue {
      snake.direction := "right"
      return Done
    }

    error("{key} not understood as a key?")
  }

  method render -> Done {
    var renderStr: String := ""

    0.asInteger.to(BOARDHEIGHT) do { y: Number ->
      var rowStr: String := ""

      0.asInteger.to(BOARDWIDTH) do { x: Number ->
        var p: Position := newPosition(x, y)

        var isWall: Boolean  := (x <= 0) || (x >= BOARDWIDTH) || (y <= 0) || (y >= BOARDHEIGHT)
        var isSnake: Boolean := snake.isTouching(p)
        var isFood: Boolean  := samePosition(food, p)

        isSnake.ifTrue { rowStr := rowStr ++ "S" } ifFalse {
          isFood.ifTrue { rowStr := rowStr ++ "O" } ifFalse {
            isWall.ifTrue { rowStr := rowStr ++ "X" } ifFalse {
              rowStr := rowStr ++ " "
            }
          }
        }
      }

      renderStr := "{rowStr}\n" + renderStr
    }

    print(renderStr)
  }

}

method replay (world: World, history: List) -> Done {
  world.reset

  history.do { item: String ->

    (item == "t").ifTrue {
      world.tick
      // world.render
      world.isGameOver.ifTrue {
        return Done
      }
    } ifFalse {
      world.handleKey(item)
    }
  }

  Done
}

class newSnakeBenchmark -> Benchmark {
  inherit harness.newBenchmark

  def world: World = newWorld
  def history: List = [
    "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "s", "t", "t", "t", "d", "t", "t", "t",
    "w", "t", "t", "t", "t", "t", "t",
    "a", "t", "t", "t", "t", "t", "t", "t",
    "s", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "a", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "w", "t", "t", "t", "t",
    "d", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "w", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "a", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "s", "t", "t", "t",
    "d", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "w", "t", "t", "t", "t", "t", "t",
    "a", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "s", "t", "t", "t", "t", "t", "t", "t", "t", "t", "t",
    "a", "t", "t",
    "w", "t", "t",
    "d", "t", "t", "t", "t", "t", "t"
  ]

  method benchmark -> World {
    replay(world, history)
    world
  }

  method verifyResult(world: World) -> Boolean {
    (world.moves == 157.asInteger) &&
      (world.snake.segments.size == 10.asInteger) &&
      (world.isGameOver)
  }
}

method newInstance -> Benchmark { newSnakeBenchmark }
