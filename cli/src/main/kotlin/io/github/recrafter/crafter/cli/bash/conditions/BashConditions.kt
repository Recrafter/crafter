package io.github.recrafter.crafter.cli.bash.conditions

import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import io.github.recrafter.crafter.cli.bash.variables.quotedValue
import io.github.recrafter.crafter.cli.bash.variables.value
import io.github.recrafter.crafter.cli.extensions.quoted

@Suppress("UnusedReceiverParameter")
object BashConditions {

    fun BashConditions.isVarEmpty(variable: String): BashCondition =
        BashCondition.from("-z $variable")

    fun BashConditions.isVarEmpty(variable: VariableReference): BashCondition =
        isVarEmpty(variable.toString())

    fun BashConditions.exists(path: String): BashCondition =
        BashCondition.from("-e $path")

    fun BashConditions.isFileExists(path: String): BashCondition =
        BashCondition.from("-f ${path.quoted()}")

    fun BashConditions.isDirectoryExists(path: String): BashCondition =
        BashCondition.from("-d ${path.quoted()}")

    fun BashConditions.isDirectoryExists(path: StringVar): BashCondition =
        isDirectoryExists(path.value)

    fun BashConditions.isPidAlive(pid: StringVar): BashCondition =
        BashCondition.from("kill -0 ${pid.quotedValue} 2>/dev/null", squared = false)

    fun BashConditions.isDescriptorReady(descriptor: String, data: String): BashCondition =
        BashCondition.from("IFS='' read -r -t 0.01 $data <&$${descriptor} 2>/dev/null", squared = false)
}
