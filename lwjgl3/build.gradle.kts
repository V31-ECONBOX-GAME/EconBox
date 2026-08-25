/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.github.fourlastor.construo.Target

plugins {
    application
    id("io.github.fourlastor.construo")
}

description = "EconBox desktop launcher (LWJGL3)"

val appName = "EconBox"
// Assets live in this module's own resources. The desktop build is the only
// build there will be, so there is nothing to share them with.
val assetsDir = layout.projectDirectory.dir("src/main/resources").asFile
val gdxVersion = property("gdxVersion").toString()

application {
    mainClass = "com.econbox.lwjgl3.Lwjgl3Launcher"
    applicationName = appName
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")

    // Natives are runtime-only payloads with no Java API of their own.
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    runtimeOnly("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    runtimeOnly("com.badlogicgames.gdx:gdx-box2d-platform:$gdxVersion:natives-desktop")

    // Raise LWJGL without adding a dependency edge of this module's own.
    constraints {
        val lwjglVersion = property("lwjglVersion").toString()
        listOf("lwjgl", "lwjgl-glfw", "lwjgl-jemalloc", "lwjgl-openal", "lwjgl-opengl", "lwjgl-stb")
            .forEach { implementation("org.lwjgl:$it:$lwjglVersion") }
    }
}

// Applied to every JavaExec, not just `run`. When the IDE launches main() through Gradle it
// creates a JavaExec task of its own, and that task needs the same flags -- otherwise the run
// starts without them and Java 24+ prints four "restricted method" warnings before the window
// even opens.
tasks.withType<JavaExec>().configureEach {
    // Java 24+ treats JNI and FFM as restricted operations.
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    // GLFW must own the main thread on macOS. StartupHelper re-execs the JVM as a fallback,
    // but the re-exec happens *after* it has already loaded an LWJGL native to inspect the
    // thread, so the parent process warns anyway. Passing the flag up front avoids both the
    // warning and the extra process, and keeps a debugger attached.
    if (providers.systemProperty("os.name").get().lowercase().contains("mac")) {
        jvmArgs("-XstartOnFirstThread")
    }

    // Stops StartupHelper re-execing the JVM when debugging on Linux with an NVIDIA GPU.
    environment("__GL_THREADED_OPTIMIZATIONS", "0")
}

tasks.named<JavaExec>("run") {
    // Running from the resources directory itself means Gdx.files.internal(...) reads the
    // source files, so a texture or a skin can be edited and picked up on the next launch
    // without going through processResources.
    workingDir = assetsDir
}

// ./gradlew lwjgl3:jar -> lwjgl3/build/libs/EconBox-<version>.jar
// Runs anywhere with java -jar; carries the natives for all three desktop platforms.
tasks.named<Jar>("jar") {
    archiveFileName = "$appName-${project.version}.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Resolving the classpath does not imply building :core:jar, so it is depended on
    // explicitly. The trees are expanded during configuration rather than inside a
    // lambda, which would capture the Project and break the configuration cache.
    dependsOn(configurations.named("runtimeClasspath"))
    from(configurations.runtimeClasspath.get().files.map { if (it.isDirectory) it as Any else zipTree(it) })

    // Signatures belong to individual dependencies and fail once everything is merged.
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/maven/**")

    manifest {
        attributes(
            "Main-Class" to application.mainClass.get(),
            "Enable-Native-Access" to "ALL-UNNAMED",
            // LWJGL3 ships multi-release classes selected per JDK version.
            "Multi-Release" to "true",
        )
    }

    doLast {
        archiveFile.get().asFile.setExecutable(true, false)
    }
}

// The application plugin's archives would ship the fat JAR plus a second copy of every
// dependency, roughly 79 MB of duplicate output on every build. Distribution is covered
// by the fat JAR and by construo.
tasks.named("distTar") { enabled = false }
tasks.named("distZip") { enabled = false }

// Self-contained executables with a JDK bundled in, so players need no Java.
// Output lands in lwjgl3/build/construo/dist/.
val jdkVersion = property("jdkBundleVersion").toString()
val jdkMajor = jdkVersion.substringBefore(".")
val jdkTag = "jdk-${jdkVersion.replace("+", "%2B")}"
val jdkFile = jdkVersion.replace("+", "_")

fun temurin(archive: String) =
    "https://github.com/adoptium/temurin$jdkMajor-binaries/releases/download/$jdkTag/" +
        "OpenJDK${jdkMajor}U-jdk_$archive"

construo {
    name = appName
    humanName = appName

    jlink {
        guessModulesFromJar = false
        // The JDK modules libGDX desktop actually touches. Add to this when the game
        // starts using java.net or java.sql, or the packaged build fails at runtime.
        modules.addAll("java.base", "java.desktop", "java.management", "jdk.unsupported")
    }

    targets.register<Target.MacOs>("macM1") {
        architecture = Target.Architecture.AARCH64
        jdkUrl = temurin("aarch64_mac_hotspot_$jdkFile.tar.gz")
        identifier = "com.econbox.$appName"
        versionNumber = project.version.toString()
        macIcon = project.file("icons/logo.icns")
    }
    targets.register<Target.MacOs>("macX64") {
        architecture = Target.Architecture.X86_64
        jdkUrl = temurin("x64_mac_hotspot_$jdkFile.tar.gz")
        identifier = "com.econbox.$appName"
        versionNumber = project.version.toString()
        macIcon = project.file("icons/logo.icns")
    }
    targets.register<Target.Windows>("winX64") {
        architecture = Target.Architecture.X86_64
        jdkUrl = temurin("x64_windows_hotspot_$jdkFile.zip")
        icon = project.file("icons/logo.png")
    }
    targets.register<Target.Linux>("linuxX64") {
        architecture = Target.Architecture.X86_64
        jdkUrl = temurin("x64_linux_hotspot_$jdkFile.tar.gz")
    }
}
