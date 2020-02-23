package lox

interface LoxCallable {
    fun call(interpreter: Interpreter, args: List<Any?>): Any?
    fun arity(): Int
}

class LoxFunction(private val declaration: Function, private val closure: Environment) : LoxCallable {
    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val environment = Environment(closure)
        for ((idx, param) in declaration.parameters.withIndex()) {
            environment.define(param.lexeme, args[idx])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: ReturnAction) {
            return returnValue.value
        }
        return null
    }

    override fun arity(): Int {
        return declaration.parameters.size
    }

}

fun nativeFn(call: (interpreter: Interpreter, args: List<Any?>) -> Any?, arity: Int = 0): LoxCallable {
    return object : LoxCallable {
        override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
            return call(interpreter, args)
        }

        override fun arity(): Int {
            return arity
        }

        override fun toString(): String {
            return "<native fn>"
        }

    }
}

class ReturnAction(val value: Any?) : RuntimeException(null, null, false, false)