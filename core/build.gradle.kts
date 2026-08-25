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
    `java-library`
}

description = "EconBox core game logic, shared by every platform"

dependencies {
    // api, not implementation: the lwjgl3 launcher uses these types directly, and so
    // would an android or web module added later.
    api("com.badlogicgames.gdx:gdx:${property("gdxVersion")}")
    api("com.badlogicgames.gdx:gdx-freetype:${property("gdxVersion")}")
    api("com.badlogicgames.gdx:gdx-box2d:${property("gdxVersion")}")
    api("com.badlogicgames.ashley:ashley:${property("ashleyVersion")}")

    // Only the Java APIs belong here. The natives-desktop artifacts are a property of
    // the platform, so lwjgl3 supplies them; core must not know where it runs.
}
