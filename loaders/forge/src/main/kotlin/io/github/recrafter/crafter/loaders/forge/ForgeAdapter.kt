package io.github.recrafter.crafter.loaders.forge

import io.github.diskria.gradle.utils.extensions.*
import io.github.diskria.gradle.utils.extensions.common.artifact
import io.github.diskria.gradle.utils.helpers.jvm.JvmArguments
import io.github.diskria.gradle.utils.helpers.jvm.Size
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.bedrock.versions.asString
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.ModLoaderAdapter
import io.github.recrafter.crafter.core.extensions.groupLoaderTasks
import io.github.recrafter.crafter.core.extensions.jarJar
import io.github.recrafter.crafter.core.extensions.shadowKotlin
import io.github.recrafter.crafter.loaders.forge.extensions.forge
import io.github.recrafter.crafter.loaders.forge.extensions.jarJar
import io.github.recrafter.crafter.loaders.forge.extensions.minecraftForge
import io.github.recrafter.crafter.mixins.Lapis
import io.github.recrafter.crafter.mixins.MixinExtras
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.repositories
import java.io.File

object ForgeAdapter : ModLoaderAdapter() {

    override fun isDataPackConfigRequired(): Boolean = true

    override fun configurePlugin(mod: Mod, project: Project, runDirectory: File, accessorConfig: File) = with(project) {
        minecraftForge {
            mappings("official", mod.minecraftVersion.asString())
            if (accessorConfig.isFile) {
                setAccessTransformer(true)
                accessTransformer.setFrom(accessorConfig)
            }
            ModSide.entries.forEach { side ->
                runs {
                    register(side.getName()) {
                        workingDir = runDirectory.resolve(side.getName())
                        val memoryRange = when (side) {
                            ModSide.CLIENT -> 2..4
                            ModSide.SERVER -> 4..8
                        }
                        jvmArgs(
                            *JvmArguments.memory(memoryRange, Size.GIGABYTES),
                            JvmArguments.property("mixin.debug.export", true),
                        )
                        args(
                            *JvmArguments.program("mixin.config", mod.mixinConfigPath),
                        )
                        when (side) {
                            ModSide.CLIENT -> args(
                                *JvmArguments.program("username", mod.player),
                            )

                            ModSide.SERVER -> args(
                                *JvmArguments.program("nogui"),
                            )
                        }
                        mods {
                            create(mod.id) {
                                sources(sourceSets.main)
                            }
                        }
                    }
                }
            }
        }
        repositories {
            minecraftForge.mavenizer(this)
            maven(forge.forgeMaven)
            maven(forge.minecraftLibsMaven)
        }
        jarJar {
            register {
                archiveClassifier = null
            }
        }
        dependencies {
            val joptVersion = "5.0.4"
            compileOnly("net.sf.jopt-simple", "jopt-simple", joptVersion).apply {
                (this as? ExternalModuleDependency)?.version {
                    strictly(joptVersion)
                }
            }
            val forgeVersion = "${mod.minecraftVersion.asString()}-${mod.loaderMetadata.loaderVersion}"
            val minecraftArtifact = artifact("net.minecraftforge", "forge", forgeVersion)
            mod.log(project, "Minecraft: $minecraftArtifact")
            implementation(minecraftForge.dependency(minecraftArtifact))
            compileOnly(
                annotationProcessor(MixinExtras.GROUP_ID, MixinExtras.COMMON_ARTIFACT_ID, MixinExtras.VERSION)
            )
            jarJar(MixinExtras.GROUP_ID, MixinExtras.FORGE_ADAPTER_ARTIFACT_ID, MixinExtras.VERSION)
            restoreDependencyResolutionRepositories()
            repositories {
                mavenLocal()
            }
            compileOnly(Lapis.GROUP_ID, Lapis.ANNOTATIONS_ARTIFACT_ID, Lapis.VERSION)
            ksp(Lapis.GROUP_ID, Lapis.KSP_ARTIFACT_ID, Lapis.VERSION)
        }
        tasks {
            jar {
                manifest {
                    attributes["MixinConfigs"] = mod.mixinConfigPath
                }
            }
        }
        shadowKotlin(mod.packageName)
        groupLoaderTasks(
            loaderPackageNamePrefixes = listOf(
                "net.minecraftforge.gradle",
                "net.minecraftforge.jarjar",
            ),
            loader = ModLoaderType.FORGE,
        )
    }
}
