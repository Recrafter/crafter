package io.github.recrafter.crafter.core.sync.common

import io.github.recrafter.bedrock.loaders.ModLoaderType
import io.github.recrafter.crafter.core.sync.maven.MavenComponentSync

abstract class MappingsSync(override val loader: ModLoaderType) : MavenComponentSync() {

    override val componentName: String = "mappings"
}