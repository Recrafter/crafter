package io.github.recrafter.crafter.loaders.fabric

import io.github.recrafter.lapis.annotations.internal.Synthetic
import net.fabricmc.loom.api.processor.MinecraftJarProcessor
import net.fabricmc.loom.api.processor.ProcessorContext
import net.fabricmc.loom.api.processor.SpecContext
import org.objectweb.asm.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.extension
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

open class SyntheticStripperProcessor @Inject constructor() : MinecraftJarProcessor<SyntheticStripperProcessor.Spec> {

    override fun getName(): String = "synthetic-stripper"

    override fun buildSpec(context: SpecContext): Spec = Spec

    override fun processJar(jarPath: Path, spec: Spec, context: ProcessorContext) {
        FileSystems.newFileSystem(jarPath, null as ClassLoader?).use { fs ->
            Files.walk(fs.getPath("/"))
                .filter { it.extension == "class" }
                .forEach { classPath -> stripClass(classPath) }
        }
    }

    private fun stripClass(classPath: Path) {
        val oldBytes = classPath.readBytes()
        val reader = ClassReader(oldBytes)
        val writer = ClassWriter(reader, 0)
        reader.accept(StrippingClassVisitor(writer), 0)

        val newBytes = writer.toByteArray()
        if (oldBytes.contentEquals(newBytes)) {
            return
        }
        classPath.writeBytes(newBytes)
    }

    object Spec : MinecraftJarProcessor.Spec
}

class StrippingClassVisitor(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM9, classVisitor) {

    private val annotationDescriptor: String = "L${Synthetic::class.java.name.replace(".", "/")};"

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val isSynthetic = access hasFlag Opcodes.ACC_SYNTHETIC || access hasFlag Opcodes.ACC_BRIDGE
        val newAccess = if (isSynthetic) transformAccess(access) else access
        return super.visitMethod(newAccess, name, descriptor, signature, exceptions)?.apply {
            if (isSynthetic) {
                visitAnnotation(annotationDescriptor, false)?.visitEnd()
            }
        }
    }

    private fun transformAccess(access: Int): Int =
        access
            .minusFlags(Opcodes.ACC_SYNTHETIC, Opcodes.ACC_BRIDGE)
            .minusFlags(Opcodes.ACC_FINAL)
            .minusFlags(Opcodes.ACC_PRIVATE, Opcodes.ACC_PROTECTED)
            .plusFlags(Opcodes.ACC_PUBLIC)

    private infix fun Int.hasFlag(flag: Int): Boolean = (this and flag) != 0

    private fun Int.plusFlags(vararg flags: Int): Int {
        var result = this
        for (flag in flags) {
            result = result or flag
        }
        return result
    }

    private fun Int.minusFlags(vararg flags: Int): Int {
        var result = this
        for (flag in flags) {
            result = result and flag.inv()
        }
        return result
    }
}
