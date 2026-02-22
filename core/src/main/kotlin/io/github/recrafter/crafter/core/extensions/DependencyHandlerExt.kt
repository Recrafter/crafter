package io.github.recrafter.crafter.core.extensions

import io.github.diskria.gradle.utils.extensions.common.artifact
import io.github.diskria.gradle.utils.extensions.ensurePluginApplied
import io.github.diskria.gradle.utils.extensions.requireDependencyNotNull
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.minecraft(dependencyNotation: Any): Dependency? =
    add("minecraft", dependencyNotation)

fun DependencyHandler.minecraft(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null,
): Dependency {
    val artifact = artifact(groupId, artefactId, version, classifier)
    return minecraft(artifact).requireDependencyNotNull("minecraft", artifact)
}

fun DependencyHandler.forge(dependencyNotation: Any): Dependency? =
    add("forge", dependencyNotation)

fun DependencyHandler.forge(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null,
): Dependency {
    val artifact = artifact(groupId, artefactId, version, classifier)
    return forge(artifact).requireDependencyNotNull("forge", artifact)
}

fun DependencyHandler.mappings(dependencyNotation: Any): Dependency? =
    add("mappings", dependencyNotation)

fun DependencyHandler.mappings(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null,
): Dependency {
    val artifact = artifact(groupId, artefactId, version, classifier)
    return mappings(artifact).requireDependencyNotNull("mappings", artifact)
}

fun DependencyHandler.modImplementation(dependencyNotation: Any): Dependency? =
    add("modImplementation", dependencyNotation)

fun DependencyHandler.modImplementation(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null,
): Dependency {
    val artifact = artifact(groupId, artefactId, version, classifier)
    return modImplementation(artifact).requireDependencyNotNull("modImplementation", artifact)
}

fun DependencyHandler.include(dependencyNotation: Any): Dependency? =
    add("include", dependencyNotation)

fun DependencyHandler.include(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null,
): Dependency {
    val artifact = artifact(groupId, artefactId, version, classifier)
    return include(artifact).requireDependencyNotNull("include", artifact)
}

fun DependencyHandler.jarJar(dependencyNotation: Any): Dependency? =
    add("jarJar", dependencyNotation)

fun DependencyHandler.jarJar(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null,
): Dependency {
    val artifact = artifact(groupId, artefactId, version, classifier)
    return jarJar(artifact).requireDependencyNotNull("jarJar", artifact)
}

fun DependencyHandler.clientExceptions(dependencyNotation: Any): Dependency? =
    add("clientExceptions", dependencyNotation)

fun DependencyHandler.serverExceptions(dependencyNotation: Any): Dependency? =
    add("serverExceptions", dependencyNotation)

fun DependencyHandler.clientSignatures(dependencyNotation: Any): Dependency? =
    add("clientSignatures", dependencyNotation)

fun DependencyHandler.serverSignatures(dependencyNotation: Any): Dependency? =
    add("serverSignatures", dependencyNotation)

fun DependencyHandler.clientNests(dependencyNotation: Any): Dependency? =
    add("clientNests", dependencyNotation)

fun DependencyHandler.serverNests(dependencyNotation: Any): Dependency? =
    add("serverNests", dependencyNotation)
