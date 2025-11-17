package io.github.recrafter.crafter.quilt.config

import io.github.recrafter.crafter.core.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuiltModLinksConfig(
    @SerialName("homepage")
    val homepageUrl: String?,

    @SerialName("sources")
    val repoUrl: String?,

    @SerialName("issues")
    val issuesUrl: String?,
) {
    companion object {
        fun of(mod: Mod): QuiltModLinksConfig =
            QuiltModLinksConfig(
                homepageUrl = mod.homepageUrl,
                repoUrl = mod.repoUrl,
                issuesUrl = mod.issuesUrl,
            )
    }
}
