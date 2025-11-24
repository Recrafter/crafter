package io.github.recrafter.crafter.cli.bash.builder

import io.github.recrafter.crafter.cli.bash.arguments.CLICommandArgumentReference
import io.github.recrafter.crafter.cli.extensions.quoted

@Suppress("UnusedReceiverParameter")
object Conditions {

    fun Conditions.isVarEmpty(variable: String): Condition =
        Condition("-z $variable")

    fun Conditions.isVarEmpty(variable: CLICommandArgumentReference): Condition =
        isVarEmpty(variable.toString())

    fun Conditions.isFileExists(path: String): Condition =
        Condition("-f ${path.quoted()}")

    fun Conditions.isDirectoryExists(path: String): Condition =
        Condition("-d ${path.quoted()}")

    fun Conditions.isPidAlive(pid: StringVar): Condition =
        Condition("kill -0 ${pid.quotedValue} 2>/dev/null")
}
