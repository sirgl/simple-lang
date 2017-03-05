package sirgl.verification

import sirgl.Symbols
import sirgl.ast.CompilationUnit

class DependencyChecker(
        val symbols: Map<CompilationUnit, Symbols>,
        val errorAnnotator: ErrorAnnotator
) {
    fun check() {
        symbols.forEach {
            val (imports, exports) = it.value
            imports.forEach {
                if (!symbolDefined(it)) {
                    errorAnnotator.addError {
                        message = "Symbol is undefined: $it"
                    }
                }
            }
        }
    }

    private fun symbolDefined(symbol: String): Boolean {
        for ((unit, value) in symbols) {
            val (imports, exports) = value
            if (exports.any { it == symbol }) {
                return true
            }
        }
        return false
    }
}
