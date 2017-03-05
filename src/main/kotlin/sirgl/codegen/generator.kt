package sirgl.codegen

import sirgl.lir.*


class BytecodeGenerator() {
    // Very bad design, I know, but want to finish
    fun generate(lirUnit: LirUnit) {
        val pool = lirUnit.pool
        val constantAddrTranslationTable = mutableMapOf<Int, Int>()
        val constantPool = ConstantPool()
        loop@ for ((addr, symbol) in pool.table) {
            when (symbol) {
                is BoolConstantSymbol -> constantAddrTranslationTable[addr] = constantPool.add(symbol)
                is StringConstantSymbol -> constantAddrTranslationTable[addr] = constantPool.add(symbol)
                is IntConstantSymbol -> constantAddrTranslationTable[addr] = constantPool.add(symbol)
                is VariableSymbol -> continue@loop
            }
        }


        val functions = mutableListOf<BytecodeFunction>()
        for ((name, code, extern) in lirUnit.functions) {
            if(extern) {
                functions.add(BytecodeFunction())
                continue
            }
            val variableAddrTranslationTable = mutableMapOf<Int, Int>()
            
        }

    }

    private fun generate(functionCode : FunctionCode, varPool : VarPool) {
        functionCode.code.forEach {
            
        }
    }
}