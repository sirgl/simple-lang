package sirgl.codegen

import sirgl.lir.BoolConstantSymbol
import sirgl.lir.IntConstantSymbol
import sirgl.lir.StringConstantSymbol

class ConstantPool {
    val storage = mutableListOf<Any>()

    fun add(symbol: BoolConstantSymbol) : Int {
        storage.add(symbol.value)
        return storage.size
    }

    fun add(symbol: StringConstantSymbol) : Int {
        storage.add(symbol.value)
        return storage.size
    }

    fun add(symbol: IntConstantSymbol) : Int {
        storage.add(symbol.value)
        return storage.size
    }
}