package io.github.recrafter.crafter.cli.commands.craft

import io.github.diskria.kotlin.utils.extensions.appendPath
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.crafter.cli.Fingerprint
import io.github.recrafter.crafter.cli.ascii.Spinners
import io.github.recrafter.crafter.cli.bash.builder.*
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isDirectoryExists
import io.github.recrafter.crafter.cli.bash.builder.Conditions.isPidAlive
import io.github.recrafter.crafter.cli.bash.utils.Cmd
import io.github.recrafter.crafter.cli.commands.api.annotations.CLICommand
import io.github.recrafter.crafter.cli.commands.api.common.AbstractCLICommand
import io.github.recrafter.crafter.cli.commands.init.InitCommand
import io.github.recrafter.crafter.cli.extensions.common.bashScript
import io.github.recrafter.crafter.cli.extensions.common.withBashScript
import io.github.recrafter.crafter.cli.extensions.unquoted
import io.github.recrafter.crafter.cli.properties.intVar
import io.github.recrafter.crafter.cli.properties.stringVar

@CLICommand(name = "craft", description = "Build the mod and launch the selected Minecraft side for development")
object CraftCommand : AbstractCLICommand<CraftArguments>(CraftArguments.serializer()) {

    override fun getCompletions(argumentName: String, arguments: CraftArguments): String = withBashScript {
        when (argumentName) {
            arguments::loader.name -> bash.getStringVar("LOADERS").quotedValue
            arguments::version.name -> bash.getMapVar("VERSIONS").getValue(arguments.loader)
            else -> failWithInvalidValue(argumentName)
        }
    }

    override fun run(fingerprint: Fingerprint, arguments: CraftArguments): String = bashScript {
        val unquotedLoader = arguments.loader.unquoted()
        val unquotedVersion = arguments.version.unquoted()
        val modProjectPath = unquotedLoader.appendPath(unquotedVersion)
        val loaderDisplayName = bash.getMapVar("LOADER_DISPLAY_NAMES").getValue(arguments.loader)
        ifBlock {
            if_(bash.conditions.isDirectoryExists(modProjectPath).not_()) {
                print_("The mod for ", color = AnsiColor.RED)
                print_("$loaderDisplayName ${arguments.version}", color = AnsiColor.RED, style = AnsiStyle.BOLD)
                print_(" is not initialized yet.", color = AnsiColor.RED)
                println_()
                println_("You need to initialize it before crafting.")
                println_("Run the following command:")
                println_()
                withPadding {
                    println_(
                        Cmd.of(fingerprint.scriptName, "${InitCommand.name} $unquotedLoader $unquotedVersion"),
                        color = AnsiColor.CYAN
                    )
                }
                println_()
                throw_()
            }
        }
        val spinner by stringVar(Spinners.DOTS)
        val spinnerProgress by intVar(0)
        val spinnerLength by intVar(spinner.length)
        when_(arguments.side) {
            case_(CraftSideType.CLIENT.getName()) {
                val pid by stringVar()
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(pid)) {
                            run_("kill", pid)
                            run_("wait", pid)
                        }
                    }
                    clearLastLine()
                    printError("Interrupted by user.")
                    return@onInterrupt this
                }
                val logPath = runGradleTaskInBackground("craftClient", unquotedLoader, unquotedVersion, pid)
                watchFileLines(logPath, pid) { line ->
                    val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
                    incrementIntVarValue(spinnerProgress)
                    println_(line)
                    print_(currentSpinnerChar.toString(), color = AnsiColor.GREEN)
                    print_(" ")
                    print_("Crafting")
                    print_(" ")
                    print_("client", color = AnsiColor.GREEN, style = AnsiStyle.BOLD)
                    print_("...")
                }
            }.case_(CraftSideType.SERVER.getName()) {
                val pid by stringVar()
                onInterrupt {
                    ifBlock {
                        if_(bash.conditions.isPidAlive(pid)) {
                            run_("kill", pid)
                            run_("wait", pid)
                        }
                    }
                    clearLastLine()
                    printError("Interrupted by user.")
                    return@onInterrupt this
                }
                val logPath = runGradleTaskInBackground("craftServer", unquotedLoader, unquotedVersion, pid)
                watchFileLines(logPath, pid) { line ->
                    val currentSpinnerChar by stringVar(spinner.getCharAt(spinnerProgress.mod(spinnerLength)))
                    incrementIntVarValue(spinnerProgress)
                    println_(line)
                    print_(currentSpinnerChar.toString(), color = AnsiColor.GREEN)
                    print_(" ")
                    print_("Crafting")
                    print_(" ")
                    print_("server", color = AnsiColor.GREEN, style = AnsiStyle.BOLD)
                    print_("...")
                }
            }
        }
        return@bashScript this
    }
}
