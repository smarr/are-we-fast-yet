INITIAL_SIZE = 10

class Pair(K, V)
  property :key
  property :value

  def initialize(key : K, value : V)
    @key   = key
    @value = value
  end
end

class Vector(T)

  def self.with(elem)
    new_vector = self.new(1)
    new_vector.append(elem)
    new_vector
  end

  def initialize(size = 50)
    @storage   = Array(T).new(size, nil)
    @first_idx = 0
    @last_idx  = 0
  end

  def at(idx)
    @storage[idx]
  end

  def append(elem : T)
    if @last_idx >= @storage.size
      # Need to expand capacity first
      new_storage = Array(T).new(2 * @storage.size, nil)
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
      yield @storage[i].not_nil!
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
      e = @storage[i].not_nil!
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
    new_array = Array(T).new(capacity, nil)
    new_last = 0
    found = false

    each { | it |
      if it == obj # Ruby uses .equal?
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

  def sort(&block : T, T -> Bool)
    sort_inner(block)
  end

  def sort_inner(block)
    # Make the argument, block, be the criterion for ordering elements of
    # the receiver.
    # Sort blocks with side effects may not work right.
    if size > 0
      sort_range_inner(@first_idx, @last_idx - 1, block)
    end
  end
  
  def sort_range_no_block(i, j)  # &block
    default_sort(i, j)
  end

  def sort_range(i, j, &block)  # &block
    sort_range_inner(i, j, block)
  end

  def sort_range_inner(i, j, block)  # &block
    # Sort elements i through j of self to be non-descending according to sortBlock.
    #unless block_given?
    #  default_sort(i, j)
    #end

    # The prefix d means the data at that index.

    n = j + 1 - i
    if n <= 1
      return self  # Nothing to sort
    end

    # Sort di, dj
    di = @storage[i]
    dj = @storage[j]

    # i.e., should di precede dj?
    unless block.call(di, dj)
      @storage.swap(i, j)
      tt = di
      di = dj
      dj = tt
    end

    # NOTE: For DeltaBlue, this is never reached.
    if n > 2  # More than two elements.
      ij  = ((i + j) / 2).floor  # ij is the midpoint of i and j.
      dij = @storage[ij]         # Sort di,dij,dj.  Make dij be their median.

      if block.call(di, dij)           # i.e. should di precede dij?
        unless block.call(dij, dj)     # i.e., should dij precede dj?
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
          while k <= l && block.call(dij, @storage[l])  # i.e. while dl succeeds dij
            l -= 1
          end

          k += 1
          while k <= l && block.call(@storage[k], dij)  # i.e. while dij succeeds dk
            k += 1
          end
          k <= l)
          @storage.swap(k, l)
        end

        # Now l < k (either 1 or 2 less), and di through dl are all less than or equal to dk
        # through dj.  Sort those two segments.

        sort_range_inner(i, l, block)
        sort_range_inner(k, j, block)
      end
    end
  end
end

class SomSet(T)
  def initialize(size = INITIAL_SIZE)
    @items = Vector(T).new(size)
  end

  def each
    @items.each do |v|
      yield v
    end
  end

  def has_some
    @items.has_some do |v|
      yield v
    end
  end

  def get_one
    @items.get_one do |v|
      yield v
    end
  end

  def add(obj : T)
    unless contains(obj)
      @items.append(obj)
    end
  end

  def collect(&block : T -> U) # &block
    coll = Vector(U?).new
    each { | e | coll.append(yield e) }
    coll
  end
end

class IdentitySomSet(T) < SomSet(T)
  def contains(obj)
    has_some { | it | it == obj } # in Ruby we use .equal? - is == always identity?
  end
end

class Dictionary(K, V)
  def initialize(size = INITIAL_SIZE)
    @pairs = IdentitySomSet(Pair(K, V)?).new(size)
  end

  def at_put(key : K, value : V)
    pair = pair_at(key)
    if pair.nil?
      @pairs.add(Pair(K, V).new(key, value))
    else
      pair.not_nil!.value = value
    end
  end

  def at(key : K)
    pair = pair_at(key)
    if pair.is_a?(Nil)
      nil
    else
      pair.value
    end
  end

  def pair_at(key : K)
    @pairs.get_one { | p | p.not_nil!.key == key }
  end

  def keys
    @pairs.collect { | p | p.not_nil!.key }
  end
end

class SomRandom
  def initialize
    @seed = 74755
  end

  def next
    @seed = ((@seed * 1309) + 13849) & 65535
  end
end
