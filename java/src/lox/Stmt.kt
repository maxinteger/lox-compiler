package lox

interface StmtVisitor<R> {
    fun visitExpression(stmt: Expression): R
    fun visitFunction(stmt: Function): R
    fun visitIf(stmt: If): R
    fun visitPrint(stmt: Print): R
    fun visitReturn(stmt: Return): R
    fun visitVar(stmt: Var): R
    fun visitWhile(stmt: While): R
    fun visitBlock(stmt: Block): R
}

sealed class Stmt {
    abstract fun <R> accept(v: StmtVisitor<R>): R
}

class Expression(val expression: Expr) : Stmt() {
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitExpression(this)
}

class Function( val name: Token, val parameters: List<Token>, val body: List<Stmt>): Stmt(){
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitFunction(this)
}

class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Stmt?) : Stmt() {
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitIf(this)
}

class Print(val expression: Expr) : Stmt() {
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitPrint(this)
}

class Return(val keyword: Token, val value: Expr?) : Stmt() {
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitReturn(this)
}

class Var(val name: Token, val initializer: Expr?) : Stmt() {
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitVar(this)
}

class While(val condition: Expr, val body: Stmt) : Stmt() {
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitWhile(this)
}

class Block(val statements: List<Stmt>) : Stmt() {
    override fun <R> accept(v: StmtVisitor<R>): R = v.visitBlock(this)
}


