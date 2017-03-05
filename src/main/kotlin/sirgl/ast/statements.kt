package sirgl.ast

import org.antlr.v4.runtime.ParserRuleContext
import sirgl.SlangType
import sirgl.verification.Scope
import sirgl.verification.Scoped

interface Statement : AstNode

data class ReturnStatement(
        var expr: Expression?, override var context: ParserRuleContext
) : Statement {
    override var parent: AstNode? = null
}

data class IfStatement(
        override var context: ParserRuleContext,
        var condition: Expression,
        var block : Block,
        var elseBlock : Block?
) : Statement {
    override var parent: AstNode? = null
}

data class WhileStatement(
        override var context: ParserRuleContext,
        var condition: Expression,
        var block : Block
) : Statement {
    override var parent: AstNode? = null
}

data class Block(override var context: ParserRuleContext, var statements: List<Statement>
) : AstNode, Scoped {
    override val scope = Scope()
    override var parent: AstNode? = null
}

data class AssignmentStatement(
        override var context: ParserRuleContext,
        var name: String,
        var expr: Expression
) : Statement {
    override var parent: AstNode? = null
}

data class VarDefinitionStatement(
        override var context: ParserRuleContext,
        override var name: String,
        override var type: SlangType,
        var expr: Expression
) : Statement, VariableSource {
    override var addr: Int? = null
    override var parent: AstNode? = null
}

interface VariableSource : AstNode{
    val name: String
    val type: SlangType
    var addr: Int?
}