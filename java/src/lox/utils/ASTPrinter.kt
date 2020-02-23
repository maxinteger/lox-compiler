package lox.utils

import lox.*
//
//class ASTPrinter : ExprVisitor<String>, StmtVisitor<String> {
//    override fun visitExpression(stmt: Expression): String {
//        return print(stmt.expression)
//    }
//
//    override fun visitIf(stmt: If): String {
//        return "(if ${print(stmt.condition)} ${printStmt(stmt.thenBranch)} ${printStmt(stmt.elseBranch!!)}"
//    }
//
//    override fun visitPrint(stmt: Print): String {
//        return parenthesize("print", stmt.expression)
//    }
//
//    override fun visitVar(stmt: Var): String {
//        return "(var ${stmt.name.lexeme} = ${print(stmt.initializer)})"
//    }
//
//    override fun visitWhile(stmt: While): String {
//        return "(while ${print(stmt.condition)} ${printStmt(stmt.body)}"
//    }
//
//    override fun visitBlock(stmt: Block): String {
//        return printStatements(stmt.statements)
//    }
//
//    override fun visitLogical(expr: Logical): String {
//        return parenthesize(expr.op.lexeme, expr.left, expr.right)
//    }
//
//    override fun visitTernary(expr: Ternary): String {
//        return parenthesize("ternary", expr.logic, expr.left, expr.right)
//    }
//
//    override fun visitAssign(expr: Assign): String {
//        return parenthesize("assing", expr)
//    }
//
//    override fun visitVariable(expr: Variable): String {
//        return parenthesize("var", expr)
//    }
//
//    override fun visitBinary(expr: Binary): String {
//        return parenthesize(expr.op.lexeme, expr.left, expr.right)
//    }
//
//    override fun visitGrouping(expr: Grouping): String {
//        return parenthesize("group", expr.expresion)
//    }
//
//    override fun visitLiteral(expr: Literal): String {
//        return expr.value?.toString() ?: "nil"
//    }
//
//    override fun visitUnary(expr: Unary): String {
//        return parenthesize(expr.op.lexeme, expr.right)
//    }
//
//    fun printStmt(stmt: Stmt) {
//        stmt.accept(this)
//    }
//
//    fun printStatements(stmt: List<Stmt>):String {
//        val builder = StringBuilder()
//        builder.append("(")
//        builder.append(stmt.map { printStmt(it) }.joinToString("\n"))
//        builder.append(")")
//
//        return builder.toString()
//    }
//
//    fun print(expr: Expr): String {
//        return expr.accept(this)
//    }
//
//    private fun parenthesize(name: String, vararg expressions: Expr): String {
//        val builder = StringBuilder()
//        builder.append("(")
//        builder.append(name)
//
//        for (e in expressions) {
//            builder.append(" ")
//            builder.append(e.accept(this))
//        }
//        builder.append(")")
//
//        return builder.toString()
//    }
//}
