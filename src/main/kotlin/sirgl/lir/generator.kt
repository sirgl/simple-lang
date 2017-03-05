package sirgl.lir

import sirgl.ast.*


val TO_REPLACE = -1

class LirGenerator(val pool: VarPool = VarPool()) {

    fun generate(compilationUnit : CompilationUnit): LirUnit {
        compilationUnit.functions.forEach {
            pool.add(it)
            it.parameters.forEach {
                it.addr = pool.add(it)
            }
        }
        val functions = compilationUnit.functions.map {
            val code = mutableListOf<Code>()
            it.block?.statements?.forEach { it.appendCodeTo(code) }
            FunctionCode(it.name, code, it.external)
        }
        return LirUnit(functions, pool)
    }

    fun Statement.generateCode() = mutableListOf<Code>().apply { appendCodeTo(this) }

    private fun Statement.appendCodeTo(code: MutableList<Code>) {
        when (this) {
            is VarDefinitionStatement -> appendCodeTo(code)
            is Expression -> appendCodeTo(code)
            is IfStatement -> appendCodeTo(code)
            is WhileStatement -> appendCodeTo(code)
            is AssignmentStatement -> appendCodeTo(code)
            is ReturnStatement -> appendCodeTo(code)
        }
    }

    private fun ReturnStatement.appendCodeTo(code: MutableList<Code>) {
        val exprAddr = expr?.appendCodeAndGetAddr(code)
        code.add(Code(OpCode.Return, argument1 = exprAddr))
    }

    private fun AssignmentStatement.appendCodeTo(code: MutableList<Code>) {
        val source = findUpperScoped()?.scope?.findSource(name)!!
        code.add(Code(OpCode.Copy, source.addr!!, expr.appendCodeAndGetAddr(code)))
    }


    private fun WhileStatement.appendCodeTo(code: MutableList<Code>) {
        val entrance = code.size
        val conditionAddr = condition.appendCodeAndGetAddr(code)
        val invertedCondition = pool.addTmpVar()
        code.add(Code(OpCode.Inv, invertedCondition, conditionAddr))

        val jumpOut = Code(OpCode.If, TO_REPLACE, conditionAddr)
        code.add(jumpOut)
        block.statements.forEach { it.appendCodeTo(code) }
        code.add(Code(OpCode.Goto, entrance))
        jumpOut.result = code.size
    }

    private fun IfStatement.appendCodeTo(code: MutableList<Code>) {
        val conditionAddr = condition.appendCodeAndGetAddr(code)
        val conditionJump = Code(OpCode.If, TO_REPLACE, conditionAddr)
        code.add(conditionJump)
        block.statements.forEach { it.appendCodeTo(code) }
        val elseBlockImm = elseBlock
        if(elseBlockImm != null) {
            val skipElseCode = Code(OpCode.Goto, TO_REPLACE)
            code.add(skipElseCode)
            conditionJump.result = code.size
            elseBlockImm.statements.forEach { it.appendCodeTo(code) }
            skipElseCode.result = code.size
        } else {
            conditionJump.result = code.size
        }
    }

    private fun Expression.appendCodeTo(code: MutableList<Code>) {
        when (this) {
            is IntLiteral -> code.add(Code(OpCode.Copy, pool.addTmpVar(), pool.add(this)))
            is True -> code.add(Code(OpCode.Copy, pool.addTmpVar(), pool.add(this)))
            is False -> code.add(Code(OpCode.Copy, pool.addTmpVar(), pool.add(this)))
            is Variable -> {
                val addr = findUpperScoped()?.scope?.findSource(this.name)?.addr!!
                code.add(Code(OpCode.Copy, pool.addTmpVar(), addr))
            }
            is NegExpr -> code.add(Code(OpCode.Inv, pool.addTmpVar(), expr.appendCodeAndGetAddr(code)))
            is MinusExpr -> code.add(Code(OpCode.Neg, pool.addTmpVar(), expr.appendCodeAndGetAddr(code)))
            is PlusExpr -> return
            is BinaryExpression -> appendCodeTo(code)
            is FunctionCall -> appendCodeTo(code)
        }
    }

    private fun FunctionCall.appendCodeTo(code: MutableList<Code>) {
        val functionId = pool.findFunctionId(name)
        arguments.map { it.appendCodeAndGetAddr(code) }
                .forEach { code.add(Code(OpCode.Param, argument1 = it)) }
        code.add(Code(OpCode.Call, pool.addTmpVar(), functionId))
    }

    private fun BinaryExpression.appendCodeTo(code: MutableList<Code>) {
        when (this) {
            is MultiplyExpr -> appendCodeTo(code)
            is SumExpr -> appendCodeTo(code)
            is ComparisionExpr -> appendCodeTo(code)
            is BooleanExpr -> appendCodeTo(code)
        }
    }

    private fun BooleanExpr.appendCodeTo(code: MutableList<Code>) {
        when (opType) {
            BooleanExpr.OpType.And -> code.add(Code(OpCode.And, pool.addTmpVar(), left.appendCodeAndGetAddr(code), right.appendCodeAndGetAddr(code)))
            BooleanExpr.OpType.Or -> code.add(Code(OpCode.Or, pool.addTmpVar(), left.appendCodeAndGetAddr(code), right.appendCodeAndGetAddr(code)))
        }
    }

