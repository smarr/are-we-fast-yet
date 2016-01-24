INITIAL_SIZE = 10

class Pair
  attr_accessor :key
  attr_accessor :value

  def initialize(key, value)
    @key   = key
    @value = value
  end
end

class Vector

  def self.with(elem)
    new_vector = self.new(1)
    new_vector.append(elem)
    new_vector
  end

  def initialize(size = 50)
    @storage   = Array.new(size)
    @first_idx = 0
    @last_idx  = 0
  end

  def at(idx)
    @storage[idx]
  end

  def append(elem)
    if @last_idx >= @storage.size
      # Need to expand capacity first
      new_storage = Array.new(2 * @storage.size)
      @storage.each_index { | i |
        new_storage[i] = @storage[i]
      }
      @storage = new_storage
    end

    @storage[@last_idx] = elem
    @last_idx += 1
    self
  end

  def empty?
    @last_idx == @first_idx
  end

  def each # &block
    (@first_idx..(@last_idx - 1)).each { | i |
      yield @storage[i]
    }
  end

  def has_some
    (@first_idx..(@last_idx - 1)).each { | i |
      if yield @storage[i]
        return true
      end
    }
    false
  end

  def get_one
    (@first_idx..(@last_idx - 1)).each { | i |
      e = @storage[i]
      if yield e
        return e
      end
    }
    nil
  end

  def remove_first
    if empty?
      return nil
    end

    @first_idx += 1
    @storage[@first_idx - 1]
  end

  def remove(obj)
    new_array = Array.new(capacity)
    new_last = 0
    found = false

    each { | it |
      if it.equal? obj
        found = true
      else
        new_array[new_last] = it
        new_last += 1
      end
    }

    @storage  = new_array
    @last_idx = new_last
    @first_idx = 0
    found
  end

  def size
    @last_idx - @first_idx
  end

  def capacity
    @storage.size
  end

  def sort(&block)
    # Make the argument, block, be the criterion for ordering elements of
    # the receiver.
    # Sort blocks with side effects may not work right.
    if size > 0
      sort_range(@first_idx, @last_idx - 1, &block)
    end
  end

  def sort_range(i, j)  # &block
    # Sort elements i through j of self to be non-descending according to sortBlock.
    unless block_given?
      default_sort(i, j)
    end

    # The prefix d means the data at that index.

    n = j + 1 - i
    if n <= 1
      return self  # Nothing to sort
    end

    # Sort di, dj
    di = @storage[i]
    dj = @storage[j]

    # i.e., should di precede dj?
    unless yield di, dj
      @storage.swap(i, j)
      tt = di
      di = dj
      dj = tt
    end

    # NOTE: For DeltaBlue, this is never reached.
    if n > 2  # More than two elements.
      ij  = ((i + j) / 2).floor  # ij is the midpoint of i and j.
      dij = @storage[ij]         # Sort di,dij,dj.  Make dij be their median.

      if yield di, dij           # i.e. should di precede dij?
        unless yield dij, dj     # i.e., should dij precede dj?
          @storage.swap(j, ij)
          dij = dj
        end
      else                       # i.e. di should come after dij
        @storage.swap(i, ij)
        dij = di
      end

      if n > 3  # More than three elements.
        # Find k>i and l<j such that dk,dij,dl are in reverse order.
        # Swap k and l.  Repeat this procedure until k and l pass each other.
        k = i
        l = j - 1

        while (
          while k <= l and yield dij, @storage[l]  # i.e. while dl succeeds dij
            l -= 1
          end

          k += 1
          while k <= l and yield @storage[k], dij  # i.e. while dij succeeds dk
            k += 1
          end
          k <= l)
          @storage.swap(k, l)
        end

        # Now l < k (either 1 or 2 less), and di through dl are all less than or equal to dk
        # through dj.  Sort those two segments.

        sort_range(i, l, &block)
        sort_range(k, j, &block)
      end
    end
  end
end

class Set
  def initialize(size = INITIAL_SIZE)
    @items = Vector.new(size)
  end

  def each(&block)
    @items.each(&block)
  end

  def has_some(&block)
    @items.has_some(&block)
  end

  def get_one(&block)
    @items.get_one(&block)
  end

  def add(obj)
    unless contains(obj)
      @items.append(obj)
    end
  end

  def collect # &block
    coll = Vector.new
    each { | e | coll.append(yield e) }
    coll
  end
end

class IdentitySet < Set
  def contains(obj)
    has_some { | it | it.equal? obj }
  end
end

class Dictionary
  def initialize(size = INITIAL_SIZE)
    @pairs = IdentitySet.new(size)
  end

  def at_put(key, value)
    pair = pair_at(key)
    if pair.nil?
      @pairs.add(Pair.new(key, value))
    else
      pair.value = value
    end
  end

  def at(key)
    pair = pair_at(key)
    if pair.nil?
      nil
    else
      pair.value
    end
  end

  def pair_at(key)
    @pairs.get_one { | p | p.key == key }
  end

  def keys
    @pairs.collect { | p | p.key }
  end
end

class Random
  def initialize
    @seed = 74755
  end

  def next
    @seed = ((@seed * 1309) + 13849) & 65535
  end
end
