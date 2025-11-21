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
import io.github.recrafter.crafter.extensions.common.shellScript
import io.github.recrafter.crafter.helpers.shell.ShellHelper
import io.github.recrafter.crafter.helpers.shell.ShellScriptBuilder
import io.github.recrafter.crafter.helpers.shell.syntax.ShellIf
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

    private val emptyCraftingTableTile: String = "/___"
    private val occupiedCraftingTableTile: String = "/-*-"
    private val craftingRow: String = emptyCraftingTableTile.repeat(3)

    private val hashAtlas: String = shellScript {
        code {
            // @formatter:off
            """
            ${HashState.IDLE}      __ __  ${HashState.SPEED_25}  __  __ __  ${HashState.SPEED_50}   __ _____  ${HashState.SPEED_75} _ _____ __  ${HashState.SPEED_100} _______ __  
            ${HashState.IDLE}   __/ // /_ ${HashState.SPEED_25}   __/ // /_ ${HashState.SPEED_50} _ __/ // /_ ${HashState.SPEED_75}  ___/ // /_ ${HashState.SPEED_100} ____/ // /_ 
            ${HashState.IDLE}  /_  _  __/ ${HashState.SPEED_25} _ _  _  __/ ${HashState.SPEED_50} _ _  _  __/ ${HashState.SPEED_75} _ _  _  __/ ${HashState.SPEED_100} _ _  _  __/  
            ${HashState.IDLE} /_  _  __/  ${HashState.SPEED_25} /_  _  __/  ${HashState.SPEED_50} /_  _  __/  ${HashState.SPEED_75} /_  _  __/  ${HashState.SPEED_100} /_  _  __/  
            ${HashState.IDLE}  /_//_/     ${HashState.SPEED_25}  /_//_/     ${HashState.SPEED_50}  /_//_/     ${HashState.SPEED_75}  /_//_/     ${HashState.SPEED_100}  /_//_/     
            """
            // @formatter:on
        }
    }

    private val creeperAtlas: String = shellScript {
        code {
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

    private val craftingTableAtlas: String = shellScript {
        code {
            // @formatter:off
            """
            ${CraftingTableState.PREPARING_1}    _ _ ____ _ _  ${CraftingTableState.PREPARING_2}    __ _ __ _ __  ${CraftingTableState.CRAFTING}    ____________ 
            ${CraftingTableState.PREPARING_1}   /_ _/_ _/_ _/| ${CraftingTableState.PREPARING_2}   /__ / _ / __/  ${CraftingTableState.CRAFTING}   $craftingRow/|
            ${CraftingTableState.PREPARING_1}  /_ _/_ _/_ _/   ${CraftingTableState.PREPARING_2}  /__ / _ / __/ / ${CraftingTableState.CRAFTING}  $craftingRow/ /
            ${CraftingTableState.PREPARING_1} /_ _/_ _/_ _/ /  ${CraftingTableState.PREPARING_2} /__ / _ / __/    ${CraftingTableState.CRAFTING} $craftingRow/ /
            ${CraftingTableState.PREPARING_1} |_ _ _ _ _ _|    ${CraftingTableState.PREPARING_2} | _ _ _ _ _ |/   ${CraftingTableState.CRAFTING} |___________|/
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

        installCompletionScript(completionScriptFile, fingerprint)
    }

    private fun generateScript(fingerprint: Fingerprint): String = shellScript {
        shebang()
        disclaimer(fingerprint)
        code { "set -e" }
        initVar("CLI_FINGERPRINT", calculateFingerprintChecksum(fingerprint).wrapWithDoubleQuote())
        shellIfThen(
            ShellIf.ofIf(isFileExists(GRADLE_FINGERPRINT_FILE_PATH)) {
                initVar("GRADLE_FINGERPRINT", sh.readFile(GRADLE_FINGERPRINT_FILE_PATH))
            },
            ShellIf.ofElse {
                initVar("GRADLE_FINGERPRINT", Constants.Char.EMPTY, quote = true)
            },
        )
        shellIfThen(
            ShellIf.ofIf("${getVar("CLI_FINGERPRINT")} != ${getVar("GRADLE_FINGERPRINT")}") {
                printErr("Some project data has changed since this CLI was installed.")
                printErr("Please re-run CLI install task to sync CLI with the plugin:")
                shellPrintln()
                shellPrintln(
                    ShellHelper.gradleCommand(fingerprint.gradleTaskName, quiet = true),
                    padding = 2
                )
                shellPrintln()
                throwException()
            }
        )
        initVar("COMMAND", getScriptArgument(1))
        shellWhen("COMMAND", commands.map { it.generateRunCase(fingerprint) })
    }

    @Suppress("SpellCheckingInspection")
    private fun generateCompletionScript(fingerprint: Fingerprint): String = shellScript {
        val functionName = "_${CrafterConstants.PLUGIN_LOWER_NAME}_complete"
        disclaimer(fingerprint)
        shellFun(functionName) {
            declareLocalVar("current", getArrayValue("COMP_WORDS", "COMP_CWORD"))
            declareLocalVar("command", getArrayValue("COMP_WORDS", 1))
            initArray("COMPREPLY")
            shellWhen("command", commands.map { it.generateCompletionCase(fingerprint, getLocalVar("current")) })
        }
        code { "complete -F $functionName ${CrafterConstants.PLUGIN_LOWER_NAME}" }
        shellIfThen(
            ShellIf.ofIf("-n ${getVar("ZSH_VERSION")}") {
                code { ZSH_COMPLETION_SUPPORT }
            }
        )
    }

    private fun ShellScriptBuilder.disclaimer(fingerprint: Fingerprint): ShellScriptBuilder {
        val gradleCommand = ShellHelper.gradleCommand(fingerprint.gradleTaskName).wrap(Constants.Char.BACKTICK)
        comment("This script was generated by the ${CrafterConstants.PLUGIN_NAME} Gradle Plugin.")
        comment("Do not edit manually — run $gradleCommand to regenerate.")
        return this
    }

    private fun getAsciiFrame(atlas: String, state: Enum<*>): String {
        val nextFrameEnum = state.nextEnumOrNull() ?: state
        val trimmedLeft = atlas.trimMargin(state.name)
        val trimmedRight = trimmedLeft.trimMarginEnd(nextFrameEnum.name.wrapWithSpace())
        return trimmedRight.trimIndent()
    }

    /**
     * ASCII logo generated via <a href="https://patorjk.com/software/taag/">patorjk.com</a>.
     * Fonts used: <b>Speed</b> for the `#` symbol and <b>Sub-Zero</b> for the text “Crafter”.
     */
    private fun generateAsciiLogo(
        hashState: HashState = HashState.IDLE,
        creeperState: CreeperState = CreeperState.A_LETTER,
    ): String = shellScript {
        code {
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
        val rcLine = listOfNotNull(prepareCommand, "source ${targetFile.absolutePath}").joinToString("&".repeat(2))
        if (rcFile.exists() && !rcFile.readLines().contains(rcLine)) {
            rcFile.appendText(rcLine.wrap(Constants.Char.NEW_LINE))
        }
    }

    enum class LogoElement { HASH, CREEPER, }
    enum class HashState { IDLE, SPEED_25, SPEED_50, SPEED_75, SPEED_100, }
    enum class CreeperState { A_LETTER, WINK, }
    enum class CraftingTableState { PREPARING_1, PREPARING_2, CRAFTING, }

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
                scriptName = SCRIPT_FILE_NAME,
                completionScriptName = COMPLETION_SCRIPT_FILE_NAME,
                loaderVersions = ModLoaderType.values().associateWith { it.supportedVersionRange.expand() },
                modEnvironment = modMetadata.environment.getName(),
                modNamespace = namespace,
                modId = id,
            )
        }
    }
}
