package lox

import lox.TokenType.*

class Interpreter : ExprVisitor<Any?>, StmtVisitor<Unit> {

    val globals = Environment()
    private var environment = globals

    fun Interpreter() {
        globals.define(
                "clock", nativeFn(({ _: Interpreter, _: List<Any?> -> System.currentTimeMillis() - 1000 })
        ))
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            lox.runtimeError(error)
        }
    }

    override fun visitCall(expr: Call): Any? {
        val function = evaluate(expr.callee)

        val args = expr.args.map { evaluate(it) }

        when {
            function !is LoxCallable -> throw RuntimeError(expr.param, "Can only call functions and classes")
            args.size != function.arity() -> throw RuntimeError(expr.param, "Expected ${function.arity()} argument(s) but got ${args.size}.")
            else -> return function.call(this, args)
        }
    }

    override fun visitFunction(stmt: Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitReturn(stmt: Return) {
        val value = if (stmt.value != null) evaluate(stmt.value) else null
        throw ReturnAction(value)
    }

    override fun visitVar(stmt: Var) {
        val value = if (stmt.initializer != null) evaluate(stmt.initializer) else null
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhile(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitExpression(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visitIf(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrint(stmt: Print) {
        val value = evaluate(stmt.expression)
        System.out.println(stringify(value))
    }

    override fun visitBlock(stmt: Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    internal fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitGrouping(expr: Grouping): Any? {
        return evaluate(expr)
    }

    override fun visitLiteral(expr: Literal): Any? {
        return expr.value
    }

    override fun visitLogical(expr: Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.op.type == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitUnary(expr: Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.op.type) {
            MINUS -> -checkNumberOperand(expr.op, right) { -it }
            BANG -> !isTruthy(right)
            else -> null
        }
    }

    override fun visitBinary(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.op.type) {
            COMMA -> right
            MINUS -> numberOps(expr.op, left, right) { x, y -> x - y }
            PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> throw RuntimeError(expr.op, "Operands must be two numbers or two strings.")
            }
            SLASH -> divideOp(expr.op, left, right)
            STAR -> numberOps(expr.op, left, right) { x, y -> x * y }
            GREATER -> numberOps(expr.op, left, right) { x, y -> x > y }
            GREATER_EQUAL -> numberOps(expr.op, left, right) { x, y -> x >= y }
            LESS -> numberOps(expr.op, left, right) { x, y -> x < y }
            LESS_EQUAL -> numberOps(expr.op, left, right) { x, y -> x <= y }
            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)
            else -> null
        }
    }

    override fun visitTernary(expr: Ternary): Any? {
        val logic = evaluate(expr.logic)

        return evaluate(if (isTruthy(logic)) expr.left else expr.right)
    }

    override fun visitVariable(expr: Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitAssign(expr: Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    private fun isTruthy(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            null -> false
            else -> true
        }
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        return when {
            a == null && b == null -> false
            a == null -> false
            else -> a == b
        }
    }

    private fun checkNumberOperand(op: Token, operand: Any?, fn: (x: Double) -> Double): Double {
        if (operand is Double)
            return fn(operand)
        throw  RuntimeError(op, "Operand must be a number.")
    }

    private fun <R> numberOps(op: Token, a: Any?, b: Any?, fn: (x: Double, y: Double) -> R): R {
        if (a is Double && b is Double)
            return fn(a, b)
        throw  RuntimeError(op, "Operands must be numbers.")
    }

    private fun divideOp(op: Token, a: Any?, b: Any?): Double {
        if (a is Double && b is Double) {
            if (b != 0.0) return a / b
            else throw RuntimeError(op, "Division by zero")
        }
        throw  RuntimeError(op, "Operands must be numbers.")
    }

    private fun stringify(value: Any?): String {
        return when (value) {
            null -> "nil"
            is Double -> {
                val text = value.toString()
                if (text.endsWith(".0")) text.substring(0, text.length - 2) else text
            }
            else -> value.toString()
        }
    }
}