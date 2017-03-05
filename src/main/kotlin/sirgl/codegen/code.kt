package sirgl.codegen


data class BytecodeUnit(
        val constantPool: ConstantPool,
        val functions:List<BytecodeFunction>
)

data class BytecodeFunction(val bytecode: List<Bytecode>? = null)

enum class Bytecode(byte: Byte) {
    NoOp(0),

    //arithmetic
    Sum(1),
    Sub(2),
    Div(3),
    Mul(4),
    Neg(5),

    //boolean
    And(6),
    Or(7),
    Inv(8),

    //comparision
    Gt(9),
    Eq(10),

    //control
    Goto(11),
    If(12),

    //stack to locals and back
    Load(13),
    LoadConst(14),
    Store(15),

    //function related
    Return(16),
    Call(17),

    //load to stack
    LoadTrue(18),
    LoadFalse(19),

    //misc
    Print(20)
}