package io.github.recrafter.crafter.fabric.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricModLinksConfig(
    @SerialName("homepage")
    val homepageUrl: String?,

    @SerialName("sources")
    val repoUrl: String?,

    @SerialName("issues")
    val issuesUrl: String?,
) {
    companion object {
        fun of(mod: Mod): FabricModLinksConfig =
            FabricModLinksConfig(
                homepageUrl = mod.homepageUrl,
                repoUrl = mod.repoUrl,
                issuesUrl = mod.issuesUrl,
            )
    }
}
