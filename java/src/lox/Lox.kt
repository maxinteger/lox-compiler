package lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


var hadError = false
var hadRuntimeError = false
val interpreter = Interpreter()

fun main(args: Array<String>) {
    when {
        args.size > 1 -> {
            System.out.println("Usage: jlox [script]")
            System.exit(64)
        }
        args.size == 1 -> runFile(args[0])
        else -> runPrompt()
    }
}

fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
        print("> ")
        run(reader.readLine())
    }
}

fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))

    if (hadError) System.exit(65)
    if (hadRuntimeError) System.exit(70)
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()
    val parser = Parser(tokens)
    val statements = parser.parse()

    if (hadError) return

    interpreter.interpret(statements)
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun error(token: Token, message: String) {
    if (token.type == TokenType.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, """ at '${token.lexeme}'""", message)
    }
}

fun runtimeError(error: RuntimeError) {
    System.out.println(error.message + "\n[line " + error.token.line + "]")
    hadRuntimeError = true
}

private fun report(line: Int, where: String, message: String) {
    System.err.println(
            "[line $line] Error$where: $message")
    hadError = true
}
