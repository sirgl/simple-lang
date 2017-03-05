package sirgl

import sirgl.ast.CompilationUnit
import sirgl.ast.FunctionDefinition

class ResolveEngine(
        units: List<CompilationUnit>
) {
    private val nameMap = mutableMapOf<String, FunctionDefinition>()

    init {
        units
                .asSequence()
                .flatMap { it -> it.functions.asSequence() }
                .forEach { it -> nameMap[it.name] = it }
    }

    fun findFunction(name: String) = nameMap[name]
}