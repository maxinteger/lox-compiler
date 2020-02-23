package lox


interface ExprVisitor<R> {
    fun visitAssign(expr: Assign): R
    fun visitUnary(expr: Unary): R
    fun visitBinary(expr: Binary): R
    fun visitCall(expr: Call): R
    fun visitTernary(expr: Ternary): R
    fun visitGrouping(expr: Grouping): R
    fun visitLiteral(expr: Literal): R
    fun visitLogical(expr: Logical): R
    fun visitVariable(expr: Variable): R
}

sealed class Expr {
    abstract fun <R> accept(v: ExprVisitor<R>): R
}

class Assign(val name: Token, val value: Expr) : Expr(){
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitAssign(this)
}

class Ternary(val logic: Expr, val left: Expr, val right: Expr) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitTernary(this)
}

class Binary(val left: Expr, val op: Token, val right: Expr) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitBinary(this)
}

class Call(val callee: Expr, val param: Token, val args: List<Expr>) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitCall(this)
}

class Grouping(val expresion: Expr) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitGrouping(this)
}

class Literal(val value: Any?) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitLiteral(this)
}

class Logical(val left: Expr, val op: Token, val right: Expr) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitLogical(this)
}

class Unary(val op: Token, val right: Expr) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitUnary(this)
}

class Variable(val name: Token) : Expr() {
    override fun <R> accept(v: ExprVisitor<R>): R = v.visitVariable(this)
}

