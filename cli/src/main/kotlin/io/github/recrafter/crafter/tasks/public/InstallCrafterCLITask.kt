package io.github.recrafter.crafter.tasks.public

import io.github.diskria.gradle.utils.extensions.getFile
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.gradle.utils.extensions.taskName
import io.github.diskria.gradle.utils.helpers.GradleDirectories
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.crafter.CrafterConstants
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.bash.builder.*
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isFileExists
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.bash.utils.ShellType
import io.github.recrafter.crafter.cli.commands.api.common.AbstractCLICommand
import io.github.recrafter.crafter.cli.commands.craft.CraftCommand
import io.github.recrafter.crafter.cli.commands.help.HelpCommand
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.commands.port.PortCommand
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.quoted
import io.github.recrafter.crafter.cli.extensions.singleQuoted
import io.github.recrafter.crafter.cli.properties.mapVar
import io.github.recrafter.crafter.cli.properties.stringVar
import io.github.recrafter.crafter.core.CrafterTasks
import io.github.recrafter.crafter.core.ModMetadata
import io.github.recrafter.crafter.core.extensions.supportedVersionRange
import io.github.recrafter.crafter.core.properties.ModMetadataProperty
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

@Suppress("UnusedVariable", "unused")
abstract class InstallCrafterCLITask : DefaultTask() {

    @get:Nested
    abstract val modMetadata: ModMetadataProperty

    @get:OutputFile
    abstract val scriptFile: RegularFileProperty

    @get:OutputFile
    abstract val completionScriptFile: RegularFileProperty

    private val commandsProvider: () -> List<AbstractCLICommand<*>> = { commands }

    private val commands: List<AbstractCLICommand<*>> by lazy {
        listOf(
            InitCommand,
            CraftCommand,
            PortCommand,
            HelpCommand(commandsProvider),
        )
    }

    init {
        group = CrafterTasks.PUBLIC_TASKS_GROUP

        scriptFile.convention(project.getFile(SCRIPT_FILE_NAME))
        completionScriptFile.convention(project.getFile(COMPLETION_SCRIPT_FILE_NAME))
    }

    @TaskAction
    fun install() {
        val modMetadata = modMetadata.get()
        val scriptFile = scriptFile.get().asFile.ensureFileExists { setExecutable(true) }
        val completionScriptFile = completionScriptFile.get().asFile.ensureFileExists { setExecutable(true) }

        val fingerprint = buildFingerprint(modMetadata)
        scriptFile.writeText(generateScript(fingerprint))
        completionScriptFile.writeText(generateCompletionScript(fingerprint))

        installCompletionScript(completionScriptFile, fingerprint)
    }

    private fun generateScript(fingerprint: Fingerprint): String = bashScript {
        shebang()
        disclaimer(fingerprint, ScriptType.MAIN)
        setWorkingDirectory(bash.getScriptLocation())
        loaders(fingerprint)
        versions(fingerprint)
        val loaderDisplayNames by mapVar(fingerprint.loaders.associate { it.name to it.displayName })
        val cliFingerprint by stringVar(calculateFingerprintChecksum(fingerprint))
        val gradleFingerprint by stringVar(Constants.Char.EMPTY)
        ifBlock {
            if_(bash.conditions.isFileExists(GRADLE_FINGERPRINT_FILE_PATH)) {
                setStringVarValue(gradleFingerprint, bash.readFile(GRADLE_FINGERPRINT_FILE_PATH).command)
            }
        }
        ifBlock {
            if_(cliFingerprint.equals_(gradleFingerprint.quotedValue).not_()) {
                printError("Some project data has changed since this CLI was installed.")
                printError("Please re-run the CLI install task to sync it with the plugin:")
                println_()
                withPadding {
                    println_(Cmd.gradleTask(fingerprint.gradleTaskName), AnsiColor.CYAN)
                }
                println_()
                throw_()
            }
        }
        val runningCommand = bash.getScriptArgument(1, Constants.Char.EMPTY)
        when_(runningCommand) {
            commands.forEach { command ->
                case_(command.name) {
                    code { command.buildRunCaseBody(fingerprint) }
                }
            }
            else_ {
                printError("Unknown command: ${runningCommand.singleQuoted()}")
                val helpCmd = Cmd.of(fingerprint.scriptName, HelpCommand.COMMAND_NAME).singleQuoted()
                println_("Tip: run $helpCmd to see available commands.", AnsiColor.GRAY)
                throw_()
            }
            return@when_ this
        }
    }

    private fun generateCompletionScript(fingerprint: Fingerprint): String = bashScript {
        disclaimer(fingerprint, ScriptType.COMPLETION)
        val completeFunction = fun_("_${CrafterConstants.PLUGIN_LOWER_NAME}_complete") {
            loaders(fingerprint)
            versions(fingerprint)
            ifBlock {
                if_(bash.completion.typingWordIndex.equals_(1)) {
                    code { bash.completion.reply(commands.joinBySpace { it.name }.quoted()).command }
                    return_()
                }
            }
            when_(bash.completion.getWord(1)) {
                commands.filter { it.hasArguments() }.forEach {
                    case_(it.name) {
                        code { it.buildCompletionCaseBody() }
                    }
                }
                return@when_ this
            }
        }
        run_("complete", "-F $completeFunction ${CrafterConstants.PLUGIN_LOWER_NAME}")
        ifBlock {
            if_(bash.getStringVar("ZSH_VERSION").isNotEmpty()) {
                code { bash.completion.enableZshSupport() }
            }
        }
    }

