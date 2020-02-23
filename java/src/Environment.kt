package lox

class Environment(val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        } else if (enclosing != null) {
            return enclosing.get(name)
        } else {
            throw RuntimeError(name, "Undefined variable'" + name.lexeme + "'.")
        }
    }


    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else if (enclosing != null) {
            enclosing.assign(name, value)
        } else {
            throw RuntimeError(name, "Undefined variable'" + name.lexeme + "'.")
        }
    }
}