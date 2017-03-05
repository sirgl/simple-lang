package sirgl.ast

import org.antlr.v4.runtime.ParserRuleContext
import sirgl.SlangType
import sirgl.getAllSuperclasses
import sirgl.verification.Scope
import sirgl.verification.Scoped

interface AstNode {
    var context: ParserRuleContext
    var parent: AstNode?

    fun <T, R : AstNode> findUpper(targetClass: Class<T>, limitClass: Class<R>?): T? {
        val parent = parent ?: return null
        val parentClasses: List<Class<*>> = getAllSuperclasses(parent.javaClass)
        if (limitClass != null && parentClasses.contains(limitClass)) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        if (parentClasses.contains(targetClass)) {
            return parent as T
        }
        return parent.findUpper(targetClass, limitClass)
    }

    fun <T> findUpper(targetClass: Class<T>): T? = findUpper<T, Nothing>(targetClass, null)
    fun findUpperScoped() = findUpper(Scoped::class.java)
}

data class CompilationUnit(
        var functions: List<FunctionDefinition>,
        override var context: ParserRuleContext
) : AstNode {
    override var parent: AstNode? = null
}

data class FunctionDefinition(
        var returnType: SlangType,
        var parameters: List<FormalParameter>,
        override var context: ParserRuleContext,
        var name: String,
        var block: Block? = null
) : AstNode, Scoped {
    override val scope = Scope()
    override var parent: AstNode? = null
    var external = block == null
}

data class FormalParameter(
        override var type: SlangType,
        override var context: ParserRuleContext,
        override var name: String
) : VariableSource {
    override var  addr: Int? = null
    override var parent: AstNode? = null
}