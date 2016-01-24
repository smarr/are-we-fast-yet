class Sieve < Benchmark
  def benchmark
    flags = Array.new(5000, true)
    sieve(flags, 5000)
  end

  def verify_result(result)
    result == 669
  end

  def sieve(flags, size)
    prime_count = 0

    (2..size).each { | i |
      if flags[i - 1]
        prime_count += 1
        k = i + i
        while k <= size
          flags[k - 1] = false
          k += i
        end
      end
    }
    prime_count
  end
end
