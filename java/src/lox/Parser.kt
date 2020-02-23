package lox

import lox.TokenType.*

private class ParseError : RuntimeException()

const val MAX_ARGS_LENGTH = 8

class Parser(private val tokens: List<Token>) {
    private var current: Int = 0

    fun parse(): List<Stmt> {
        val statements = arrayListOf<Stmt>()
        while (!isAtEnd()) {
            val statement = declaration()
            if (statement != null) {
                statements.add(statement)
            }
        }
        return statements
    }


    private fun statement(): Stmt {
        if (match(FUN)) return function("function")
        if (match(IF)) return ifStatement()
        if (match(FOR)) return forStatement()
        if (match(PRINT)) return printStatement()
        if (match(RETURN)) return returnStatement()
        if (match(WHILE)) return whileStatement()
        if (match(LEFT_BRACE)) return Block(block())
        return expressionStatement()
    }

    private fun function(kind: String): Stmt {
        val name = consume(IDENTIFIER, "Expect $kind name.")
        consume(LEFT_PAREN, "Expected '(' after $kind name.")

        val params = arrayListOf<Token>()
        if (!match(RIGHT_PAREN)) {
            do {
                if (params.size >= MAX_ARGS_LENGTH) {
                    error(peek(), "Cannot have more then $MAX_ARGS_LENGTH parameters.")
                }
                params.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expected ')' after parameters.")
        consume(LEFT_BRACE, "Expected '{' before $kind body.")
        return Function(name, params, block())
    }

    private fun declaration(): Stmt? {
        try {
            if (match(VAR)) return varDeclaration()
            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name")
        val initializer = if (match(EQUAL)) expression() else null
        consume(SEMICOLON, "Expect ';' after variable declaration")
        return Var(name, initializer)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after while condition.")
        val body = statement()

        return While(condition, body)
    }

    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'for'.")
        val initializer = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        val condition = when {
            !match(SEMICOLON) -> expression()
            else -> Literal(true)
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        val increment = when {
            !match(RIGHT_PAREN) -> expression()
            else -> null
        }
        consume(SEMICOLON, "Expect ')' after for clauses.")

        var body = statement()

        if (increment != null) {
            body = Block(listOf(body, Expression(increment)))
        }

        body = While(condition, body)

        if (initializer != null) {
            body = Block(listOf(initializer, body))
        }

        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else null

        return If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()
        val value = if (!check(SEMICOLON)) expression() else null
        consume(SEMICOLON, "Expect ';' after return")
        return Return(keyword, value)
    }

    private fun expressionStatement(): Stmt {
        val stmt = Expression(expression())
        consume(SEMICOLON, "Expect ';' after expression")
        return stmt
    }

    private fun block(): List<Stmt> {
        val statements = arrayListOf<Stmt>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            declaration()?.let { statements.add(it) }
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun expression(): Expr {
        return comma()
    }

    private fun comma(): Expr {
        return binaryOp({ assignment() }, COMMA)
    }

    private fun assignment(): Expr {
        val expr = ternary()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Variable) {
                return Assign(expr.name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun ternary(): Expr {
        val expr = or()
        if (match(QUESTION_MARK)) {
            val trueSide = expression()
            consume(COLON, "Missing ternary colon.")
            val falseSide = expression()
            return Ternary(expr, trueSide, falseSide)
        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(OR)) {
            val op = previous()
            val right = and()
            expr = Logical(expr, op, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND)) {
            val op = previous()
            val right = equality()
            expr = Logical(expr, op, right)
        }
        return expr
    }

    private fun equality(): Expr {
        return binaryOp({ comparison() }, BANG_EQUAL, EQUAL_EQUAL)
    }

    private fun comparison(): Expr {
        return binaryOp({ addition() }, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)
    }

    private fun addition(): Expr {
        return binaryOp({ multiplication() }, MINUS, PLUS)
    }

    private fun multiplication(): Expr {
        return binaryOp({ unary() }, SLASH, STAR)
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val op = previous()
            val right = unary()
            return Unary(op, right)
        }
        return call()
    }

    private fun call(): Expr {
        var expr = primary()
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            }
            break
        }
        return expr
    }

    private fun primary(): Expr {
        return when {
            match(FALSE) -> Literal(false)
            match(TRUE) -> Literal(true)
            match(NIL) -> Literal(null)
            match(NUMBER, STRING) -> Literal(previous().literal)
            match(IDENTIFIER) -> Variable(previous())
            match(LEFT_PAREN) -> {
                val expr = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression")
                return Grouping(expr)
            }
            else -> throw error(peek(), "Expect expression")
        }

    }

    private fun finishCall(callee: Expr): Expr {
        val args = arrayListOf<Expr>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (args.size > MAX_ARGS_LENGTH) {
                    error(peek(), "Cannot have more then $MAX_ARGS_LENGTH arguments")
                }
                args.add(assignment())
            } while (match(COMMA))
        }

        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")

        return Call(callee, paren, args)
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }


    private fun binaryOp(base: () -> Expr, vararg tokens: TokenType): Expr {
        var expr = base()

        while (match(*tokens)) {
            val op = previous()
            val right = base()
            expr = Binary(expr, op, right)
        }
        return expr
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }


    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type === type
    }

    private fun isAtEnd(): Boolean {
        return peek().type == EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }


    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }

    private fun error(token: Token, message: String): ParseError {
        lox.error(token, message)
        return ParseError()
    }
}
