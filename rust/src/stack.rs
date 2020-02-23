use std::slice::Iter;

const STACK_LIMIT: usize = 128;

#[derive(Debug)]
pub struct Stack<T> {
    size: usize,
    list: Vec<T>,
}

impl<T> Stack<T> {
    pub fn new(size: usize) -> Stack<T> {
        if size > 1024 {
            panic!("Stack can not bigger then 1024")
        }
        Stack {
            size,
            list: Vec::new(),
        }
    }

    pub fn push(&mut self, value: T) {
        if self.list.len() >= self.size {
            panic!("Stack limit reached");
        }
        self.list.push(value)
    }

    pub fn pop(&mut self) -> T {
        self.list.pop().unwrap()
    }

    pub fn iter(&self) -> Iter<'_, T> {
        self.list.iter()
    }
}
