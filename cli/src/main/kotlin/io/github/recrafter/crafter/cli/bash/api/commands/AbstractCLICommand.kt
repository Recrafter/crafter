package io.github.recrafter.crafter.cli.bash.api.commands

import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.KotlinSerializer
import io.github.diskria.kotlin.utils.extensions.common.className
import io.github.diskria.kotlin.utils.extensions.common.failWithUnsupportedType
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.recrafter.bedrock.versions.MinecraftVersionRange
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.ansi.AnsiColor
import io.github.recrafter.crafter.cli.bash.ansi.AnsiStyle
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommandEnumArgument
import io.github.recrafter.crafter.cli.bash.api.annotations.CLICommandStringArgument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.Argument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.EnumArgument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.StringArgument
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.common.CLIArguments
import io.github.recrafter.crafter.cli.bash.api.commands.arguments.enums.EnumArgumentOption
import io.github.recrafter.crafter.cli.bash.builder.ScriptBuilder
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isDirectoryExists
import io.github.recrafter.crafter.cli.bash.conditions.BashConditions.isVarEmpty
import io.github.recrafter.crafter.cli.bash.conditions.not_
import io.github.recrafter.crafter.cli.bash.properties.arrayVar
import io.github.recrafter.crafter.cli.bash.properties.booleanVar
import io.github.recrafter.crafter.cli.bash.properties.intVar
import io.github.recrafter.crafter.cli.bash.properties.stringVar
import io.github.recrafter.crafter.cli.bash.references.VariableReference
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.variables.*
import io.github.recrafter.crafter.cli.commands.help.HelpCommand
import io.github.recrafter.crafter.cli.extensions.*
import io.github.recrafter.crafter.cli.extensions.common.script
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.reflect.full.findAnnotation

abstract class AbstractCLICommand<T : CLIArguments>(private val serializer: KotlinSerializer<T>) {

    val name: String get() = commandAnnotation.name
    val description: String get() = commandAnnotation.description

    val arguments: List<Argument> by lazy {
        serializer.descriptor.elementAnnotations.map { (argumentName, annotations) ->
            val annotation = annotations.filterIsInstance<CLICommandStringArgument>().singleOrNull()
                ?: annotations.filterIsInstance<CLICommandEnumArgument>().singleOrNull()
            requireGradleNotNull(annotation) {
                "Command ${name.singleQuoted()} has an argument ${argumentName.singleQuoted()} " +
                        "that is not annotated as a CLI argument. " +
                        "Annotate it with ${CLICommandStringArgument::class.atName} if it should be " +
                        "a String with arbitrary data, or with ${CLICommandEnumArgument::class.atName} " +
                        "if it should be an Enum with predefined options."
            }
            when (annotation) {
                is CLICommandStringArgument -> StringArgument(argumentName, annotation.description)
                is CLICommandEnumArgument -> {
                    val enums = annotation.enumClass.java.enumConstants.toList()
                    val options = enums.map { EnumArgumentOption(it.argumentName, it.description) }
                    val defaultOption = enums.first().defaultEnum?.argumentName?.let { argumentName ->
                        options.first { option -> option.name == argumentName }
                    }
                    EnumArgument(argumentName, annotation.description, options, defaultOption)
                }

                else -> failWithUnsupportedType(annotation::class)
            }
        }
    }

    val signature: String
        get() = name + Constants.Char.SPACE + arguments.joinBySpace { it.name.angled() }

    private val referenceArguments: T by lazy {
        Json.decodeFromJsonElement(serializer, buildJsonObject {
            arguments.forEach {
                put(it.name, JsonPrimitive(it.variable.name))
            }
        })
    }

    private val commandAnnotation: CLICommand
        get() = requireGradleNotNull(this::class.findAnnotation<CLICommand>()) {
            "Class ${this::class.className()} is not annotated as CLI command." +
                    "Annotate it with ${CLICommand::class.atName}."
        }

    open fun getCompletions(argumentName: String, arguments: T): String =
        Constants.Char.EMPTY.wrapWithDoubleQuote()

    abstract fun run(fingerprint: Fingerprint, arguments: T): String

    fun hasArguments(): Boolean =
        this !is NoArgumentsCommand && arguments.isNotEmpty()

