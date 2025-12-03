package io.github.recrafter.crafter.cli.bash.completion

import io.github.recrafter.crafter.cli.bash.BashCommand

@Suppress("SpellCheckingInspection")
object BashCompletion {

    const val WORDS: String = "COMP_WORDS"
    const val TYPING_WORD_INDEX: String = "COMP_CWORD"
    const val REPLY_ARRAY: String = "COMPREPLY"

    val GENERATE_COMMAND: BashCommand = BashCommand.COMPGEN
    val COMPLETE_COMMAND: BashCommand = BashCommand.COMPLETE
}
