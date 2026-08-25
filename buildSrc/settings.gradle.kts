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

pluginManagement {
    // buildSrc is a separate build and does not inherit the root gradle.properties,
    // so it is loaded by hand. Versions stay declared in exactly one file.
    val properties = java.util.Properties()
    File(rootDir.parentFile, "gradle.properties").inputStream().use(properties::load)
    properties.forEach { key, value -> settings.extra.set(key.toString(), value) }
    gradle.rootProject {
        properties.forEach { key, value -> project.extra.set(key.toString(), value) }
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"

listOf("buildJavaVersion", "runtimeJavaVersion", "construoVersion").forEach { name ->
    if (!settings.extra.has(name)) {
        throw GradleException(
            "gradle.properties must set $name. Build logic that compiles to a different " +
                "release from the code it builds is build logic somebody's machine cannot load, " +
                "so there is nothing to fall back to."
        )
    }
}
