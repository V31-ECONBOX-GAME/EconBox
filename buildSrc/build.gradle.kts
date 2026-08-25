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

plugins {
    // Compiles every *.gradle.kts under src/main/kotlin into a plugin that the rest of
    // the build applies by id.
    `kotlin-dsl`
}

dependencies {
    // Putting construo on the build logic classpath lets lwjgl3 apply it as
    // id("io.github.fourlastor.construo") with no version of its own, the same way a
    // version declared in gradle.properties reaches everything else.
    implementation("io.github.fourlastor:construo:${property("construoVersion")}")
}
