use std::ops::Index;

pub type Value = f64;

pub fn print_value(value: Value) {
    print!("{}", value)
}

#[derive(Debug)]
pub struct ValueArray {
    values: Vec<Value>,
}

impl ValueArray {
    pub fn new() -> ValueArray {
        ValueArray { values: Vec::new() }
    }

    pub fn add(&mut self, value: Value) {
        self.values.push(value)
    }

    pub fn len(&self) -> u8 {
        self.values.len() as u8
    }
}

impl Index<usize> for ValueArray {
    type Output = Value;

    fn index(&self, idx: usize) -> &Self::Output {
        &self.values[idx]
    }
}
