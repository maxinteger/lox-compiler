package lox


class Scanner(private var source: String) {
    private val tokens = arrayListOf<Token>()
    private val keywords: Map<String, TokenType> = hashMapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
    )

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '?' -> addToken(TokenType.QUESTION_MARK)
            ':' -> addToken(TokenType.COLON)

            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            '/' -> when {
                match('/') -> oneLineComment()
                match('*') -> multiLineComment()
                else -> addToken(TokenType.SLASH)
            }
            ' ', '\r', '\t' -> {
            }
            '\n' -> line++

            '"' -> string()

            else -> when {
                c.isAlpha() -> identifier()
                c.isDigit() -> number()
                else -> lox.error(line, "Unexpected character")
            }
        }
    }

    private fun identifier() {
        while (peek().isAlphaNumeric()) advance()

        val text = source.substring(start, current)
        addToken(keywords.get(text) ?: TokenType.IDENTIFIER)
    }


    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            return lox.error(line, "Unterminated string.")
        }

        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun number() {
        while (peek().isDigit()) advance()

        if (peek() == '.' && peekNext().isDigit()) {
            advance()
            while (peek().isDigit()) advance()
        }
        val value = source.substring(start, current)
        addToken(TokenType.NUMBER, value.toDouble())
    }

    private fun oneLineComment() {
        while (peek() != '\n' && !isAtEnd()) advance()
    }

    private fun multiLineComment() {
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            return lox.error(line, "Unterminated multiline comment.")
        }

        advance() // *
        advance() // /
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun advance(): Char {
        current++
        return source[current-1]
    }

    private fun peek(): Char {
        if (isAtEnd()) return 0.toChar()
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return 0.toChar()
        return source[current + 1]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

}

fun Char.isAlpha(): Boolean {
    return (this >= 'a' && this <= 'z') || (this >= 'A' && this <= 'Z') || this == '_'
}

fun Char.isAlphaNumeric(): Boolean {
    return this.isAlpha() || this.isDigit()
}
