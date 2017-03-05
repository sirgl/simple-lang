package sirgl.verification

import sirgl.AstWalker
import sirgl.ast.*
import java.util.*


class Scope(var parentScope: Scope? = null) {
    val definitons = mutableListOf<VariableSource>()

    fun tryAddSource(def: VariableSource): Boolean {
        if (definitons.any { it.name == def.name }) {
            return false
        } else {
            definitons.add(def)
            return true
        }
    }

    fun hasDefinition(variable: Variable): Boolean {
        return definitons.any { it.name == variable.name }
                || parentScope?.hasDefinition(variable) ?: false
    }

    fun findSource(name: String): VariableSource? {
        return definitons.find({ it.name == name }) ?: parentScope?.findSource(name)
    }
}

class ScopeSession(
        val scope: Scope,
        private val annotator: ErrorAnnotator
) {
    private val conflictingMap = mutableMapOf<String, MutableList<VariableSource>>()

    fun add(variable: Variable) {
        if (!scope.hasDefinition(variable)) {
            annotator.addError {
                message = "Usage of undeclared variable"
                addHighlight(variable)
            }
        }
    }

    fun add(variableSource: VariableSource) {
        if (!scope.tryAddSource(variableSource)) {
            val conflictingDeclarations = conflictingMap.getOrPut(variableSource.name, { mutableListOf() })
            conflictingDeclarations.add(variableSource)
        }
    }

    fun commitErrors() {
        conflictingMap.entries.forEach {
            val name = it.key
            val conflicts = it.value
            conflicts.add(scope.findSource(name)!!)
            annotator.addError {
                message = "Redefinition"
                conflicts.forEach { addHighlight(it) }
            }
        }
    }
}

class ScopeAndDependencyChecker(val errorAnnotator: ErrorAnnotator) {
    val importFunctionNames = mutableSetOf<String>()
    val exportFunctionNames = mutableListOf<String>()


    fun check(compilationUnit: CompilationUnit) {
        val walker = AstWalker()
        val sessionStack = Stack<ScopeSession>()
        walker.addListenerFor<Block> {
            connectToParentScope(it)
            sessionStack.push(ScopeSession(it.scope, errorAnnotator))
            return@addListenerFor { // on block exit removing current session
                val session = sessionStack.pop()
                session.commitErrors()
            }
        }
        walker.addSimpleListenerFor<FunctionDefinition> {
            val session = ScopeSession(it.scope, errorAnnotator)
            it.parameters.forEach { parameter -> session.add(parameter) }
            if(!exportFunctionNames.add(it.name)) {
                errorAnnotator.addError {
                    message = "Function redefinition"
                    addHighlight(it)
                }
            }
        }
        walker.addSimpleListenerFor<Variable> {
            val session = sessionStack.peek()
            session.add(it)
        }
        walker.addSimpleListenerFor<VarDefinitionStatement> {
            val session = sessionStack.peek()
            session.add(it)
        }
        walker.addSimpleListenerFor<FunctionCall> {
            importFunctionNames.add(it.name)
        }
        walker.walk(compilationUnit)
    }

    private fun connectToParentScope(it: Block) {
        val upperScoped = it.findUpperScoped()
        if (upperScoped != null) {
            it.scope.parentScope = upperScoped.scope
        }
    }

}

interface Scoped {
    val scope: Scope
}