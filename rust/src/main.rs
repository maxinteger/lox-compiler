mod chunk;
mod compiler;
mod debug;
mod line_number;
mod scanner;
mod stack;
mod value;
mod vm;
use self::chunk::Chunk;
use self::vm::VM;
use crate::vm::InterpretResult;
use std::io::Write;
use std::{env, fs, io};

fn main() {
    let args: Vec<String> = env::args().collect();

    match args.len() {
        1 => repl(),
        2 => run_file(&args[1]),
        _ => {
            eprintln!("Usage: clox [path]");
            std::process::exit(64)
        }
    }
}

fn repl() {
    loop {
        let line = get_input("> ");
        if line.is_empty() {
            return println!("Bye!");
        } else {
            println!("> {}", line)
        }
    }
}

fn run_file(file_path: &str) {
    let source = fs::read_to_string(file_path).expect("Something went wrong reading the file");

    let chunk = Chunk::new();
    let mut vm = VM::new(chunk);
    let result = vm.interpret(&source[..]);

    match result {
        InterpretResult::InterpretOk => std::process::exit(0),
        InterpretResult::InterpretCompileError => std::process::exit(65),
        InterpretResult::InterpretRuntimeError => std::process::exit(70),
    }
}

fn get_input(prompt: &str) -> String {
    print!("{}", prompt);
    io::stdout().flush();
    let mut input = String::new();
    match io::stdin().read_line(&mut input) {
        Ok(_goes_into_input_above) => {}
        Err(_no_updates_is_fine) => {}
    }
    input.trim().to_string()
}
