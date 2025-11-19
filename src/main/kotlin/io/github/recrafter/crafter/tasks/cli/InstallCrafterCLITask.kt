package io.github.recrafter.crafter.tasks.cli

import io.github.diskria.gradle.utils.extensions.common.gradleError
import io.github.diskria.gradle.utils.extensions.getFile
import io.github.diskria.gradle.utils.extensions.rootDirectory
import io.github.diskria.gradle.utils.extensions.taskName
import io.github.diskria.gradle.utils.helpers.GradleDirectories
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.common.emptyFileName
import io.github.diskria.kotlin.utils.extensions.common.fileName
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.CrafterGradlePlugin
import io.github.recrafter.crafter.core.CrafterConstants
import io.github.recrafter.crafter.core.ModMetadata
import io.github.recrafter.crafter.core.extensions.supportedVersionRange
import io.github.recrafter.crafter.extensions.common.buildScript
import io.github.recrafter.crafter.helpers.shell.ShellHelper
import io.github.recrafter.crafter.tasks.cli.commands.common.Command
import io.github.recrafter.crafter.tasks.cli.commands.help.HelpCommand
import io.github.recrafter.crafter.tasks.cli.commands.init.InitCommand
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class InstallCrafterCLITask : DefaultTask() {

    @get:Internal
    abstract val modMetadata: Property<ModMetadata>

    @get:OutputFile
    abstract val scriptFile: RegularFileProperty

    @get:OutputFile
    abstract val completionScriptFile: RegularFileProperty

    enum class LogoElement { HASH, CREEPER, }
    enum class HashState { IDLE, SPEED_25, SPEED_50, SPEED_75, SPEED_100, CRAFTING_TABLE, }
    enum class CreeperState { A_LETTER, WINK, }

    private val hashAtlas: String = buildScript {
        append {
            // @formatter:off
            """
            ${HashState.IDLE}      __ __  ${HashState.SPEED_25}  __  __ __  ${HashState.SPEED_50}   __ _____  ${HashState.SPEED_75} _ _____ __  ${HashState.SPEED_100} _______ __  ${HashState.CRAFTING_TABLE}     _ __ __ __ _
            ${HashState.IDLE}   __/ // /_ ${HashState.SPEED_25}   __/ // /_ ${HashState.SPEED_50} _ __/ // /_ ${HashState.SPEED_75}  ___/ // /_ ${HashState.SPEED_100} ____/ // /_ ${HashState.CRAFTING_TABLE}    /_ _/_ _/_ _/|
            ${HashState.IDLE}  /_  _  __/ ${HashState.SPEED_25} _ _  _  __/ ${HashState.SPEED_50} _ _  _  __/ ${HashState.SPEED_75} _ _  _  __/ ${HashState.SPEED_100} _ _  _  __/ ${HashState.CRAFTING_TABLE}   /_ _/_ _/_ _/ /  
            ${HashState.IDLE} /_  _  __/  ${HashState.SPEED_25} /_  _  __/  ${HashState.SPEED_50} /_  _  __/  ${HashState.SPEED_75} /_  _  __/  ${HashState.SPEED_100} /_  _  __/  ${HashState.CRAFTING_TABLE}  /_ _/_ _/_ _/ / 
            ${HashState.IDLE}  /_//_/     ${HashState.SPEED_25}  /_//_/     ${HashState.SPEED_50}  /_//_/     ${HashState.SPEED_75}  /_//_/     ${HashState.SPEED_100}  /_//_/     ${HashState.CRAFTING_TABLE}  |_ _ _ _ _ _|/
            """
            // @formatter:on
        }
    }

    private val creeperAtlas: String = buildScript {
        append {
            // @formatter:off
            """
            ${CreeperState.A_LETTER}  ______    ${CreeperState.WINK}  ______    
            ${CreeperState.A_LETTER} /\  __ \   ${CreeperState.WINK} /\ + - \   
            ${CreeperState.A_LETTER} \ \  __ \  ${CreeperState.WINK} \ \  __ \  
            ${CreeperState.A_LETTER}  \ \_\ \_\ ${CreeperState.WINK}  \ \_\ \_\ 
            ${CreeperState.A_LETTER}   \/_/\/_/ ${CreeperState.WINK}   \/_/\/_/ 
            """
            // @formatter:on
        }
    }

    private val commandsProvider: () -> List<Command<*>> = { commands }

    private val commands: List<Command<*>> by lazy {
        listOf(
            InitCommand(),
            HelpCommand(generateAsciiLogo(), commandsProvider),
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

        installCompletionScript(completionScriptFile)
    }

    private fun generateScript(fingerprint: Fingerprint): String = buildScript {
        append { ShellHelper.BASH_SHEBANG }
        append { buildDisclaimer(fingerprint) }
        append {
            """
            set -e
        
            SCRIPT_FINGERPRINT="${calculateFingerprintChecksum(fingerprint)}"
            if [ -f "$GRADLE_FINGERPRINT_FILE_PATH" ]; then
              GRADLE_FINGERPRINT=$(cat "$GRADLE_FINGERPRINT_FILE_PATH")
            else
              GRADLE_FINGERPRINT=""
            fi
        
            if [ ${ShellHelper.variable("SCRIPT_FINGERPRINT")} != ${ShellHelper.variable("GRADLE_FINGERPRINT")} ]; then
              ${ShellHelper.echoRed("Some project data has changed since this CLI was installed.")}
              ${ShellHelper.echoRed("Please re-run CLI install task to sync CLI with the plugin:")}
              ${ShellHelper.echo()}
              ${ShellHelper.echo("  ${buildGradleCommand(fingerprint)} --quiet")}
              ${ShellHelper.echo()}
              ${ShellHelper.fail()}
            fi
        
            COMMAND=${ShellHelper.variable(1)}
            """
        }
        append { ShellHelper.whenBy("COMMAND", commands.map { it.generateRunCase(fingerprint) }) }
    }

    @Suppress("SpellCheckingInspection")
    private fun generateCompletionScript(fingerprint: Fingerprint): String = buildScript {
        append { buildDisclaimer(fingerprint) }
        append {
            """
            _${CrafterConstants.PLUGIN_LOWER_NAME}_complete() {
              local typingWord command
              typingWord=${ShellHelper.arrayElement("COMP_WORDS", index = "COMP_CWORD")}
              command=${ShellHelper.arrayElement("COMP_WORDS", index = 1)}
              COMPREPLY=()
            """
        }
        append { ShellHelper.whenBy("command", commands.map { it.generateCompletionCase(fingerprint) }) }
        indentOut()
        append { "}" }
        append {
            """
            complete -F _${CrafterConstants.PLUGIN_LOWER_NAME}_complete ${CrafterConstants.PLUGIN_LOWER_NAME}
            if [ -n ${ShellHelper.variable("ZSH_VERSION")} ]; then
              $ZSH_COMPLETION_SUPPORT
            fi
            """
        }
    }

    private fun buildDisclaimer(fingerprint: Fingerprint): String = buildScript {
        append {
            val regenerateCommand = buildGradleCommand(fingerprint).wrap(Constants.Char.BACKTICK)
            """
            ${ShellHelper.comment("This script was generated by the ${CrafterConstants.PLUGIN_NAME} Gradle Plugin")}.
            ${ShellHelper.comment("Do not edit manually — run $regenerateCommand to regenerate")}.
            """
        }
    }

    private fun buildGradleCommand(fingerprint: Fingerprint): String =
        "./gradlew ${fingerprint.gradleTaskName}"

    private fun getAsciiFrame(atlas: String, state: Enum<*>): String {
        val nextFrameEnum = state.nextEnumOrNull() ?: state
        return atlas.trimMargin(state.name).trimMarginEnd(nextFrameEnum.name.wrapWithSpace()).trimIndent()
    }

    /**
     * ASCII logo generated via <a href="https://patorjk.com/software/taag/">patorjk.com</a>.
     * Fonts used: <b>Speed</b> for the `#` symbol and <b>Sub-Zero</b> for the text “Crafter”.
     */
    private fun generateAsciiLogo(
        hashState: HashState = HashState.IDLE,
        creeperState: CreeperState = CreeperState.A_LETTER,
    ): String = buildScript {
        append {
            """
            ${LogoElement.HASH}   ______     ______    ${LogoElement.CREEPER}  ______    ______    ______     ______   
            ${LogoElement.HASH}  /\  ___\   /\  == \   ${LogoElement.CREEPER} /\  ___\  /\__  _\  /\  ___\   /\  == \  
            ${LogoElement.HASH}  \ \ \____  \ \  __<   ${LogoElement.CREEPER} \ \  __\  \/_/\ \/  \ \  __\   \ \  __<  
            ${LogoElement.HASH}   \ \_____\  \ \_\ \_\ ${LogoElement.CREEPER}  \ \_\       \ \_\   \ \_____\  \ \_\ \_\
            ${LogoElement.HASH}    \/_____/   \/_/ /_/ ${LogoElement.CREEPER}   \/_/        \/_/    \/_____/   \/_/ /_/
            """
        }
    }
        .replaceMultiLine(LogoElement.HASH.name, getAsciiFrame(hashAtlas, hashState))
        .replaceMultiLine(LogoElement.CREEPER.name, getAsciiFrame(creeperAtlas, creeperState))

    private fun installCompletionScript(sourceFile: File) {
        val shell = System.getenv("SHELL")?.substringAfterLast(Constants.Char.SLASH) ?: "bash"
        val userHomeDirectory = System.getProperty("user.home").toFile()
        val (homePath, prepareCommand) = when (shell) {
            "bash" -> ".local/share/bash-completion/completions/$SCRIPT_FILE_NAME" to null
            "zsh" -> ".zsh/completions/_$SCRIPT_FILE_NAME" to ZSH_COMPLETION_SUPPORT
            else -> gradleError("Unsupported shell: $shell")
        }
        val targetFile = userHomeDirectory.resolve(homePath)
        sourceFile.copyTo(targetFile.ensureFileExists { setExecutable(true) }, overwrite = true)
        val rcFile = userHomeDirectory.resolve(emptyFileName(shell + "rc"))
        val rcLine = listOfNotNull(prepareCommand, "source ${targetFile.absolutePath}").joinToString(ShellHelper.AND)
        if (rcFile.exists() && !rcFile.readLines().contains(rcLine)) {
            rcFile.appendText(rcLine.wrap(Constants.Char.NEW_LINE))
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
                pluginVersion = CrafterGradlePlugin.version,
                gradleTaskName = InstallCrafterCLITask::class.taskName,
                scriptFileName = SCRIPT_FILE_NAME,
                completionScriptFileName = COMPLETION_SCRIPT_FILE_NAME,
                loaderVersions = ModLoaderType.values().associateWith { it.supportedVersionRange.expand() },
                modEnvironment = modMetadata.environment.getName(),
                modNamespace = namespace,
                modId = id,
            )
        }
    }
}
