class Permute < Benchmark
  def initialize
    @count = 0
    @v     = nil
  end

  def benchmark
    @count = 0
    @v = Array.new(6, 0)
    permute(6)
    @count
  end

  def permute(n)
    @count += 1
    if n != 0
      n1 = n - 1
      permute(n1)

      n1.downto(0).each { | i |
        swap(n1, i)
        permute(n1)
        swap(n1, i)
      }
    end
  end

  def swap(i, j)
    tmp = @v[i]
    @v[i] = @v[j]
    @v[j] = tmp
  end

  def verify_result(result)
    result == 8660
  end
end