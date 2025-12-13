package io.github.recrafter.crafter.cli.bash.utils

import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.common.emptyFileName
import io.github.diskria.kotlin.utils.extensions.mappers.toEnumOrNull

enum class ShellType(val rcFileName: String) {

    BASH(emptyFileName("bashrc")),
    ZSH(emptyFileName("zshrc"));

    fun getCompletionPath(completionScriptName: String): String =
        when (this) {
            BASH -> ".local/share/bash-completion/completions/$completionScriptName"
            ZSH -> ".zsh/completions/_$completionScriptName"
        }

    companion object {
        fun detect(): ShellType {
            val shell = System.getenv("SHELL")
            val type = shell?.substringAfterLast(Constants.Char.SLASH)?.toEnumOrNull<ShellType>()
            return requireGradleNotNull(type) {
                "Unsupported shell: $shell"
            }
        }
    }
}
