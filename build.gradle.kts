/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
}

group = providers.gradleProperty("pluginGroup").get()
val basePluginVersion = providers.gradleProperty("pluginVersion")
val isGithubPrerelease = providers.environmentVariable("GITHUB_RELEASE_PRERELEASE")
    .map(String::toBoolean)
    .orElse(false)
val effectivePluginVersion = providers.provider {
    val pluginVersion = basePluginVersion.get()

    if (!isGithubPrerelease.get()) {
        pluginVersion
    } else {
        val releaseId = providers.environmentVariable("GITHUB_RELEASE_ID").orNull
            ?: throw GradleException("GITHUB_RELEASE_ID is required when publishing a GitHub prerelease.")

        "$pluginVersion-alpha.$releaseId"
    }
}

version = effectivePluginVersion.get()

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.10.2")

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
    }

    implementation("org.json:json:20171018")
    implementation("org.codehaus.plexus:plexus-utils:3.5.1")
}

intellijPlatform {
    pluginConfiguration {
        version = effectivePluginVersion

        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        changeNotes = basePluginVersion.map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("MAGENTO_PHPSTORM_intellijPublishToken")
        channels = effectivePluginVersion.map {
            listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    test {
        val excludePatterns = project.findProperty("excludeTests") as String?

        if (!excludePatterns.isNullOrEmpty()) {
            // Split the comma-separated string and apply exclusions
            excludePatterns.split(",").forEach {
                exclude(it.trim())
            }
        }

        // Workaround for kernel-related crashes in tests (Fleet/Platform Kernel background tasks)
        systemProperty("intellij.platform.kernel.disable", "true")
        systemProperty("ide.fleet.launch", "false")

        useJUnitPlatform()
    }


}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                        "-Deap.require.license=true",
                        "-Dide.show.tips.on.startup.default.value=false"
                    )
                }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}

kover {
    currentProject {
        instrumentation {
            excludedClasses.add("org.apache.velocity.*")
        }
    }
}
