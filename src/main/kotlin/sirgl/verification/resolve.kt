package sirgl.verification

import sirgl.ast.CompilationUnit

class ResolveCheck(val units: List<CompilationUnit>, val errorAnnotator: ErrorAnnotator) {
    fun check() {
        val distinctNames = mutableSetOf<String>()

        units
                .flatMap { it.functions }
                .filter { distinctNames.add(it.name) }
                .forEach {
                    errorAnnotator.addError {
                        message = "Function duplicates in different units"
                    }
                }
    }
}