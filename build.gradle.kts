import java.security.MessageDigest

plugins {
    id("java")
    id("maven-publish")
    id("signing")
}
group = "dev.astles"
version = "0.0.3"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.jar {
    include("dev/astles/**")
    exclude("dev/astles/exclude/**")
}

fun File.hash(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    inputStream().use { fis ->
        val buffer = ByteArray(8192)
        var read: Int
        while (fis.read(buffer).also { read = it } > 0) {
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

val prepareMavenUpload by tasks.registering {
    group = "publishing"
    description = "Prepare Maven artifacts with signatures, checksums, folder structure, and zip"

    val outputDir = layout.buildDirectory.dir("maven-upload")

    doFirst {
        val output = outputDir.get().asFile
        if (output.exists()) output.deleteRecursively()
    }

    dependsOn(
        tasks.named("build"),
        tasks.named("generatePomFileForMavenJavaPublication")
    )

    doLast {
        val baseDir = outputDir.get().asFile.resolve("dev/astles/SwingDevTools/$version")
        baseDir.mkdirs()

        val mainJar = tasks.named("jar").get().outputs.files.singleFile
        val sourcesJar = tasks.named("sourcesJar").get().outputs.files.singleFile
        val javadocJar = tasks.named("javadocJar").get().outputs.files.singleFile
        val pomFile = tasks.named("generatePomFileForMavenJavaPublication").get().outputs.files.singleFile

        val artifacts = listOf(
            "SwingDevTools-$version.jar" to mainJar,
            "SwingDevTools-$version-sources.jar" to sourcesJar,
            "SwingDevTools-$version-javadoc.jar" to javadocJar,
            "SwingDevTools-$version.pom" to pomFile
        )

        artifacts.forEach { (name, file) ->
            val target = File(baseDir, name)
            file.copyTo(target, overwrite = true)

            exec {
                commandLine("gpg","--batch", "--yes", "--armor", "--detach-sign", target.absolutePath)
            }

            val md5 = File(target.parentFile, "$name.md5")
            val sha1 = File(target.parentFile, "$name.sha1")
            md5.writeText(target.hash("MD5"))
            sha1.writeText(target.hash("SHA-1"))
        }

        println("All artifacts, signatures, and checksums are in: ${baseDir.absolutePath}")

        // Create a zip of the folder
        val zipFile = outputDir.get().asFile.resolve("SwingDevTools-$version-maven.zip")
        ant.withGroovyBuilder {
            "zip"(
                "destfile" to zipFile.absolutePath,
                "basedir" to outputDir.get().asFile.absolutePath,
                "includes" to "dev/**"
            )
        }

        println("Zipped Maven upload folder: ${zipFile.absolutePath}")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Swing Dev Tools")
                description.set("Highlights Swing Components in the UI to aid debugging")
                url.set("https://github.com/chrissyast/SwingDevTools")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                }

                developers {
                    developer {
                        id.set("chrissyast")
                        name.set("Chris Astles")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/chrissyast/SwingDevTools.git")
                    developerConnection.set("scm:git:ssh://github.com/chrissyast/SwingDevTools.git")
                    url.set("https://github.com/chrissyast/SwingDevTools")
                }
            }
        }
    }
}

signing {
    //sign(publishing.publications["mavenJava"])
    useGpgCmd()
}




repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}