    private fun BashScriptBuilder.loaders(fingerprint: Fingerprint) {
        val loaders by stringVar(fingerprint.loaders.joinBySpace { it.name })
    }

    private fun BashScriptBuilder.versions(fingerprint: Fingerprint) {
        val versions by mapVar(fingerprint.loaders.associate {
            it.name to it.supportedVersions.joinBySpace { version -> version.asString() }
        })
    }

    private fun installCompletionScript(sourceFile: File, fingerprint: Fingerprint) {
        val shell = ShellType.detect()
        val userHomeDirectory = System.getProperty("user.home").toFile().asDirectoryOrNull() ?: return
        val targetFile = userHomeDirectory
            .resolve(shell.getCompletionPath(fingerprint.scriptName))
            .ensureFileExists { setExecutable(true) }
        sourceFile.copyTo(targetFile, overwrite = true)
        userHomeDirectory.resolve(shell.rcFileName).asFileOrNull()?.let { rcFile ->
            val rcLine = bashScript {
                run_(
                    buildList {
                        if (shell == ShellType.ZSH) {
                            add(bash.completion.enableZshSupport())
                        }
                        add(bash.source(targetFile.absolutePath))
                    }
                )
            }
            if (!rcFile.readLines().contains(rcLine)) {
                rcFile.appendText(rcLine.wrap(Constants.Char.NEW_LINE))
            }
        }
    }

    companion object {
        val CLI_CACHE_DIRECTORY_PATH: String =
            GradleDirectories.CACHE
                .appendPath(CrafterConstants.PLUGIN_LOWER_NAME)
                .appendPath("cli")

        private val SCRIPT_FILE_NAME: String = CrafterConstants.PLUGIN_LOWER_NAME
        private val COMPLETION_SCRIPT_FILE_NAME: String = fileName(CrafterConstants.PLUGIN_LOWER_NAME, "completion")

        private val GRADLE_FINGERPRINT_FILE_PATH: String =
            CLI_CACHE_DIRECTORY_PATH.appendPath(fileName("fingerprint", "md5"))

        fun saveGradleFingerprint(project: Project, modMetadata: ModMetadata) {
            project.rootDirectory
                .resolve(GRADLE_FINGERPRINT_FILE_PATH)
                .ensureFileExists()
                .writeText(calculateFingerprintChecksum(modMetadata))
        }

        fun ensureScriptExists(project: Project, modMetadata: ModMetadata) {
            project.rootDirectory.resolve(SCRIPT_FILE_NAME).ensureFileExists {
                setExecutable(true)
                writeText(generateStubScript(buildFingerprint(modMetadata)))
            }
        }

        private fun BashScriptBuilder.disclaimer(
            fingerprint: Fingerprint,
            scriptType: ScriptType
        ): BashScriptBuilder {
            comment("This ${scriptType.getName()} script was generated by the ${CrafterConstants.PLUGIN_NAME}.")
            val installCommand = Cmd.gradleTask(fingerprint.gradleTaskName).singleQuoted()
            if (scriptType == ScriptType.STUB) {
                comment("Run $installCommand to install ${ScriptType.MAIN.getName()} script.")
            } else {
                comment("Do not edit manually — run $installCommand to regenerate.")
            }
            return this
        }

        private fun generateStubScript(fingerprint: Fingerprint): String = bashScript {
            shebang()
            disclaimer(fingerprint, ScriptType.STUB)
            errorOptions()
            println_("${CrafterConstants.PLUGIN_NAME} CLI is not installed.")
            println_("Run the following command to install it:")
            withPadding {
                println_(Cmd.gradleTask(fingerprint.gradleTaskName), AnsiColor.CYAN)
            }
            println_()
            throw_()
        }

        private fun calculateFingerprintChecksum(fingerprint: Fingerprint): String =
            fingerprint.toString().getChecksum()

        private fun calculateFingerprintChecksum(modMetadata: ModMetadata): String =
            calculateFingerprintChecksum(buildFingerprint(modMetadata))

        private fun buildFingerprint(modMetadata: ModMetadata): Fingerprint = with(modMetadata) {
            Fingerprint(
                pluginVersion = CrafterTasks.pluginVersion,
                gradleTaskName = InstallCrafterCLITask::class.taskName,
                scriptName = SCRIPT_FILE_NAME,
                completionScriptName = COMPLETION_SCRIPT_FILE_NAME,
                modId = id,
                modNamespace = namespace,
                modSides = modMetadata.environment.sides,
                loaders = ModLoaderType.entries.map { loader ->
                    Fingerprint.LoaderInfo(
                        name = loader.getName(`kebab-case`),
                        displayName = loader.displayName,
                        supportedVersions = loader.supportedVersionRange.expand()
                    )
                },
            )
        }

        private enum class ScriptType {
            STUB, MAIN, COMPLETION;
        }
    }
}
