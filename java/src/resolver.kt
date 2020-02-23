package lox

import java.util.*
import kotlin.collections.HashMap

class Resolver(private val interpreter: Interpreter) : ExprVisitor<Unit>, StmtVisitor<Unit> {
    private val scopes: Stack<HashMap<String, Boolean>> = Stack()

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        scopes.peek()[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

    private fun beginScope() {
        scopes.push(hashMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun resolveLocal(expression: Expr, name: Token) {
        for (i in scopes.size - 1..0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expression, scopes.size - 1 - i)
            }
        }
    }

    private fun resolveFunction(fn: Function) {
        beginScope()
        fn.parameters.forEach {
            declare(it)
            define(it)
        }
        resolve(fn.body)
        endScope()
    }

    private fun resolve(statements: List<Stmt>) {
        statements.forEach { resolve(it) }
    }

    private fun resolve(statement: Stmt) {
        statement.accept(this)
    }

    private fun resolve(expression: Expr) {
        expression.accept(this)
    }

    override fun visitAssign(expr: Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitUnary(expr: Unary) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBinary(expr: Binary) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCall(expr: Call) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitTernary(expr: Ternary) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitGrouping(expr: Grouping) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLiteral(expr: Literal) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLogical(expr: Logical) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitVariable(expr: Variable) {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            lox.error(expr.name, "Cannot read local variable in its onw initializer.")
        }
        resolveLocal(expr, expr.name)
    }

    override fun visitExpression(stmt: Expression) {
        resolve(stmt.expression)
    }

    override fun visitFunction(stmt: Function) {
        declare(stmt.name)
        declare(stmt.name)
        resolveFunction(stmt)
    }

    override fun visitIf(stmt: If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        resolve(stmt.elseBranch!!)
    }

    override fun visitPrint(stmt: Print) {
        resolve(stmt.expression)
    }

    override fun visitReturn(stmt: Return) {
        resolve(stmt.value!!)
    }

    override fun visitVar(stmt: Var) {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }

    override fun visitWhile(stmt: While) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBlock(stmt: Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }
}

