#pragma once

#include <array>
#include <memory>

#include "benchmark.h"


class TowersDisk {
private:
  const int32_t size;
  TowersDisk*   next;

public:
  
  explicit TowersDisk(int32_t size) : size(size), next(nullptr) {}
  
  ~TowersDisk() {
    if (next != nullptr) {
      delete next;
    }
  }
  
  int32_t get_size() const { return size; }
  
  TowersDisk* get_next() const { return next; }
  void set_next(TowersDisk* disk) { next = disk; }
};


class Towers : public Benchmark {
private:
  std::array<TowersDisk*, 3> piles;
  int32_t moves_done;

public:
  
  virtual void* benchmark() {
    piles = std::array<TowersDisk*, 3>();
    build_tower_at(0, 13);
    moves_done = 0;
    move_disks(13, 0, 1);
    
    std::for_each(piles.begin(), piles.end(), [](TowersDisk*& disk){ delete disk; });
    return (void*) (intptr_t) moves_done;
  }
  
  virtual bool verify_result(void* result) {
    return 8191 == (int32_t) (intptr_t) result;
  }
  
private:
  
  void push_disk(TowersDisk* disk, int32_t pile) {
    TowersDisk* top = piles[pile];
    if (!(top == nullptr) && (disk->get_size() >= top->get_size())) {
      std::cout << "Cannot put a big disk on a smaller one";
      exit(1); // TODO: should this be an exception?
    }
    
    disk->set_next(top);
    piles[pile] = disk;
  }
  
  TowersDisk* pop_disk_from(int32_t pile) {
    TowersDisk* top = piles[pile];
    if (top == nullptr) {
      std::cout << "Attempting to remove a disk from an empty pile";
      exit(1); // TODO: should this be an exception?
    }
    
    piles[pile] = top->get_next();
    top->set_next(nullptr);
    return top;
  }
  
  void move_top_disk(int32_t from_pile, int32_t to_pile) {
    push_disk(pop_disk_from(from_pile), to_pile);
    moves_done++;
  }
  
  void build_tower_at(int32_t pile, int32_t disks) {
    for (int32_t i = disks; i >= 0; i--) {
      push_disk(new TowersDisk(i), pile);
    }
  }
  
  void move_disks(int32_t disks, int32_t from_pile, int32_t to_pile) {
    if (disks == 1) {
      move_top_disk(from_pile, to_pile);
    } else {
      int32_t other_pile = (3 - from_pile) - to_pile;
      move_disks(disks - 1, from_pile, other_pile);
      move_top_disk(from_pile, to_pile);
      move_disks(disks - 1, other_pile, to_pile);
    }
  }
};
