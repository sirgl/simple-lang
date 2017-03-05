package sirgl.ast

import org.antlr.v4.runtime.ParserRuleContext
import sirgl.*


interface Expression : Statement {
    var inferredType: SlangType
}

data class True(override var context: ParserRuleContext) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = BoolType
}

data class False(override var context: ParserRuleContext) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = BoolType
}

data class IntLiteral(
        override var context: ParserRuleContext,
        var number: Int
) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = IntType
}

data class Variable(override var context: ParserRuleContext, val name: String) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = UnknownType
}

data class FunctionCall(
        override var context: ParserRuleContext,
        var arguments: List<Expression>,
        var name: String
) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = UnknownType
}

interface BinaryExpression : Expression {
    var left: Expression
    var right: Expression
}

data class MultiplyExpr(
        override var context: ParserRuleContext,
        override var left: Expression,
        override var right: Expression,
        var opType : OpType
) : BinaryExpression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = IntType

    enum class OpType {
        Mul,
        Div
    }
}

data class SumExpr(
        override var context: ParserRuleContext,
        override var left: Expression,
        override var right: Expression,
        var opType : OpType
) : BinaryExpression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = IntType

    enum class OpType {
        Sum,
        Sub
    }
}

data class ComparisionExpr(
        override var context: ParserRuleContext,
        override var left: Expression,
        override var right: Expression,
        var opType : OpType
) : BinaryExpression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = BoolType

    enum class OpType {
        Gt,
        Lt,
        LtEq,
        GtEq,
        Eq,
    }
}

data class BooleanExpr(
        override var context: ParserRuleContext,
        override var left: Expression,
        override var right: Expression,
        var opType : OpType
) : BinaryExpression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = BoolType

    enum class OpType {
        And,
        Or
    }
}

data class NegExpr(
        override var context: ParserRuleContext,
        var expr : Expression
) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = BoolType
}

data class MinusExpr(
        override var context: ParserRuleContext,
        var expr : Expression
) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = IntType
}

data class PlusExpr(
        override var context: ParserRuleContext,
        var expr : Expression
) : Expression {
    override var parent: AstNode? = null
    override var inferredType: SlangType = IntType
}
