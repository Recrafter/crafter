package io.github.recrafter.crafter.tasks.internal

import com.palantir.javapoet.*
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.appendPackageName
import io.github.diskria.kotlin.utils.extensions.common.SCREAMING_SNAKE_CASE
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.ensureDirectoryExists
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.diskria.kotlin.utils.words.PascalCase
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.CrafterTasks
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.extensions.family
import io.github.recrafter.crafter.core.properties.ModProperty
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.lang.model.element.Modifier

abstract class CraftEntryPointsTask : DefaultTask() {

    @get:Nested
    abstract val mod: ModProperty

    @get:Input
    abstract val sides: SetProperty<ModSide>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        group = CrafterTasks.INTERNAL_GROUP
    }

    @TaskAction
    fun craft() {
        val mod = mod.get()
        val sides = sides.get()
        val outputDirectory = outputDirectory.get().asFile.ensureDirectoryExists()

        sides.forEach { side ->
            val entryPointClass = buildSideEntryPointClass(mod, side)
            val packageName = mod.packageName.appendPackageName(side.getName())
            val javaFile = JavaFile.builder(packageName, entryPointClass).build()
            mod.log(project, "${side.getName(PascalCase)} entry point generated", javaFile.toString())
            javaFile.writeTo(outputDirectory)
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun buildSideEntryPointClass(mod: Mod, side: ModSide): TypeSpec {
        val environment = mod.environment
        val environmentSide = when {
            side == ModSide.CLIENT -> ModSide.CLIENT
            environment == ModEnvironment.DEDICATED_SERVER_ONLY -> ModSide.SERVER
            else -> null
        }
        val className = mod.getEntryPointName(side)
        val builder = TypeSpec.classBuilder(className).apply {
            addModifiers(Modifier.PUBLIC)
        }
        return when (mod.loader.family) {
            ModLoaderFamily.FABRIC -> {
                val initializerPrefix = when (environmentSide) {
                    ModSide.CLIENT -> environmentSide.getName(PascalCase)
                    ModSide.SERVER -> environment.getName(PascalCase)
                    else -> Constants.Char.EMPTY
                }

                val superInterfaceClass = ClassName.get("net.fabricmc.api", initializerPrefix + "ModInitializer")
                val methodName = "onInitialize" + environmentSide?.getName(PascalCase).orEmpty()
                val initializeMethod = MethodSpec.methodBuilder(methodName).run {
                    addAnnotation(Override::class.java)
                    addModifiers(Modifier.PUBLIC)
                    returns(Void.TYPE)
                    build()
                }
                builder
                    .addSuperinterface(superInterfaceClass)
                    .addMethod(initializeMethod)
                    .apply {
                        if (mod.loader == ModLoaderType.QUILT) {
                            addAnnotation(
                                AnnotationSpec.builder(SuppressWarnings::class.java)
                                    .addMember("value", "\$S", "deprecation")
                                    .build()
                            )
                        }
                    }
                    .build()
            }

            ModLoaderFamily.FORGE -> {
                val modAnnotationPackageName = when (mod.loader) {
                    ModLoaderType.FORGE -> "net.minecraftforge.fml.common"
                    ModLoaderType.NEOFORGE -> "net.neoforged.fml.common"
                    else -> failWithInvalidValue(mod.loader)
                }
                val modAnnotation = AnnotationSpec.builder(ClassName.get(modAnnotationPackageName, "Mod")).run {
                    addMember("value", "\$S", mod.id)

                    if (mod.loader == ModLoaderType.NEOFORGE) {
                        val distEnumName = when (environmentSide) {
                            ModSide.CLIENT -> environmentSide.getName(SCREAMING_SNAKE_CASE)
                            ModSide.SERVER -> environment.getName(SCREAMING_SNAKE_CASE)
                            else -> null
                        }
                        if (distEnumName != null) {
                            val distEnumClass = ClassName.get("net.neoforged.api.distmarker", "Dist")
                            addMember("dist", "\$T.$distEnumName", distEnumClass)
                        }
                    }
                    build()
                }
                builder
                    .addAnnotation(modAnnotation)
                    .build()
            }
        }
    }
}