    private fun MultiplyExpr.appendCodeTo(code: MutableList<Code>) {
        when (opType) {
            MultiplyExpr.OpType.Div -> code.add(Code(OpCode.Div, pool.addTmpVar(), left.appendCodeAndGetAddr(code), right.appendCodeAndGetAddr(code)))
            MultiplyExpr.OpType.Mul -> code.add(Code(OpCode.Mul, pool.addTmpVar(), left.appendCodeAndGetAddr(code), right.appendCodeAndGetAddr(code)))
        }
    }

    private fun SumExpr.appendCodeTo(code: MutableList<Code>) {
        when (opType) {
            SumExpr.OpType.Sum -> code.add(Code(OpCode.Sum, pool.addTmpVar(), left.appendCodeAndGetAddr(code), right.appendCodeAndGetAddr(code)))
            SumExpr.OpType.Sub -> code.add(Code(OpCode.Sub, pool.addTmpVar(), left.appendCodeAndGetAddr(code), right.appendCodeAndGetAddr(code)))
        }
    }

    private fun ComparisionExpr.appendCodeTo(code: MutableList<Code>) {
        val leftAddr = left.appendCodeAndGetAddr(code)
        val rightAddr = right.appendCodeAndGetAddr(code)
        when (opType) {
            ComparisionExpr.OpType.Eq -> code.add(Code(OpCode.Eq, pool.addTmpVar(), leftAddr, rightAddr))
            ComparisionExpr.OpType.Gt -> code.add(Code(OpCode.Gt, pool.addTmpVar(), leftAddr, rightAddr))
            ComparisionExpr.OpType.Lt -> {
                code.add(Code(OpCode.Gt, pool.addTmpVar(), leftAddr, rightAddr))
                code.add(Code(OpCode.Inv, pool.addTmpVar(), code.last().result))
            }
            ComparisionExpr.OpType.LtEq -> {
                code.add(Code(OpCode.Gt, pool.addTmpVar(), leftAddr, rightAddr))
                code.add(Code(OpCode.Inv, pool.addTmpVar(), code.last().result))
                val lt = code.last().result
                code.add(Code(OpCode.Eq, pool.addTmpVar(), leftAddr, rightAddr))
                val eq = code.last().result
                code.add(Code(OpCode.Or, pool.addTmpVar(), lt, eq))
            }
            ComparisionExpr.OpType.GtEq -> {
                code.add(Code(OpCode.Gt, pool.addTmpVar(), leftAddr, rightAddr))
                val gt = code.last().result
                code.add(Code(OpCode.Eq, pool.addTmpVar(), leftAddr, rightAddr))
                val eq = code.last().result
                code.add(Code(OpCode.Or, pool.addTmpVar(), gt, eq))
            }
        }
    }

    private fun VarDefinitionStatement.appendCodeTo(code: MutableList<Code>) {
        val varAddr = pool.add(this)
        val exprAddr = expr.appendCodeAndGetAddr(code)
        code.add(Code(OpCode.Copy, varAddr, exprAddr))
        addr = varAddr
    }

    private fun Expression.appendCodeAndGetAddr(code: MutableList<Code>) : Int {
        appendCodeTo(code)
        return code.last().result
    }



}

class LimitsReachedException : RuntimeException()

class VarPool {
    val table = mutableMapOf<Int, Symbol>()
    val functionNames = mutableMapOf<String, Int>()
    val constantLimit = 65536
    val variableLimit = 131072
    var constantCounter = 0
    var variableCounter = 65536

    fun add(variableSource : VariableSource)  = addVar()

    fun addTmpVar() = addVar()

    private fun addVar(): Int {
        variableCounter++
        if (variableCounter == variableLimit) {
            throw LimitsReachedException()
        }
        table[variableCounter] = VariableSymbol()
        return variableCounter
    }

    private fun checkConstantLimit() {
        constantCounter++
        if (constantCounter == constantLimit) {
            throw LimitsReachedException()
        }
    }

    fun add(intLiteral : IntLiteral) : Int {
        checkConstantLimit()
        table[constantCounter] = IntConstantSymbol(intLiteral.number)
        return constantCounter
    }

    fun add(trueLiteral: True) : Int {
        checkConstantLimit()
        table[constantCounter] = BoolConstantSymbol(true)
        return constantCounter
    }

    fun add(falseLiteral : False)  : Int {
        checkConstantLimit()
        table[constantCounter] = BoolConstantSymbol(true)
        return constantCounter
    }

    fun add(functionDefinition : FunctionDefinition)  : Int {
        checkConstantLimit()
        table[constantCounter] = StringConstantSymbol(functionDefinition.name)
        functionNames[functionDefinition.name] = constantCounter
        return constantCounter
    }

    fun findFunctionId(name: String)  = functionNames[name]!!
}

interface Symbol

class VariableSymbol : Symbol

data class BoolConstantSymbol(val value: Boolean) : Symbol
data class IntConstantSymbol(val value: Int) : Symbol
data class StringConstantSymbol(val value: String) : Symbol

data class LirUnit(
        val functions: List<FunctionCode>,
        val pool: VarPool
)