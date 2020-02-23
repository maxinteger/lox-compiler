use crate::chunk::{Chunk, OpCode};
use crate::debug;
use crate::stack::Stack;
use crate::value::{print_value, Value};

#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum InterpretResult {
    InterpretOk,
    InterpretCompileError,
    InterpretRuntimeError,
}

#[derive(Debug)]
pub struct VM {
    chunk: Chunk,
    ip: usize,
    stack: Stack<Value>,
}

impl VM {
    pub fn new(chunk: Chunk) -> VM {
        VM {
            chunk,
            ip: 0,
            stack: Stack::new(256),
        }
    }

    pub fn interpret(&mut self, _source: &str) -> InterpretResult {
        return self.run();
    }

    fn run(&mut self) -> InterpretResult {
        loop {
            if cfg!(debug_assertions) {
                print!("          ");
                for val in self.stack.iter() {
                    print!("[{}]", val)
                }
                println!();
                debug::disassemble_instruction(&self.chunk, self.ip);
            }
            match self.next() {
                OpCode::OpReturn => {
                    let val = self.stack.pop();
                    print_value(val);
                    return InterpretResult::InterpretOk;
                }
                OpCode::OpNegate => {
                    let val = self.stack.pop();
                    self.stack.push(-val)
                }
                OpCode::OpAdd => {
                    let a = self.stack.pop();
                    let b = self.stack.pop();
                    self.stack.push(a + b)
                }
                OpCode::OpSubtract => {
                    let a = self.stack.pop();
                    let b = self.stack.pop();
                    self.stack.push(a - b)
                }
                OpCode::OpMultiply => {
                    let a = self.stack.pop();
                    let b = self.stack.pop();
                    self.stack.push(a * b)
                }
                OpCode::OpDivide => {
                    let a = self.stack.pop();
                    let b = self.stack.pop();
                    self.stack.push(a / b)
                }
                OpCode::OpConstant => match self.next() {
                    OpCode::Reg(idx) => {
                        let constant = self.chunk.constants[idx as usize];
                        self.stack.push(constant);
                    }
                    _ => panic!("Register OpCode expected!"),
                },
                OpCode::Reg(_) => {}
            }
        }
    }

    fn next(&mut self) -> OpCode {
        self.ip += 1;
        self.chunk.code[self.ip - 1]
    }
}
