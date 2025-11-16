package io.github.recrafter.crafter.configs.ornithe

import io.github.recrafter.crafter.models.Mod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrnitheModLinksConfig(
    @SerialName("homepage")
    val homepageUrl: String?,

    @SerialName("sources")
    val repoUrl: String?,

    @SerialName("issues")
    val issuesUrl: String?,
) {
    companion object {
        fun of(mod: Mod): OrnitheModLinksConfig =
            OrnitheModLinksConfig(
                homepageUrl = mod.homepageUrl,
                repoUrl = mod.repoUrl,
                issuesUrl = mod.issuesUrl,
            )
    }
}
