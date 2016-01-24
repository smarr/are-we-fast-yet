require "./benchmark"
require "./som"

class TowersDisk
  property :next
  property :size

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
    @piles = Array(TowersDisk).new(4)
    build_tower_at(1, 13)
    @moves_done = 0
    move_disks(13, 1, 2)
    @moves_done
  end

  def verify_result(result)
    8191 == result
  end

  def push_disk(disk, pile)
    top = @piles.not_nil![pile]
    if top != nil && disk.size >= top.size
      raise "Cannot put a big disk on a smaller one"
    end

    disk.next = top
    @piles.not_nil![pile] = disk
  end

  def pop_disk_from(pile)
    top = @piles.not_nil![pile]
    if top.nil?
      raise "Attempting to remove a disk from an empty pile"
    end

    @piles.not_nil![pile.not_nil!] = top.next.not_nil!
    top.next = nil
    top
  end

  def move_top_disk(from_pile, to_pile)
    push_disk(pop_disk_from(from_pile), to_pile)
    @moves_done += 1
  end

  def build_tower_at(pile, disks)
    disks.downto(0) { | i |
      push_disk(TowersDisk.new(i), pile)
    }
  end

  def move_disks(disks, from_pile, to_pile)
    if disks == 1
      move_top_disk(from_pile, to_pile)
    else
      other_pile = (6 - from_pile) - to_pile
      move_disks(disks - 1, from_pile, other_pile)
      move_top_disk(from_pile, to_pile)
      move_disks(disks - 1, other_pile, to_pile)
    end
  end
end
