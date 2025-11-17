package io.github.recrafter.crafter.fabric.config

import io.github.diskria.kotlin.utils.extensions.appendPackageName
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricModEntryPointsConfig(
    @SerialName("main")
    val mainEntryPoints: List<String>? = null,

    @SerialName("client")
    val clientEntryPoints: List<String>? = null,

    @SerialName("server")
    val serverEntryPoints: List<String>? = null,
) {
    companion object {
        fun of(mod: Mod, splitSide: ModSide?): FabricModEntryPointsConfig {
            val clientEntryPoints = FabricModEntryPointsConfig(
                clientEntryPoints = listOf(buildPackageName(mod, ModSide.CLIENT))
            )
            val serverEntryPoints = FabricModEntryPointsConfig(
                serverEntryPoints = listOf(buildPackageName(mod, ModSide.SERVER))
            )
            return when (splitSide) {
                ModSide.CLIENT -> clientEntryPoints
                ModSide.SERVER -> serverEntryPoints
                null -> when (mod.environment) {
                    ModEnvironment.CLIENT_SERVER -> FabricModEntryPointsConfig(
                        mainEntryPoints = listOf(buildPackageName(mod, ModSide.SERVER)),
                        clientEntryPoints = listOf(buildPackageName(mod, ModSide.CLIENT)),
                    )

                    ModEnvironment.CLIENT_ONLY -> clientEntryPoints
                    ModEnvironment.SERVER_ONLY -> FabricModEntryPointsConfig(
                        mainEntryPoints = listOf(buildPackageName(mod, ModSide.SERVER)),
                    )

                    ModEnvironment.DEDICATED_SERVER_ONLY -> serverEntryPoints
                }
            }
        }

        private fun buildPackageName(mod: Mod, side: ModSide): String =
            mod.packageName
                .appendPackageName(side.getName())
                .appendPackageName(mod.getEntryPointName(side))
    }
}
