use crate::benchmark::Benchmark;
use std::any::Any;
use std::rc::Rc;

#[derive(Default)]
pub struct List;

impl Benchmark for List {
    fn benchmark(&self) -> Box<dyn Any> {
        let result = tail(&make_list(15), &make_list(10), &make_list(6));
        return Box::new(result.unwrap().length());
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Ok(result) = result.downcast::<usize>() {
            10 == *result
        } else {
            false
        }
    }
}

struct Element {
    #[allow(dead_code)]
    val: Box<dyn Any>,
    next: Option<ElementRef>,
}

type ElementRef = Rc<Element>;

impl Element {
    fn new(val: Box<dyn Any>) -> Element {
        Element { val, next: None }
    }

    fn length(&self) -> usize {
        match &self.next {
            None => 1,
            Some(next) => 1 + next.length(),
        }
    }

    #[allow(dead_code)]
    fn get_val(&self) -> &Box<dyn Any> {
        &self.val
    }

    #[allow(dead_code)]
    fn set_val(&mut self, val: Box<dyn Any>) {
        self.val = val;
    }

    fn get_next(&self) -> &Option<ElementRef> {
        &self.next
    }

    fn set_next(&mut self, next: Option<ElementRef>) {
        self.next = next;
    }
}

fn make_list(length: usize) -> Option<ElementRef> {
    if length == 0 {
        None
    } else {
        let mut e = Element::new(Box::new(length));
        e.set_next(make_list(length - 1));
        Some(Rc::new(e))
    }
}

fn is_shorter_than(x: &Option<ElementRef>, y: &Option<ElementRef>) -> bool {
    let mut x_tail = x;
    let mut y_tail = y;

    while let Some(y) = y_tail {
        if let Some(x) = x_tail {
            x_tail = x.get_next();
            y_tail = y.get_next();
        } else {
            return true;
        }
    }
    false
}

fn tail(
    x: &Option<ElementRef>,
    y: &Option<ElementRef>,
    z: &Option<ElementRef>,
) -> Option<ElementRef> {
    if is_shorter_than(y, x) {
        let x_next = x.as_ref().unwrap().get_next();
        let y_next = y.as_ref().unwrap().get_next();
        let z_next = z.as_ref().unwrap().get_next();
        tail(
            &tail(x_next, y, z),
            &tail(y_next, z, x),
            &tail(z_next, x, y),
        )
    } else {
        z.clone()
    }
}
