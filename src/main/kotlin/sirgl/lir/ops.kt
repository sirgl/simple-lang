package sirgl.lir

enum class OpCode {
    Copy,

    //arithmetic
    Sum,
    Sub,
    Div,
    Mul,
    Neg,

    //boolean
    And,
    Or,
    Inv,

    //comparision
    Gt,
    Eq,

    //control
    Goto,
    If,

    Param,

    //function related
    Return,
    Call;

    fun isJump() = this == Goto || this == If
}