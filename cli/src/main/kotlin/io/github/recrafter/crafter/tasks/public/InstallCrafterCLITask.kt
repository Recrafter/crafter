package io.github.recrafter.crafter.tasks.public

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.getFile
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.gradle.utils.extensions.taskName
import io.github.diskria.gradle.utils.helpers.GradleDirectories
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.common.emptyFileName
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.common.`kebab-case`
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.ascii.ASCII
import io.github.recrafter.crafter.cli.commands.bisect.BisectCommand
import io.github.recrafter.crafter.cli.commands.common.Command
import io.github.recrafter.crafter.cli.commands.help.HelpCommand
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.completion.ShellCompletion
import io.github.recrafter.crafter.cli.extensions.common.shellScript
import io.github.recrafter.crafter.cli.shell.ShellHelper
import io.github.recrafter.crafter.cli.shell.ShellScriptBuilder
import io.github.recrafter.crafter.cli.shell.syntax.ShellIf
import io.github.recrafter.crafter.core.CrafterConstants
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

abstract class InstallCrafterCLITask : DefaultTask() {

    @get:Nested
    abstract val modMetadata: ModMetadataProperty

    @get:OutputFile
    abstract val scriptFile: RegularFileProperty

    @get:OutputFile
    abstract val completionScriptFile: RegularFileProperty

    private val commandsProvider: () -> List<Command<*>> = { commands }

    private val commands: List<Command<*>> by lazy {
        listOf(
            InitCommand,
            BisectCommand,
            HelpCommand(ASCII.generateLogo(), commandsProvider),
        )
    }

    init {
        group = CrafterConstants.PUBLIC_TASKS_GROUP

        scriptFile.convention(project.getFile(SCRIPT_FILE_NAME))
        completionScriptFile.convention(project.getFile(COMPLETION_SCRIPT_FILE_NAME))
    }

    @TaskAction
    fun install() {
        val modMetadata = modMetadata.get()
        val scriptFile = scriptFile.get().asFile
        val completionScriptFile = completionScriptFile.get().asFile

        val fingerprint = buildFingerprint(modMetadata)
        scriptFile
            .ensureFileExists { setExecutable(true) }
            .writeText(generateScript(fingerprint))
        completionScriptFile
            .ensureFileExists { setExecutable(true) }
            .writeText(generateCompletionScript(fingerprint))

        installCompletionScript(completionScriptFile, fingerprint)
    }

    private fun generateScript(fingerprint: Fingerprint): String = shellScript {
        shebang()
        disclaimer(fingerprint)
        code { "set -e" }
        loaders(fingerprint)
        versions(fingerprint)
        initVar("CLI_FINGERPRINT", calculateFingerprintChecksum(fingerprint).wrapWithDoubleQuote())
        initVar("GRADLE_FINGERPRINT", Constants.Char.EMPTY, quote = true)
        buildIfThen(
            ShellIf.ofIf(sh.isFileExists(GRADLE_FINGERPRINT_FILE_PATH)) {
                initVar("GRADLE_FINGERPRINT", sh.readFile(GRADLE_FINGERPRINT_FILE_PATH))
            }
        )
        buildIfThen(
            ShellIf.ofIf("${sh.getVar("CLI_FINGERPRINT")} != ${sh.getVar("GRADLE_FINGERPRINT")}") {
                printErr("Some project data has changed since this CLI was installed.")
                printErr("Please re-run CLI install task to sync CLI with the plugin:")
                shellPrintln()
                shellPrintln(
                    ShellHelper.gradleTaskCommand(fingerprint.gradleTaskName),
                    padding = 2
                )
                shellPrintln()
                throwException()
            }
        )
        initVar("COMMAND", sh.getScriptArgument(1))
        buildWhen("COMMAND", commands.map { it.runCase(fingerprint) })
    }

    private fun generateCompletionScript(fingerprint: Fingerprint): String = shellScript {
        disclaimer(fingerprint)
        val completeFunction = function("_${CrafterConstants.PLUGIN_LOWER_NAME}_complete") {
            loaders(fingerprint)
            versions(fingerprint)
            initLocalVar("typing", sh.getArrayValue(ShellCompletion.WORDS, ShellCompletion.CURRENT_WORD))
            initLocalVar("command", sh.getArrayValue(ShellCompletion.WORDS, 1))
            initArray(ShellCompletion.REPLY)
            buildWhen("command", commands.map { it.completionCase(sh.getLocalVar("typing")) })
        }
        code { "complete -F $completeFunction ${CrafterConstants.PLUGIN_LOWER_NAME}" }
        buildIfThen(
            ShellIf.ofIf(sh.isNotEmpty(sh.getVar("ZSH_VERSION"))) {
                code { ZSH_COMPLETION_SUPPORT }
            }
        )
    }

