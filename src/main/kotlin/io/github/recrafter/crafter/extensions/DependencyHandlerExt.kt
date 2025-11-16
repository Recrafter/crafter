package io.github.recrafter.crafter.extensions

import io.github.diskria.gradle.utils.extensions.add
import io.github.diskria.gradle.utils.extensions.common.buildArtifactCoordinates
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.minecraft(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null
): Dependency? =
    add("minecraft", groupId, artefactId, version, classifier)

fun DependencyHandler.mappings(dependencyNotation: Any): Dependency? =
    add("mappings", dependencyNotation)

fun DependencyHandler.mappings(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null
): Dependency? =
    mappings(buildArtifactCoordinates(groupId, artefactId, version, classifier))

fun DependencyHandler.modImplementation(dependencyNotation: Any): Dependency? =
    add("modImplementation", dependencyNotation)

fun DependencyHandler.modImplementation(
    groupId: String,
    artefactId: String,
    version: String,
    classifier: String? = null
): Dependency? =
    add("modImplementation", groupId, artefactId, version, classifier)

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
