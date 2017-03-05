package sirgl


import SlangParser
import SlangLexer
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.BufferedTokenStream
import sirgl.ast.CompilationUnit
import sirgl.lir.LirGenerator
import sirgl.verification.*
import java.io.InputStream

class Compiler(val streams: List<InputStream>) {
    fun compile(): List<Error> {
        val units = parse()
        val errors = verify(units)
        if(errors.isNotEmpty()){
            return errors
        }
        val lirUnits = units.map { LirGenerator().generate(it) }


        return errors
    }

    fun buildAndVerifyAst(): List<CompilationUnit> {
        val units = parse()
        val errors = verify(units)
        return units
    }

    private fun parse() = streams
            .asSequence()
            .map { SlangLexer(ANTLRInputStream(it)) }
            .map { SlangParser(BufferedTokenStream(it)) }
            .map { it.compilationUnit().toAst() }
            .toList()

    private fun verify(units: List<CompilationUnit>) : List<Error> {
        val annotator = ErrorAnnotator()

        val resolveEngine = ResolveEngine(units)
        val typeChecker = TypeChecker(annotator, resolveEngine)
        val symbols = units.associateBy({it}) {
            val checker = ScopeAndDependencyChecker(annotator)
            checker.check(it)
            Symbols(checker.importFunctionNames.toList(), checker.exportFunctionNames)
        }
        DependencyChecker(symbols, annotator).check()
        units.forEach { typeChecker.check(it) }
        val errors = annotator.getErrors()
        return errors
    }
}

data class Symbols (
        val imports: List<String>,
        val exports: List<String>
)