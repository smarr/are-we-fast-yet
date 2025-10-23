use crate::benchmark::Benchmark;
use std::any::Any;

#[derive(Clone, Default)]
pub struct Towers {
    piles: [Option<Box<TowersDisk>>; 3],
    moves_done: usize,
}

impl Benchmark for Towers {
    fn benchmark(&self) -> Box<dyn Any> {
        let mut piles = self.clone();
        piles.build_tower_at(0, 13);
        piles.moves_done = 0;
        piles.move_disks(13, 0, 1);
        Box::new(piles.moves_done)
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Ok(&i) = result.downcast::<usize>().as_deref() {
            i == 8191
        } else {
            false
        }
    }
}

impl Towers {
    fn push_disk(&mut self, mut disk: Box<TowersDisk>, pile: usize) {
        let top = &mut self.piles[pile];
        if top.is_none() || top.as_ref().unwrap().get_size() >= disk.get_size() {
            disk.set_next(top.take());
            *top = Some(disk);
        } else {
            panic!("Cannot put a big disk on a smaller one")
        }
    }

    fn pop_disk_from(&mut self, pile: usize) -> Box<TowersDisk> {
        let pile_top = &mut self.piles[pile];
        let mut top = pile_top
            .take()
            .expect("Attempting to remove a disk from an empty pile");
        *pile_top = top.get_next();
        top
    }

    fn move_top_disk(&mut self, from_pile: usize, to_pile: usize) {
        let disk = self.pop_disk_from(from_pile);
        self.push_disk(disk, to_pile);
        self.moves_done += 1;
    }

    fn build_tower_at(&mut self, pile: usize, disks: usize) {
        for i in (0..disks).rev() {
            self.push_disk(Box::new(TowersDisk::new(i)), pile);
        }
    }

    fn move_disks(&mut self, disks: usize, from_pile: usize, to_pile: usize) {
        if disks == 1 {
            self.move_top_disk(from_pile, to_pile);
        } else {
            let other_pile = (3 - from_pile) - to_pile;
            self.move_disks(disks - 1, from_pile, other_pile);
            self.move_top_disk(from_pile, to_pile);
            self.move_disks(disks - 1, other_pile, to_pile);
        }
    }
}

#[derive(Clone, Default)]
struct TowersDisk {
    size: usize,
    next: Option<Box<TowersDisk>>,
}

impl TowersDisk {
    fn new(size: usize) -> TowersDisk {
        TowersDisk { size, next: None }
    }

    fn get_size(&self) -> usize {
        self.size
    }

    fn get_next(&mut self) -> Option<Box<TowersDisk>> {
        self.next.take()
    }

    fn set_next(&mut self, next: Option<Box<TowersDisk>>) {
        self.next = next;
    }
}
