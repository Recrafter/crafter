package io.github.recrafter.crafter.configs.ornithe

import io.github.diskria.kotlin.utils.extensions.appendPackageName
import io.github.diskria.kotlin.utils.extensions.mappers.getName
import io.github.recrafter.bedrock.sides.ModEnvironment
import io.github.recrafter.bedrock.sides.ModSide
import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrnitheModEntryPointsConfig(
    @SerialName("init")
    val initEntryPoints: List<String>? = null,

    @SerialName("client")
    val clientEntryPoints: List<String>? = null,

    @SerialName("server")
    val serverEntryPoints: List<String>? = null,
) {
    companion object {
        fun of(mod: Mod, splitSide: ModSide?): OrnitheModEntryPointsConfig =
            if (splitSide != null) {
                OrnitheModEntryPointsConfig(
                    initEntryPoints = listOf(buildPackageName(mod, splitSide)),
                )
            } else {
                when (mod.environment) {
                    ModEnvironment.CLIENT_SERVER -> OrnitheModEntryPointsConfig(
                        initEntryPoints = listOf(buildPackageName(mod, ModSide.SERVER)),
                        clientEntryPoints = listOf(buildPackageName(mod, ModSide.CLIENT)),
                    )

                    ModEnvironment.CLIENT_ONLY -> OrnitheModEntryPointsConfig(
                        clientEntryPoints = listOf(buildPackageName(mod, ModSide.CLIENT))
                    )

                    ModEnvironment.SERVER_ONLY -> OrnitheModEntryPointsConfig(
                        initEntryPoints = listOf(buildPackageName(mod, ModSide.SERVER)),
                    )

                    ModEnvironment.DEDICATED_SERVER_ONLY -> OrnitheModEntryPointsConfig(
                        serverEntryPoints = listOf(buildPackageName(mod, ModSide.SERVER))
                    )
                }
            }

        private fun buildPackageName(mod: Mod, side: ModSide): String =
            mod.packageName
                .appendPackageName(side.getName())
                .appendPackageName(mod.getEntryPointName(side))
    }
}
