package sirgl.lir


data class Code(
        var operation: OpCode,
        var result: Int = -1,
        var argument1: Int? = null,
        var argument2: Int? = null
) {
    override fun toString(): String {
        return "%8s %8d %8d %10d".format(operation, result, argument1, argument2)
    }
}

data class FunctionCode(
        val name: String,
        val code: List<Code>,
        val extern: Boolean
)