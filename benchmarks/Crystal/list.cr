require "benchmarkx"

class List < BenchmarkX

  def benchmark
    result = tail(make_list(15),
                  make_list(10),
                  make_list(6))
    result.length
  end

  def make_list(length)
    if length == 0
      nil
    else
      e = Element.new(length)
      e.next = make_list(length - 1)
      e
    end
  end
  
  def is_shorter_than(x, y)
    x_tail = x
    y_tail = y

    while !y_tail.nil?
      if x_tail.nil?
        return true
      end
      x_tail = x_tail.not_nil!.next
      y_tail = y_tail.not_nil!.next
      false
    end
  end

  def tail(x, y, z)
    if is_shorter_than(y, x)
      tail(tail(x.not_nil!.next, y, z),
           tail(y.not_nil!.next, z, x),
           tail(z.not_nil!.next, x, y))
    else
      z.not_nil!
    end
  end

  def verify_result(result)
    10 == result
  end
end

class Element
  property :val, :next

  def initialize(v)
    @val  = v
    @next = nil
  end

  def length
    n = @next
    if n.is_a?(Nil)
      1
    else
      1 + n.length
    end
  end
end

TheBenchmark = List

require "harness"
