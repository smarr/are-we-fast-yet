# frozen_string_literal: true

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

require_relative 'benchmark'

class TowersDisk
  attr_accessor :next
  attr_reader   :size

  def initialize(size)
    @size = size
    @next = nil
  end
end

class Towers < Benchmark
  def initialize
    @piles = nil
    @moves_done = 0
  end

  def benchmark
    @piles = Array.new(3)
    build_tower_at(0, 13)
    @moves_done = 0
    move_disks(13, 0, 1)
    @moves_done
  end

  def verify_result(result)
    8191 == result
  end

  def push_disk(disk, pile)
    top = @piles[pile]
    if top && disk.size >= top.size
      raise 'Cannot put a big disk on a smaller one'
    end

    disk.next = top
    @piles[pile] = disk
  end

  def pop_disk_from(pile)
    top = @piles[pile]
    raise 'Attempting to remove a disk from an empty pile' unless top

    @piles[pile] = top.next
    top.next = nil
    top
  end

  def move_top_disk(from_pile, to_pile)
    push_disk(pop_disk_from(from_pile), to_pile)
    @moves_done += 1
  end

  def build_tower_at(pile, disks)
    disks.downto(0) do |i|
      push_disk(TowersDisk.new(i), pile)
    end
  end

  def move_disks(disks, from_pile, to_pile)
    if disks == 1
      move_top_disk(from_pile, to_pile)
    else
      other_pile = (3 - from_pile) - to_pile
      move_disks(disks - 1, from_pile, other_pile)
      move_top_disk(from_pile, to_pile)
      move_disks(disks - 1, other_pile, to_pile)
    end
  end
end
