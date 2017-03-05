package sirgl

import sirgl.ast.*

fun SlangParser.CompilationUnitContext.toAst() =
        CompilationUnit(functionDef().map { it.toAst() }, this)
                .apply { functions.forEach { it.parent = this } }

fun SlangParser.FunctionDefContext.toAst() =
        FunctionDefinition(type().toAst(), formalParameters()?.toAst() ?: emptyList(), this, this.Id().text, block()?.toAst())
                .apply {
                    parameters.forEach { it.parent = this }
                    block?.parent = this
                }

fun SlangParser.TypeContext.toAst() = when (text) {
    "int" -> IntType
    "bool" -> BoolType
    "unit" -> UnitType
    else -> throw UnsupportedOperationException("$this is not supported")
}

fun SlangParser.FormalParameterContext.toAst() = FormalParameter(type().toAst(), this, Id().text)

fun SlangParser.FormalParametersContext.toAst() = formalParameter().map { it.toAst() }

fun SlangParser.BlockContext.toAst() = Block(this, statement().map { it.toAst() })
        .apply { statements.forEach { it.parent = this } }

fun SlangParser.StatementContext.toAst(): Statement {
    return when {
        assignStmt() != null -> assignStmt().toAst()
        expr() != null -> expr().toAst()
        ifStmt() != null -> ifStmt().toAst()
        whileStmt() != null -> whileStmt().toAst()
        varDefStmt() != null -> varDefStmt().varDef().toAst()
        returnStmt() != null -> returnStmt().toAst()
        else -> throw UnsupportedOperationException("${this.text} is not supported")
    }
}

private fun SlangParser.ReturnStmtContext.toAst() =
        ReturnStatement(expr()?.toAst(), this)
        .apply { expr?.parent = this }

fun SlangParser.VarDefContext.toAst() =
        VarDefinitionStatement(this, Id().text, type().toAst(), expr().toAst())
                .apply { expr.parent = this }

fun SlangParser.IfStmtContext.toAst(): IfStatement {
    val elseBlock = if (block().size == 2) block(1).toAst() else null
    return IfStatement(this, condition().expr().toAst(), block(0).toAst(), elseBlock)
            .apply {
                block.parent = this
                condition.parent = this
                elseBlock?.parent = this
            }
}

fun SlangParser.WhileStmtContext.toAst() =
        WhileStatement(this, condition().expr().toAst(), block().toAst())
                .apply {
                    condition.parent = this
                    block.parent = this
                }

fun SlangParser.AssignStmtContext.toAst() = AssignmentStatement(this, Id().text, expr().toAst())
        .apply { expr.parent = this }

fun SlangParser.ExprContext.toAst(): Expression = when (this) {
    is SlangParser.PrimaryExprContext -> primary().toExprAst()
    is SlangParser.CallExprContext -> functionCall().toExprAst()
    is SlangParser.MultiplyExprContext -> toExprAst()
    is SlangParser.SumExprContext -> toExprAst()
    is SlangParser.ComarsionExprContext -> toExprAst()
    is SlangParser.BoolExprContext -> toExprAst()
    is SlangParser.NegExprContext -> toExprAst()
    is SlangParser.UnaryMinusContext -> toExprAst()
    is SlangParser.UnaryPlusContext -> toExprAst()
    else -> throw UnsupportedOperationException("$this is not supported")
}

fun SlangParser.MultiplyExprContext.toExprAst(): MultiplyExpr {
    val opType = when (operator.text) {
        "*" -> MultiplyExpr.OpType.Mul
        "/" -> MultiplyExpr.OpType.Div
        else -> throw UnsupportedOperationException("$this is not supported")
    }
    return MultiplyExpr(this, expr(0).toAst(), expr(1).toAst(), opType)
            .apply {
                left.parent = this
                right.parent = this
            }
}

fun SlangParser.SumExprContext.toExprAst(): SumExpr {
    val opType = when (operator.text) {
        "+" -> SumExpr.OpType.Sum
        "-" -> SumExpr.OpType.Sub
        else -> throw UnsupportedOperationException("$this is not supported")
    }
    return SumExpr(this, expr(0).toAst(), expr(1).toAst(), opType)
            .apply {
                left.parent = this
                right.parent = this
            }
}

fun SlangParser.ComarsionExprContext.toExprAst(): ComparisionExpr {
    val opType = when (operator.text) {
        "==" -> ComparisionExpr.OpType.Eq
        ">" -> ComparisionExpr.OpType.Gt
        ">=" -> ComparisionExpr.OpType.GtEq
        "<" -> ComparisionExpr.OpType.Lt
        "<=" -> ComparisionExpr.OpType.LtEq
        else -> throw UnsupportedOperationException("$this is not supported")
    }
    return ComparisionExpr(this, expr(0).toAst(), expr(1).toAst(), opType)
            .apply {
                left.parent = this
                right.parent = this
            }
}

fun SlangParser.BoolExprContext.toExprAst(): BooleanExpr {
    val opType = when (operator.text) {
        "&&" -> BooleanExpr.OpType.And
        "||" -> BooleanExpr.OpType.Or
        else -> throw UnsupportedOperationException("$this is not supported")
    }
    return BooleanExpr(this, expr(0).toAst(), expr(1).toAst(), opType)
            .apply {
                left.parent = this
                right.parent = this
            }
}

fun SlangParser.UnaryMinusContext.toExprAst() = MinusExpr(this, expr().toAst()).apply { expr.parent = this }
fun SlangParser.UnaryPlusContext.toExprAst() = PlusExpr(this, expr().toAst()).apply { expr.parent = this }
fun SlangParser.NegExprContext.toExprAst() = NegExpr(this, expr().toAst()).apply { expr.parent = this }

fun SlangParser.FunctionCallContext.toExprAst() =
        FunctionCall(this, expressionList()?.expr()?.map { it.toAst() } ?: emptyList(), Id().text)
                .apply { arguments.forEach { it.parent = this } }

fun SlangParser.PrimaryContext.toExprAst(): Expression = when {
    False() != null -> False(this)
    True() != null -> True(this)
    Id() != null -> Variable(this, Id().text)
    expr() != null -> expr().toAst()
    IntLiteral() != null -> IntLiteral(this, IntLiteral().text.toInt())
    else -> throw UnsupportedOperationException("$this is not supported")
}


















