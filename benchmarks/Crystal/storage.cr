require "benchmarkx"
require "som"

class Storage < BenchmarkX

  def initialize
    @count = 0
  end

  def benchmark
    random = SomRandom.new
    @count = 0
    build_tree_depth(7, random)
    @count
  end

  def verify_result(result)
    5461 == result
  end

  def build_tree_depth(depth, random)
    @count += 1

    if depth == 1
      Array.new(random.next % 10 + 1)
    else
      arr = Array.new(4)
      arr.fill { build_tree_depth(depth - 1, random) }
      arr
    end
  end
end

TheBenchmark = Storage

require "harness"
