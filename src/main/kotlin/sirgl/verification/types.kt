package sirgl.verification

import sirgl.*
import sirgl.ast.*

class TypeChecker(val errorAnnotator: ErrorAnnotator, val resolveEngine: ResolveEngine) {
    fun check(node: AstNode) {
        val walker = AstWalker()
        walker.addSimpleListenerFor<Variable> { inferVariable(it) }
        walker.addSimpleListenerFor<IfStatement> { it.condition.assertHasBoolType() }
        walker.addSimpleListenerFor<WhileStatement> { it.condition.assertHasBoolType() }
        walker.addSimpleListenerFor<SumExpr> { it.assertHasType(IntType) }
        walker.addSimpleListenerFor<MultiplyExpr> { it.assertHasType(IntType) }
        walker.addSimpleListenerFor<BooleanExpr> { it.assertHasType(BoolType) }
        walker.addSimpleListenerFor<ComparisionExpr> { it.assertHasType(BoolType) }
        walker.addSimpleListenerFor<NegExpr> { it.expr.assertHasBoolType() }
        walker.addSimpleListenerFor<PlusExpr> { it.expr.assertHasType(IntType) }
        walker.addSimpleListenerFor<MinusExpr> { it.expr.assertHasType(IntType) }
        walker.addSimpleListenerFor<FunctionCall> { checkFunction(it) }
        walker.walk(node)
    }

    private fun checkFunction(call: FunctionCall) {
        val function = resolveEngine.findFunction(call.name) ?: return
        for ((index, argument) in call.arguments.withIndex()) {
            argument.assertHasType(function.parameters[index].type)
        }
        call.inferredType = function.returnType
    }

    private fun inferVariable(it: Variable) {
        val scope = it.findUpperScoped()?.scope
        val source = scope?.findSource(it.name)
        it.inferredType = source?.type ?: UnknownType
    }

    private fun Expression.assertHasBoolType() = assertHasType(BoolType)

    private fun Expression.assertHasType(type: SlangType) {
        if (inferredType != type && inferredType != UnknownType && type != UnknownType) {
            errorAnnotator.addError {
                message = "Unexpected expression type, $type expected"
            }
        }
    }

    private fun BinaryExpression.assertHasType(type: SlangType) {
        left.assertHasType(type)
        right.assertHasType(type)
    }
}