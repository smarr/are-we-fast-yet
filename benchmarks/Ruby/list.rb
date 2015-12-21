class List < Benchmark

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
        return true;
      end
      x_tail = x_tail.next
      y_tail = y_tail.next
      false
    end
  end

  def tail(x, y, z)
    if is_shorter_than(y, x)
      tail(tail(x.next, y, z),
           tail(y.next, z, x),
           tail(z.next, x, y))
    else
      z
    end
  end

  def verify_result(result)
    10 == result
  end
end

class Element
  attr_accessor :val, :next

  def initialize(v)
    @val  = v
    @next = nil
  end

  def length
    if @next.nil?
      1
    else
      1 + @next.length
    end
  end
end
