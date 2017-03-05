package sirgl


fun main(args: Array<String>) {
    val lib = """
extern unit printInt(int value)
extern unit printBool(bool value)
"""

    val text = """
int main(){
    int a = printA(test());
}

int test() {
    return 12 + 34;
}

"""
    val userStream = text.toByteArray().inputStream()
    val libStream = lib.toByteArray().inputStream()
    val compile = Compiler(listOf(userStream, libStream)).compile()

}