package io.github.recrafter.crafter.core.configs.mixins

import kotlinx.serialization.Serializable

@Serializable
data class InjectorConfig(
    val defaultRequire: Int,
) {
    companion object {
        fun newInstance(): InjectorConfig =
            InjectorConfig(
                defaultRequire = 1,
            )
    }
}
