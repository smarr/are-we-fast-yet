# This code is derived from the SOM benchmarks, see AUTHORS.md file.
#
# Copyright (c) 2015-2016 Stefan Marr <git@stefan-marr.de>
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
require "math"


INITIAL_SIZE = 10
INITIAL_CAPACITY = 16

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

  def initialize(size = 0)
    @storage = size == 0 ? nil : Array(T).new(size, nil)
    @first_idx = 0
    @last_idx  = 0
  end

  def at(idx)
    if @storage == nil || idx >= @storage.not_nil!.size
      return nil
    end
    @storage.not_nil![idx]
  end

  def at_put(idx, val : T)
    if @storage == nil
      @storage = Array(T).new(Math.max(idx + 1, INITIAL_SIZE), nil)
    elsif idx >= @storage.not_nil!.size
      new_length = @storage.not_nil!.size
      while new_length <= idx
        new_length *= 2
      end
      new_storage = Array(T).new(new_length, nil)
      @storage.not_nil!.each_index { | i |
        new_storage[i] = @storage.not_nil![i]
      }
      @storage = new_storage
    end
    @storage.not_nil![idx] = val
    if @last_idx < idx + 1
      @last_idx = idx + 1
    end
  end

  def append(elem : T)
    if @storage == nil
      @storage = Array(T).new(INITIAL_SIZE, nil)
    elsif @last_idx >= @storage.not_nil!.size
      # Need to expand capacity first
      new_storage = Array(T).new(2 * @storage.not_nil!.size, nil)
      @storage.not_nil!.each_index { | i |
        new_storage[i] = @storage.not_nil![i]
      }
      @storage = new_storage
    end

    @storage.not_nil![@last_idx] = elem
    @last_idx += 1
    self
  end

  def empty?
    @last_idx == @first_idx
  end

  def each # &block
    (@first_idx..(@last_idx - 1)).each { | i |
      yield @storage.not_nil![i].not_nil!
    }
  end

  def has_some
    (@first_idx..(@last_idx - 1)).each { | i |
      if yield @storage.not_nil![i]
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
    @storage.not_nil![@first_idx - 1]
  end

  def remove(obj)
    if @storage == nil || empty?
      return false
    end

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

  def remove_all
    @first_idx = 0
    @last_idx  = 0

    if @storage
      @storage = Array(T).new(@storage.not_nil!.size, nil)
    end
  end

  def size
    @last_idx - @first_idx
  end

  def capacity
    @storage == nil ? 0 : @storage.not_nil!.size
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
    di = @storage.not_nil![i]
    dj = @storage.not_nil![j]

    # i.e., should di precede dj?
    unless block.call(di, dj)
      @storage.not_nil!.swap(i, j)
      tt = di
      di = dj
      dj = tt
    end

    # NOTE: For DeltaBlue, this is never reached.
    if n > 2  # More than two elements.
      ij  = ((i + j) / 2).floor.to_i32 # ij is the midpoint of i and j.
      dij = @storage.not_nil![ij]               # Sort di,dij,dj.  Make dij be their median.

      if block.call(di, dij)           # i.e. should di precede dij?
        unless block.call(dij, dj)     # i.e., should dij precede dj?
          @storage.not_nil!.swap(j, ij)
          dij = dj
        end
      else                       # i.e. di should come after dij
        @storage.not_nil!.swap(i, ij)
        dij = di
      end

      if n > 3  # More than three elements.
        # Find k>i and l<j such that dk,dij,dl are in reverse order.
        # Swap k and l.  Repeat this procedure until k and l pass each other.
        k = i
        l = j - 1

        while (
          while k <= l && block.call(dij, @storage.not_nil![l])  # i.e. while dl succeeds dij
            l -= 1
          end

          k += 1
          while k <= l && block.call(@storage.not_nil![k], dij)  # i.e. while dij succeeds dk
            k += 1
          end
          k <= l)
          @storage.not_nil!.swap(k, l)
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
  
  def size
    @items.size
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

  def contains(obj : T)
    has_some { | it | it == obj }
  end
end

class IdentitySomSet(T) < SomSet(T)
  def initialize(size = INITIAL_SIZE)
    super(size)
  end

  def contains(obj)
    has_some { | it | it == obj } # in Ruby we use .equal? - is == always identity?
  end
end

class Entry(K, V)
  getter :hash, :key
  property :value, :next

  def initialize(hash : Int32, key : K, value : V, next_ : Entry(K, V)?)
    @hash  = hash
    @key   = key
    @value = value
    @next  = next_
  end

  def match(hash, key)
    @hash == hash && @key == key
  end
end

class Dictionary(K, V)
  getter :size

  def initialize(size = INITIAL_CAPACITY)
    @buckets = Array(Entry(K, V)?).new(size, nil)
    @size    = 0
  end

  def hash(key)
    unless key
      return 0
    end

    hash = key.custom_hash
    hash ^ hash >> 16
  end

  def empty?
    @size == 0
  end

  def get_bucket_idx(hash)
    (@buckets.size - 1) & hash
  end

  def get_bucket(hash)
    @buckets[get_bucket_idx(hash)]
  end

  def at(key : K)
    hash = hash(key)
    e = get_bucket(hash)

    while e
      if e.match(hash, key)
        return e.value
      end
      e = e.next
    end
    nil
  end

  def contains_key(key)
    hash = hash(key)
    e = get_bucket(hash)

    while e
      if e.match(hash, key)
        return true
      end
      e = e.next
    end
    false
  end

  def at_put(key : K, value : V)
    hash = hash(key)
    i = get_bucket_idx(hash)
    current = @buckets[i]

    unless current
      @buckets[i] = new_entry(key, value, hash)
      @size += 1
    else
      insert_bucket_entry(key, value, hash, current)
    end

    if @size > @buckets.size
      resize
    end
  end

  def new_entry(key : K, value : V, hash : Int32) : Entry(K, V)
    Entry.new(hash, key, value, nil)
  end

  def insert_bucket_entry(key, value, hash, head)
    current = head

    while true
      if current.match(hash, key)
        current.value = value
        return
      end
      unless current.next
        @size += 1
        current.next = new_entry(key, value, hash)
        return
      end
      current = current.next.not_nil!
    end
  end

  def resize
    old_storage = @buckets
    @buckets = Array(Entry(K, V)?).new(old_storage.size * 2, nil)
    transfer_entries(old_storage)
  end

  def transfer_entries(old_storage)
    old_storage.each_with_index { |current, i|
      if current
        old_storage[i] = nil

        unless current.next
          @buckets[current.hash & (@buckets.size - 1)] = current
        else
          split_bucket(old_storage, i, current)
        end
      end
    }
  end

  def split_bucket(old_storage, i, head)
    lo_head = nil
    lo_tail = nil
    hi_head = nil
    hi_tail = nil
    current = head

    while current
      if (current.hash & old_storage.size) == 0
        unless lo_tail
          lo_head = current
        else
          lo_tail.next = current
        end
        lo_tail = current
      else
        unless hi_tail
          hi_head = current
        else
          hi_tail.next = current
        end
        hi_tail = current
      end
      current = current.next
    end

    if lo_tail
      lo_tail.next = nil
      @buckets[i] = lo_head
    end
    if hi_tail
      hi_tail.next = nil
      @buckets[i + old_storage.size] = hi_head
    end
  end

  def remove_all
    @buckets = Array(Entry(K, V)?).new(@buckets.size, nil)
    @size = 0
  end

  def keys
    keys = Vector(K?).new(@size)
    @buckets.each_index { |i|
      current = @buckets[i]
      while current
        keys.append(current.key)
        current = current.next
      end
    }
    keys
  end

  def values
    vals = Vector.new(@size)
    @buckets.each_index { |i|
      current = @buckets[i]
      while current
        vals.append(current.value)
        current = current.next
      end
    }
    vals
  end
end

class IdEntry(K, V) < Entry(K, V)
  def match(hash, key)
    @hash == hash && (@key == key)  ## was @key.equal? key in Ruby
  end
end

class IdentityDictionary(K, V) < Dictionary(K, V)
  ## This does not seem to work with the Crystal type system, but it is not
  ## strictly necessary, because it isn't used (see IdEntry issue, too)
  # def new_entry(key : K, value : V, hash : Int32) : Entry(K, V)
  #   IdEntry.new(hash, key, value, nil)
  # end
end

class SomRandom
  def initialize
    @seed = 74755
  end

  def next
    @seed = ((@seed * 1309) + 13849) & 65535
  end
end
