package sirgl

import org.junit.Test
import sirgl.lir.LirGenerator
import sirgl.lir.showAsString


class LirGeneratorTest {
    fun getStream(text: String) = text.toByteArray().inputStream()
    fun getUnit(text:String) =
            Compiler(listOf(getStream(text)))
            .buildAndVerifyAst()
            .first()

    @Test
    fun `statement`() {
        val generator = LirGenerator()
        val unit = getUnit("""
        int main(int s) {
            s = 12 + 4;
            bool a = 12 <= 23 + 234 * 234 -(123 + 23);
            if(a) {
                int d = 112 / 12;
            } else {
                int s = 12323 * 12;
            }
            int i = 0;
            while(i < 10) { i = i + 1;}
            foo(12, 213);
            return 0;
        }

        extern unit foo(int a, int b)
""")
        val lirUnit = generator.generate(unit)
        println(lirUnit.showAsString())
    }
}