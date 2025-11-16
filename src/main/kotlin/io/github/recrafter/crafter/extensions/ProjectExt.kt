package io.github.recrafter.crafter.extensions

import io.github.diskria.gradle.utils.extensions.*
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.legacyfabric.legacylooming.LegacyUtilsExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun Project.kotlinApply(block: Project.() -> Unit): Project {
    block(this)
    return this
}

val Project.legacyFabric: LegacyUtilsExtension
    get() {
        ensurePluginApplied("legacy-looming")
        return getExtension<LegacyUtilsExtension>()
    }

val Project.ornithe: PloceusGradleExtensionApi
    get() {
        ensurePluginApplied("ploceus")
        return getExtension<PloceusGradleExtensionApi>()
    }

val Project.craftedSourcesDirectory
    get() = getGeneratedSourcesDirectory().resolve("crafter")

val Project.craftedResourcesDirectory
    get() = getGeneratedResourcesDirectory().resolve("crafter")

fun Project.fabric(configure: LoomGradleExtensionAPI.() -> Unit = {}) {
    withPluginExtension<LoomGradleExtensionAPI>("fabric-loom", configure)
}

fun Project.legacyFabric(configure: LegacyUtilsExtension.() -> Unit = {}) {
    legacyFabric.apply(configure)
}

fun Project.ornithe(configure: PloceusGradleExtensionApi.() -> Unit = {}) {
    ornithe.apply(configure)
}

fun Project.forge(configure: UserDevExtension.() -> Unit = {}) {
    withPluginExtension<UserDevExtension>("net.minecraftforge.gradle", configure)
}

fun Project.neoforge(configure: NeoForgeExtension.() -> Unit = {}) {
    withPluginExtension<NeoForgeExtension>("net.neoforged.moddev", configure)
}

fun Project.kotlin(configure: KotlinProjectExtension.() -> Unit = {}) {
    configureExtension<KotlinProjectExtension>(configure)
}
