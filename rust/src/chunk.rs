use crate::line_number::LineNumber;
use crate::value::{Value, ValueArray};

#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum OpCode {
    OpConstant,
    OpNegate,
    OpAdd,
    OpSubtract,
    OpMultiply,
    OpDivide,
    OpReturn,
    Reg(u8),
}

#[derive(Debug)]
pub struct Chunk {
    pub code: Vec<OpCode>,
    pub lines: LineNumber,
    pub constants: ValueArray,
}

impl Chunk {
    pub fn new() -> Chunk {
        Chunk {
            code: Vec::new(),
            lines: LineNumber::new(),
            constants: ValueArray::new(),
        }
    }

    pub fn add_chunk(&mut self, byte: OpCode, line: usize) {
        self.code.push(byte);
        self.lines.add_line(line)
    }

    pub fn add_constant(&mut self, value: Value) -> u8 {
        self.constants.add(value);
        self.constants.len() - 1
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn init_chunk() {
        let chunk = Chunk::new();
        assert_eq!(chunk.code.len(), 0);
        assert_eq!(chunk.constants.len(), 0);
    }

    #[test]
    fn add_to_chunk() {
        let mut chunk = Chunk::new();
        chunk.add_chunk(OpCode::OpReturn, 42);

        assert_eq!(chunk.code.len(), 1);
        assert_eq!(chunk.code[0], OpCode::OpReturn);
        assert_eq!(chunk.constants.len(), 0);
    }
}
