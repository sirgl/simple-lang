package sirgl

interface SlangType {
    override fun toString() : String
}

object IntType : SlangType {
    override fun toString() = "int"
}

object BoolType : SlangType {
    override fun toString() = "bool"
}

object UnitType : SlangType {
    override fun toString() = "unit"
}

object UnknownType : SlangType {
    override fun toString() = "unit"
}