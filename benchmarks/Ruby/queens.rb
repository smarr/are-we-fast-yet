class Queens < Benchmark
  def initialize
    @free_maxs  = nil
    @free_rows  = nil
    @free_mins  = nil
    @queen_rows = nil
  end

  def benchmark
    result = true
    10.times {
      result = result && queens
    }
  end

  def verify_result(result)
    result
  end

  def queens
    @free_rows  = Array.new( 8, true)
    @free_maxs  = Array.new(16, true)
    @free_mins  = Array.new(16, true)
    @queen_rows = Array.new( 8, -1)

    return place_queen(0)
  end

  def place_queen(c)
    8.times { | r |
      if get_row_column(r, c)
        @queen_rows[r] = c
        set_row_column(r, c, false)

        if c == 7
          return true
        end

        if place_queen(c + 1)
          return true
        end

        set_row_column(r, c, true)
      end
    }
    false
  end

  def get_row_column(r, c)
    return @free_rows[r] && @free_maxs[c + r] && @free_mins[c - r + 7]
  end

  def set_row_column(r, c, v)
    @free_rows[r        ] = v
    @free_maxs[c + r    ] = v
    @free_mins[c - r + 7] = v
  end
end
