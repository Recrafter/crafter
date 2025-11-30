package io.github.recrafter.crafter.cli.commands.api.common

import io.github.diskria.gradle.utils.extensions.common.gradleProjectPath
import io.github.diskria.gradle.utils.extensions.common.requireGradleNotNull
import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.KotlinSerializer
import io.github.diskria.kotlin.utils.extensions.common.className
import io.github.diskria.kotlin.utils.extensions.common.failWithUnsupportedType
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.generics.joinToString
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import io.github.recrafter.bedrock.crafter.CrafterFlow
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.arguments.CLIArguments
import io.github.recrafter.crafter.cli.bash.arguments.CLICommandArgumentReference
import io.github.recrafter.crafter.cli.bash.builder.*
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isVarEmpty
import io.github.recrafter.crafter.cli.bash.syntax.BashOperator
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommandEnumArgument
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommandStringArgument
import io.github.recrafter.crafter.cli.commands.api.arguments.Argument
import io.github.recrafter.crafter.cli.commands.api.arguments.EnumArgument
import io.github.recrafter.crafter.cli.commands.api.arguments.EnumArgumentOption
import io.github.recrafter.crafter.cli.commands.api.arguments.StringArgument
import io.github.recrafter.crafter.cli.commands.help.HelpCommand
import io.github.recrafter.crafter.cli.extensions.*
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.properties.intVar
import io.github.recrafter.crafter.cli.properties.stringVar
import io.github.recrafter.crafter.tasks.public.InstallCrafterCLITask
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
        get() = name + Constants.Char.SPACE + arguments.joinBySpace { it.name.wrapWithBrackets(BracketsType.ANGLE) }

    private val referenceArguments: T by lazy {
        Json.decodeFromJsonElement(serializer, buildJsonObject {
            arguments.forEach {
                put(it.name, JsonPrimitive(it.reference.toString()))
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

    fun buildRunCaseBody(fingerprint: Fingerprint): String = bashScript {
        if (hasArguments()) {
            arguments.mapIndexed { index, argument ->
                val default = (argument as? EnumArgument)?.defaultOption?.name.orEmpty()
                initStringVar(argument.reference.name, bash.getScriptArgument(index + 2, default))
            }
            val conditions = arguments
                .filter { it !is EnumArgument || it.defaultOption == null }
                .map { argument -> bash.conditions.isVarEmpty(argument.reference) }
            ifBlock {
                if_(conditions, BashOperator.OR) {
                    printError("Incorrect ${name.singleQuoted()} command usage.")
                    println_("Usage:")
                    withPadding {
                        println_(Cmd.of(fingerprint.scriptName, signature), color = AnsiColor.CYAN)
                    }
                    val helpCmd = Cmd.of(fingerprint.scriptName, HelpCommand.COMMAND_NAME).singleQuoted()
                    println_("Tip: run $helpCmd to see commands, arguments and more.", color = AnsiColor.GRAY)
                    throw_()
                }
            }
            arguments.forEach { argument ->
                val completions = when (argument) {
                    is StringArgument -> getCompletions(argument.name, referenceArguments)
                    is EnumArgument -> argument.options.joinBySpace { it.name }
                }
                val argumentValues = initArrayVar(argument.name.uppercase() + "_VALUES", completions.unquoted())
                val found by intVar(0)
                foreach_(argumentValues) {
                    ifBlock {
                        if_(it.equals_(argument.reference.toString())) {
                            setIntVarValue(found, 1)
                            break_()
                        }
                    }
                }
                ifBlock {
                    if_(found.equals_(0)) {
                        print_("Invalid value ", color = AnsiColor.RED)
                        print_(
                            argument.reference.toString().singleQuoted(),
                            color = AnsiColor.RED,
                            style = AnsiStyle.BOLD
                        )
                        print_(" provided for argument ", color = AnsiColor.RED)
                        print_(argument.name.singleQuoted(), color = AnsiColor.RED, style = AnsiStyle.BOLD)
                        print_(".", color = AnsiColor.RED)
                        println_()
                        print_("Allowed values: ")
                        print_(completions.quoted().squared(), color = AnsiColor.WHITE, style = AnsiStyle.BOLD)
                        println_()
                        print_("Tip: Press ", color = AnsiColor.GRAY)
                        print_(
                            "TAB".wrapWithBrackets(BracketsType.ANGLE),
                            color = AnsiColor.GRAY,
                            style = AnsiStyle.BOLD
                        )
                        println_(" to auto-complete available options.", color = AnsiColor.GRAY)
                        throw_()
                    }
                }
            }
        }
        code {
            run(fingerprint, referenceArguments)
        }
    }

    fun buildCompletionCaseBody(): String = bashScript {
        arguments.size.let { argumentsCount ->
            val indices = (0..<argumentsCount).toList()
            val references = mutableListOf<Pair<Int, CLICommandArgumentReference>>()
            when_(bash.completion.typingWordIndex.value) {
                indices.forEach { index ->
                    val argument = arguments[index]
                    val argumentName = argument.name
                    val argumentIndex = index + 2
                    case_(argumentIndex) {
                        references.forEach { (index, reference) ->
                            initStringVar(reference.name, bash.completion.getWord(index))
                        }
                        references += argumentIndex to CLICommandArgumentReference.of(argumentName)
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
        return@bashScript this
    }

    protected fun BashScriptBuilder.runGradleTaskInBackground(
        taskName: String,
        loader: String,
        version: String,
        pid: StringVar,
    ): StringVar {
        val command = Cmd.gradleTask(taskName, gradleProjectPath(loader, version), listOf("--no-daemon")) {
            mapOf(
                "crafter.flow" to CrafterFlow.Single.name,
                "crafter.loader" to loader,
                "crafter.version" to version,
            )
        }
        val outputDirectoryPath by stringVar(InstallCrafterCLITask.CLI_CACHE_DIRECTORY_PATH.appendPath("gradle-output"))
        createDirectory(outputDirectoryPath.value, recursive = true)
        val logName = listOf(taskName, loader, version, bash.nowDate()).joinToString(Constants.Char.UNDERSCORE)
        val logPath by stringVar(outputDirectoryPath.toString().appendPath(fileName(logName, "log")))
        code { "$command > $logPath 2>&1 &" }
        setStringVarValue(pid, "$!")
        return logPath
    }

    companion object {
        protected const val LOG_HISTORY_SIZE: Int = 7
    }
}
