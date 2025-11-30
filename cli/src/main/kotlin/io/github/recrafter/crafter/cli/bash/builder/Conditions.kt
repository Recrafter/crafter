package io.github.recrafter.crafter.cli.bash.builder

import io.github.diskria.kotlin.utils.extensions.wrapWithSpace
import io.github.recrafter.crafter.cli.bash.arguments.CLICommandArgumentReference
import io.github.recrafter.crafter.cli.extensions.quoted
import io.github.recrafter.crafter.cli.extensions.squared

@Suppress("UnusedReceiverParameter")
object Conditions {

    fun Conditions.isVarEmpty(variable: String): Condition =
        Condition("-z $variable".wrapWithSpace().squared(2))

    fun Conditions.isVarEmpty(variable: CLICommandArgumentReference): Condition =
        isVarEmpty(variable.toString())

    fun Conditions.isFileExists(path: String): Condition =
        Condition("-f ${path.quoted()}".wrapWithSpace().squared(2))

    fun Conditions.isDirectoryExists(path: String): Condition =
        Condition("-d ${path.quoted()}".wrapWithSpace().squared(2))

    fun Conditions.isPidAlive(pid: StringVar): Condition =
        Condition("kill -0 ${pid.quotedValue} 2>/dev/null")
}
