package io.github.recrafter.crafter.sync.jfrog

import kotlinx.serialization.Serializable

@Serializable
data class JFrogFolderInfo(
    val repo: String,
    val path: String,
    val created: String,
    val createdBy: String,
    val lastModified: String,
    val modifiedBy: String,
    val lastUpdated: String,
    val children: List<JFrogChild>,
    val uri: String,
)
