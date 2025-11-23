package io.github.recrafter.crafter.core.configs.mixins

import io.github.diskria.kotlin.utils.extensions.appendPackageName
import io.github.diskria.kotlin.utils.serialization.annotations.EncodeDefaults
import io.github.diskria.kotlin.utils.serialization.annotations.PrettyPrint
import io.github.recrafter.bedrock.loaders.ModLoaderFamily
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.Mod
import io.github.recrafter.crafter.core.extensions.family
import io.github.recrafter.crafter.core.extensions.toInt
import io.github.recrafter.crafter.core.helpers.MixinsHelper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@EncodeDefaults
@PrettyPrint
data class MixinsConfig(
    @SerialName("required")
    val isRequired: Boolean = true,

    val minVersion: String = "0.8",

    @SerialName("package")
    val packageName: String,

    @SerialName("compatibilityLevel")
    val jvmTargetVersion: String,

    @SerialName("injectors")
    val injectorConfig: InjectorConfig,

    @SerialName("overwrites")
    val overwriteConfig: OverwriteConfig,

    val refmap: String? = null,

    @SerialName("mixins")
    val mainMixins: List<String>? = null,

    @SerialName("client")
    val clientMixins: List<String>? = null,

    @SerialName("server")
    val serverMixins: List<String>? = null,
) {
    companion object {
        fun of(mod: Mod, sideMixins: Map<ModSide, List<String>>): MixinsConfig =
            MixinsConfig(
                packageName = mod.packageName.appendPackageName(MixinsHelper.MIXINS_NAME),
                jvmTargetVersion = "JAVA_${mod.jvmTarget.toInt()}",
                injectorConfig = InjectorConfig.newInstance(),
                overwriteConfig = OverwriteConfig.newInstance(),
                refmap = if (mod.loader.family == ModLoaderFamily.FABRIC) mod.refmapFileName else null,
                mainMixins = when (mod.environment) {
                    ModEnvironment.DEDICATED_SERVER_ONLY -> null
                    else -> sideMixins[ModSide.SERVER]
                },
                clientMixins = sideMixins[ModSide.CLIENT],
                serverMixins = when (mod.environment) {
                    ModEnvironment.DEDICATED_SERVER_ONLY -> sideMixins[ModSide.SERVER]
                    else -> null
                },
            )
    }
}
