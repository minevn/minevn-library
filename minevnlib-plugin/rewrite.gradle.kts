import org.gradle.api.GradleException
import org.gradle.jvm.tasks.Jar
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.ow2.asm:asm:9.9.1")
    }
}

val shadowJarTaskNames = setOf("shadowJar", "shadowNoKotlin")
val anvilGuiWrapperClassEntries = setOf(
    "net/minevn/libs/anvilgui/version/Wrapper26_R1.class",
    $$"net/minevn/libs/anvilgui/version/Wrapper26_R1$AnvilContainer.class"
)

tasks.withType<Jar>()
    .matching { task -> task.name in shadowJarTaskNames }
    .configureEach {
        doLast {
            rewriteClassVersion(
                archiveFile.get().asFile,
                anvilGuiWrapperClassEntries,
                Opcodes.V17
            )
        }
    }

fun rewriteClassVersion(jarFile: File, classEntries: Set<String>, targetVersion: Int) {
    val tempFile = File.createTempFile(jarFile.nameWithoutExtension, ".jar", jarFile.parentFile)
    val rewrittenEntries = mutableSetOf<String>()
    try {
        ZipFile(jarFile).use { zipFile ->
            ZipOutputStream(tempFile.outputStream().buffered()).use { zipOutput ->
                val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val newEntry = ZipEntry(entry.name)
                    zipOutput.putNextEntry(newEntry)

                    if (!entry.isDirectory) {
                        val bytes = zipFile.getInputStream(entry).use { input ->
                            input.readBytes()
                        }

                        if (entry.name in classEntries) {
                            val classReader = ClassReader(bytes)
                            val classWriter = ClassWriter(0)
                            val classVisitor = object : ClassVisitor(Opcodes.ASM9, classWriter) {
                                override fun visit(
                                    version: Int,
                                    access: Int,
                                    name: String?,
                                    signature: String?,
                                    superName: String?,
                                    interfaces: Array<out String>?
                                ) {
                                    super.visit(targetVersion, access, name, signature, superName, interfaces)
                                }
                            }

                            classReader.accept(classVisitor, 0)
                            zipOutput.write(classWriter.toByteArray())
                            rewrittenEntries.add(entry.name)
                        } else {
                            zipOutput.write(bytes)
                        }
                    }

                    zipOutput.closeEntry()
                }
            }
        }

        val missingEntries = classEntries - rewrittenEntries
        if (missingEntries.isNotEmpty()) {
            throw GradleException(
                "Failed to rewrite class version in ${jarFile.name}. Missing entries: ${missingEntries.joinToString()}"
            )
        }

        tempFile.copyTo(jarFile, overwrite = true)
        println("file $jarFile rewritten with class version $targetVersion")
    } finally {
        tempFile.delete()
    }
}
