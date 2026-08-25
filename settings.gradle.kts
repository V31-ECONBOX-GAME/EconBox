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

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    // Repositories are declared here and nowhere else, so a subproject cannot quietly
    // pull an artifact from somewhere the rest of the build does not know about.
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "EconBox"

// core   - game logic, no platform backend
// lwjgl3 - desktop launcher for Windows, macOS and Linux
include("core")
include("lwjgl3")
