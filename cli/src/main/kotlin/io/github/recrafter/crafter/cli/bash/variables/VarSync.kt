package io.github.recrafter.crafter.cli.bash.variables

class VarSync(val varName: String, val strategy: VarSyncStrategy) {
    val fifoPathVarName: String get() = varName + "_FIFO"
    val descriptorVarName: String get() = varName + "_FD"
    val descriptorDataVarName: String get() = descriptorVarName + "_DATA"

    val fifoPath: String get() = "/tmp/${fifoPathVarName.lowercase()}_$$"
    val descriptorPath: String get() = "/proc/$$/fd/$${descriptorVarName}"
}
