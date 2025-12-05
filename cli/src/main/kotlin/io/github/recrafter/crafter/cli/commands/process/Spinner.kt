package io.github.recrafter.crafter.cli.commands.process

import io.github.recrafter.crafter.cli.bash.ascii.Spinners
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.properties.intVar
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.variables.IntVar
import io.github.recrafter.crafter.cli.bash.variables.StringVar
import io.github.recrafter.crafter.cli.bash.variables.length

class Spinner(val chars: StringVar, val length: IntVar, val progress: IntVar) {
    companion object {
        fun build(builder: ScriptBuilder): Spinner {
            val spinnerChars by builder.stringVar(Spinners.DOTS)
            val spinnerProgress by builder.intVar()
            val spinnerLength by builder.intVar(spinnerChars.length)
            return Spinner(spinnerChars, spinnerLength, spinnerProgress)
        }
    }
}
