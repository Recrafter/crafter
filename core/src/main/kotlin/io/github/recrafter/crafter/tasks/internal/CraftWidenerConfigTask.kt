package io.github.recrafter.crafter.tasks.internal

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.common.requireGradle
import io.github.diskria.kotlin.regex.dsl.RegexPattern
import io.github.diskria.kotlin.regex.dsl.extensions.common.buildRegexPattern
import io.github.diskria.kotlin.regex.dsl.extensions.findSingleGroupValueOrNull
import io.github.diskria.kotlin.regex.dsl.groups.NamedRegexGroup
import io.github.diskria.kotlin.regex.dsl.primitives.RegexCharacterClass
import io.github.diskria.kotlin.regex.dsl.properties.autoNamedRegexGroup
import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.singleLineIndentSize
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.diskria.kotlin.utils.extensions.wrapWithSingleQuote
import io.github.recrafter.crafter.core.CrafterTasks
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CraftWidenerConfigTask : DefaultTask() {

    init {
        group = CrafterTasks.INTERNAL_GROUP
    }

    @TaskAction
    fun craft() {
        gradleError(buildString {
            appendLine("Mod loader plugins doesn't support lazy AW/AT file properties yet.")
            appendLine("Use CraftWidenerConfigTask.extractClassNames in configuration phase as workaround of it.")
        })
    }

    companion object {
        private val classNameGroup: NamedRegexGroup by RegexCharacterClass
            .ofNegated(Constants.Char.DOUBLE_QUOTE)
            .oneOrMore()
            .autoNamedRegexGroup()

        private val annotationRegex: RegexPattern by lazy {
            buildRegexPattern {
                append("@Widener")
                append(classNameGroup.pattern.wrap(Constants.Char.DOUBLE_QUOTE).wrapWithBrackets(BracketsType.ROUND))
            }
        }

        fun extractClassNames(kotlinClass: File): List<String> {
            val classNames = mutableListOf<String>()
            val stack = mutableListOf<IndentNode>()
            val innerClassSeparator = Constants.Char.DOLLAR.toString()
            kotlinClass.readLines().forEach { line ->
                val className = line.findSingleGroupValueOrNull(classNameGroup, annotationRegex.toRegex())
                    ?: return@forEach
                requireGradle(!className.contains(Constants.Char.DOLLAR)) {
                    "Invalid @Widener value ${className.wrapWithDoubleQuote()}: " +
                            "${innerClassSeparator.wrapWithSingleQuote()} is not supported in this context. " +
                            "Annotate the parent interface and use a separate @Widener for inner classes."
                }
                val indent = line.singleLineIndentSize()
                while (stack.isNotEmpty() && stack.last().indent >= indent) {
                    stack.removeLast()
                }
                val fullName = if (stack.isEmpty() || className.contains(Constants.Char.DOT)) {
                    className
                } else {
                    stack.last().fullName + innerClassSeparator + className
                }
                stack += IndentNode(indent, fullName)
                classNames += fullName
            }
            return classNames
        }

        data class IndentNode(
            val indent: Int,
            val fullName: String
        )
    }
}
