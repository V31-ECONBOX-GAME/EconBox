
/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress(
    "unused",
    "nothing_to_inline",
    "useless_cast",
    "unchecked_cast",
    "extension_shadowed_by_member",
    "redundant_projection",
    "RemoveRedundantBackticks",
    "ObjectPropertyName",
    "deprecation",
    "detekt:all"
)
@file:org.gradle.api.Generated

package gradle.kotlin.dsl.accessors._79009060e9cfa46887e56c6188fc230e


import org.gradle.api.Action


/**
 * Retrieves the [versionCatalogs][org.gradle.api.artifacts.VersionCatalogsExtension] extension.
 */
internal
val org.gradle.api.Project.`versionCatalogs`: org.gradle.api.artifacts.VersionCatalogsExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("versionCatalogs") as org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * Configures the [versionCatalogs][org.gradle.api.artifacts.VersionCatalogsExtension] extension.
 */
internal
fun org.gradle.api.Project.`versionCatalogs`(configure: Action<org.gradle.api.artifacts.VersionCatalogsExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("versionCatalogs", configure)