    fun buildRunCaseBody(fingerprint: Fingerprint): String = script {
        if (hasArguments()) {
            arguments.mapIndexed { index, argument ->
                val default = (argument as? EnumArgument)?.defaultOption?.name.orEmpty()
                initString(argument.variable.name, bash.getScriptArgument(index + 2, default))
            }
            val conditions = arguments.mapNotNull { argument ->
                if (argument is EnumArgument && argument.defaultOption != null) {
                    return@mapNotNull null
                }
                bash.conditions.isVarEmpty(argument.variable)
            }
            ifBlock {
                ifAny(conditions) {
                    print_("Incorrect ", AnsiColor.RED)
                    print_(name.singleQuoted(), AnsiColor.RED, AnsiStyle.BOLD)
                    print_(" command usage.", AnsiColor.RED)
                    println_()
                    println_("Usage:")
                    withPadding {
                        println_(Cmd.of(fingerprint.scriptName, signature), AnsiColor.CYAN)
                    }
                    val helpCmd = Cmd.of(fingerprint.scriptName, HelpCommand.COMMAND_NAME).singleQuoted()
                    println_("Tip: run $helpCmd to see commands, arguments and more.", AnsiColor.GRAY)
                    error_()
                }
            }
            arguments.forEach { argument ->
                val completions = when (argument) {
                    is StringArgument -> getCompletions(argument.name, referenceArguments)
                    is EnumArgument -> argument.options.joinBySpace { it.name }
                }
                val argumentValues by arrayVar(completions.unquoted())
                val isFound by booleanVar()
                forEach_(argumentValues) { argumentValue ->
                    ifBlock {
                        if_(argumentValue.equals_(argument.variable.toString())) {
                            setBooleanValue(isFound, true)
                            break_()
                        }
                    }
                }
                ifBlock {
                    if_(isFound.equals_(false)) {
                        print_("Invalid value ", AnsiColor.RED)
                        print_(argument.variable.toString().singleQuoted(), AnsiColor.RED, AnsiStyle.BOLD)
                        print_(" provided for argument ", AnsiColor.RED)
                        print_(argument.name.singleQuoted(), AnsiColor.RED, AnsiStyle.BOLD)
                        print_(".", AnsiColor.RED)
                        println_()
                        print_("Allowed values: ")
                        print_(completions.quoted().squared(), AnsiColor.WHITE, AnsiStyle.BOLD)
                        println_()
                        print_("Tip: Press ", AnsiColor.GRAY)
                        print_(SHOW_COMPLETIONS_KEY.angled(), AnsiColor.GRAY, AnsiStyle.BOLD)
                        println_(" to auto-complete available options.", AnsiColor.GRAY)
                        error_()
                    }
                }
            }
        }
        code {
            run(fingerprint, referenceArguments)
        }
    }

    fun buildCompletionCaseBody(): String = script {
        arguments.size.let { argumentsCount ->
            val indices = (0..<argumentsCount).toList()
            val references = mutableListOf<Pair<Int, VariableReference>>()
            when_(bash.completion.typingWordIndex.value) {
                indices.forEach { index ->
                    val argument = arguments[index]
                    val argumentName = argument.name
                    val argumentIndex = index + 2
                    case_(argumentIndex) {
                        references.forEach { (index, reference) ->
                            initString(reference.name, bash.completion.getWord(index))
                        }
                        references += argumentIndex to VariableReference.of(argumentName)
                        val completions = when (argument) {
                            is StringArgument -> getCompletions(argumentName, referenceArguments)
                            is EnumArgument -> argument.options.joinBySpace { it.name }
                        }
                        code {
                            bash.completion.reply(completions.quoted()).command
                        }
                    }
                }
                return@when_ this
            }
        }
    }


    protected fun ScriptBuilder.detectModProjectName(
        loader: StringVar,
        version: StringVar,
        allowRangeSwitch: Boolean = false,
        found: ScriptBuilder.(projectName: StringVar, isRange: Boolean) -> ScriptBuilder
    ) {
        ifBlock {
            if_(bash.conditions.isDirectoryExists(loader.value.appendPath(version.value))) {
                found(version, false)
            }.else_ {
                val versionsString by stringVar(bash.getMap("VERSIONS").getValue(loader))
                val versionsArray by arrayVar(versionsString)
                val versionIndices = initArrayIndicesMap(versionsArray)
                val versionIndex by intVar(versionIndices.getValue(version))
                val modProjectPaths by arrayVar("$loader/*")
                forEach_(modProjectPaths) { modProjectPath ->
                    ifBlock {
                        if_(bash.conditions.isDirectoryExists(modProjectPath).not_()) {
                            continue_()
                        }
                    }
                    val directoryName by stringVar(modProjectPath.substringAfterLast(Constants.Char.SLASH))
                    val rangeParts by arrayVar(directoryName.split(MinecraftVersionRange.MOD_PROJECT_NAME_SEPARATOR))
                    val rangeMin by stringVar(rangeParts.getElement(0))
                    val rangeMax by stringVar(rangeParts.getElement(1))
                    ifBlock {
                        ifAny(rangeMin.isEmpty(), rangeMax.isEmpty()) {
                            continue_()
                        }
                    }
                    val minIndex by intVar(versionIndices.getValue(rangeMin.value))
                    val maxIndex by intVar(versionIndices.getValue(rangeMax.value))
                    ifBlock {
                        ifAll(
                            minIndex.isNotEmpty(),
                            maxIndex.isNotEmpty(),
                            minIndex.isLessOrEqual(versionIndex),
                            versionIndex.isLessOrEqual(maxIndex),
                        ) {
                            if (allowRangeSwitch) {
                                ifBlock {
                                    if_(versionIndex.equals_(minIndex).not_()) {
                                        print_("Requested version ", AnsiColor.YELLOW)
                                        print_(version, AnsiColor.YELLOW, AnsiStyle.BOLD)
                                        print_(" does not exist as a standalone mod project. ", AnsiColor.YELLOW)
                                        println_()
                                        print_("Automatically using version range ", AnsiColor.YELLOW)
                                        print_(directoryName, AnsiColor.YELLOW, AnsiStyle.BOLD)
                                        print_(" that includes it.", AnsiColor.YELLOW)
                                        println_()
                                        setStringValue(version, rangeMin)
                                    }
                                }
                            }
                            found(directoryName, true)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val SHOW_COMPLETIONS_KEY: String = "TAB"
    }
}
