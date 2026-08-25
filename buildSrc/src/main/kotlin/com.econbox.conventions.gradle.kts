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

// Applied by the root build to every subproject. A rule that belongs to more than one
// project belongs here; a rule that belongs to exactly one belongs in that build file.

plugins {
    `java-library`
}

java {
    toolchain {
        // A toolchain rather than sourceCompatibility: the JDK running the Gradle daemon
        // stops affecting the bytecode, and Gradle provisions the right JDK when the
        // machine does not have it.
        languageVersion = JavaLanguageVersion.of(providers.gradleProperty("buildJavaVersion").get())
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = providers.gradleProperty("runtimeJavaVersion").map(String::toInt)
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // Tests can load the same LWJGL and JNI natives the game does.
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