    private fun ShellScriptBuilder.disclaimer(fingerprint: Fingerprint): ShellScriptBuilder {
        val gradleCommand = ShellHelper.gradleTaskCommand(fingerprint.gradleTaskName).wrap(Constants.Char.BACKTICK)
        comment("This script was generated by the ${CrafterConstants.PLUGIN_NAME} Gradle Plugin.")
        comment("Do not edit manually — run $gradleCommand to regenerate.")
        return this
    }

    private fun ShellScriptBuilder.loaders(fingerprint: Fingerprint): ShellScriptBuilder =
        initVar("LOADERS", fingerprint.loaderVersions.keys.joinBySpace(), quote = true)

    private fun ShellScriptBuilder.versions(fingerprint: Fingerprint): ShellScriptBuilder =
        initMap("VERSIONS", fingerprint.loaderVersions)

    private fun installCompletionScript(sourceFile: File, fingerprint: Fingerprint) {
        val shell = System.getenv("SHELL")?.substringAfterLast(Constants.Char.SLASH) ?: "bash"
        val userHomeDirectory = System.getProperty("user.home").toFile()
        val (homePath, prepareCommand) = when (shell) {
            "bash" -> ".local/share/bash-completion/completions/${fingerprint.scriptName}" to null
            "zsh" -> ".zsh/completions/_${fingerprint.scriptName}" to ZSH_COMPLETION_SUPPORT
            else -> gradleError("Unsupported shell: $shell")
        }
        val targetFile = userHomeDirectory.resolve(homePath)
        sourceFile.copyTo(targetFile.ensureFileExists { setExecutable(true) }, overwrite = true)
        val rcFile = userHomeDirectory.resolve(emptyFileName(shell + "rc"))
        if (rcFile.exists()) {
            val rcLine = ShellHelper.runSequentially(listOfNotNull(prepareCommand, "source ${targetFile.absolutePath}"))
            if (!rcFile.readLines().contains(rcLine)) {
                rcFile.appendText(rcLine.wrap(Constants.Char.NEW_LINE))
            }
        }
    }

    companion object {
        private const val ZSH_COMPLETION_SUPPORT: String = "autoload -U bashcompinit && bashcompinit"

        private val SCRIPT_FILE_NAME: String = CrafterConstants.PLUGIN_LOWER_NAME
        private val COMPLETION_SCRIPT_FILE_NAME: String = fileName(CrafterConstants.PLUGIN_LOWER_NAME, "completion")

        private val GRADLE_FINGERPRINT_FILE_PATH: String =
            GradleDirectories.CACHE
                .appendPath(CrafterConstants.PLUGIN_LOWER_NAME)
                .appendPath("cli")
                .appendPath(fileName("fingerprint", "md5"))

        fun saveGradleFingerprint(project: Project, modMetadata: ModMetadata) {
            project
                .rootDirectory
                .resolve(GRADLE_FINGERPRINT_FILE_PATH)
                .ensureFileExists()
                .writeText(calculateFingerprintChecksum(modMetadata))
        }

        internal fun calculateFingerprintChecksum(fingerprint: Fingerprint): String =
            fingerprint.toString().getChecksum()

        internal fun calculateFingerprintChecksum(modMetadata: ModMetadata): String =
            calculateFingerprintChecksum(buildFingerprint(modMetadata))

        private fun buildFingerprint(modMetadata: ModMetadata): Fingerprint = with(modMetadata) {
            Fingerprint(
                pluginVersion = CrafterConstants.pluginVersion,
                gradleTaskName = InstallCrafterCLITask::class.taskName,
                scriptName = SCRIPT_FILE_NAME,
                completionScriptName = COMPLETION_SCRIPT_FILE_NAME,
                loaderVersions = ModLoaderType.values().associate { loader ->
                    loader.getName(`kebab-case`) to loader.supportedVersionRange.expand().joinBySpace { it.asString() }
                },
                modEnvironment = modMetadata.environment.getName(),
                modNamespace = namespace,
                modId = id,
            )
        }
    }
}
