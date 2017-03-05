package sirgl.verification

import sirgl.ast.AstNode


class ErrorAnnotator {
    private val errors = mutableListOf<Error>()

    fun addError(builder: Error.() -> Unit) {
        val error = Error()
        builder(error)
        errors.add(error)
    }

    fun getErrors(): List<Error> = errors
}

data class Error(
        var level: ErrorLevel = ErrorLevel.Error,
        var highlights: MutableList<AstNode> = mutableListOf(),
        var message: String = ""
        ) {
    fun addHighlight(highlight: AstNode) {

    }
}

enum class ErrorLevel {
    Error,
    Warning,
    Notice
}