package sirgl.lir

fun LirUnit.showAsString(): String {
    return buildString {
        append("=== Constant pool ===\n")
        append(pool.showAsString())
        append("=== Functions ===\n")
        for ((name, code, extern) in functions) {
            append(">>> $name\n")
            if(extern) {
                append("   EXTERNAL\n")
            } else {
                code.forEachIndexed { i, code ->  append("${"%3d".format(i)}$code\n") }
            }
        }
    }
}

fun VarPool.showAsString(): String {
    val builder = StringBuilder()
    loop@ for ((id, symbol) in table) {
        var symbolId = "?"
        var value = ""
        when(symbol) {
            is BoolConstantSymbol -> {
                symbolId = "B"
                value = symbol.value.toString()
            }
            is IntConstantSymbol -> {
                symbolId = "I"
                value = symbol.value.toString()
            }
            is StringConstantSymbol -> {
                symbolId = "S"
                value = symbol.value
            }
            is VariableSymbol -> continue@loop
        }
        builder.append("$symbolId    $value\n")
    }
    return builder.toString()
}

