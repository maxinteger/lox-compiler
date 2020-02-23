use crate::chunk::{Chunk, OpCode};
use crate::value::print_value;

pub fn disassemble_chunk(chunk: &Chunk, name: &str) {
    println!("== {} ==", name);

    let mut offset: usize = 0;
    while offset < chunk.code.len() {
        offset = disassemble_instruction(chunk, offset);
    }
}

pub fn disassemble_instruction(chunk: &Chunk, offset: usize) -> usize {
    print!("{number:>0width$}", number = offset, width = 4);
    let current_line = chunk.lines.get_line(offset);
    if offset > 0 && current_line == chunk.lines.get_line(offset - 1) {
        print!("   | ");
    } else {
        print!("{:4}", current_line);
    }
    let instruction = &chunk.code[offset];
    match instruction {
        OpCode::OpReturn => return simple_instruction("OP_RETURN", offset),
        OpCode::OpAdd => return simple_instruction("OP_ADD", offset),
        OpCode::OpSubtract => return simple_instruction("OP_SUBTRACT", offset),
        OpCode::OpMultiply => return simple_instruction("OP_MULTIPLY", offset),
        OpCode::OpDivide => return simple_instruction("OP_DIVIDE", offset),
        OpCode::OpNegate => return simple_instruction("OP_NEGATE", offset),
        OpCode::OpConstant => return constant_instruction("OP_CONSTANT", chunk, offset),
        OpCode::Reg(_) => panic!("Invalid opCode"),
    }
}

fn simple_instruction(name: &str, offset: usize) -> usize {
    println!(" {}", name);
    offset + 1
}

fn constant_instruction(name: &str, chunk: &Chunk, offset: usize) -> usize {
    let constant = chunk.code[offset + 1];
    match constant {
        OpCode::Reg(val) => {
            print!(" {:<16} {:4} ", name, val);
            print_value(chunk.constants[val as usize]);
        }
        _ => panic!("Invalid item"),
    }
    println!();
    offset + 2
}
