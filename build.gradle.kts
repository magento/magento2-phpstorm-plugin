/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestListener

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
    jvmToolchain(25)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets {
    create("uiTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val uiTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val uiTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.14.4")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.14.4")

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
        testFramework(
            TestFrameworkType.Starter,
            configurationName = "uiTestImplementation"
        )
    }

    uiTestImplementation(platform(libs.junit.bom))
    uiTestImplementation(libs.junit.jupiter)
    uiTestImplementation("org.kodein.di:kodein-di-jvm:7.26.1")
    uiTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")
    uiTestRuntimeOnly(libs.junit.jupiter.engine)
    uiTestRuntimeOnly(kotlin("stdlib"))
    uiTestRuntimeOnly(kotlin("reflect"))
    uiTestRuntimeOnly("org.jetbrains.teamcity:serviceMessages:2024.07")

    implementation("org.json:json:20171018")
    implementation("org.codehaus.plexus:plexus-utils:3.5.1")
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginGroup")
        name = providers.gradleProperty("pluginName")
        version = effectivePluginVersion

        vendor {
            name = "Magento Inc."
            url = providers.gradleProperty("pluginRepositoryUrl")
        }

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
    processResources {
        from(layout.projectDirectory.dir("skills")) {
            into("skills")
        }
    }

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
        addTestListener(object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) = Unit

            override fun beforeTest(testDescriptor: TestDescriptor) = Unit

            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit

            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                if (suite.parent == null) {
                    logger.lifecycle(
                        "Test summary: ${result.testCount} run, " +
                            "${result.successfulTestCount} passed, " +
                            "${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped"
                    )
                }
            }
        })
    }


}

val uiTest by intellijPlatformTesting.testIdeUi.registering {
    type = IntelliJPlatformType.WebStorm
    version = providers.gradleProperty("platformVersion")

    task {
        val uiTestSourceSet = sourceSets.getByName("uiTest")
        testClassesDirs = uiTestSourceSet.output.classesDirs
        classpath = uiTestSourceSet.runtimeClasspath
        systemProperty("path.to.build.plugin", tasks.prepareSandbox.get().pluginDirectory.get().asFile)
        systemProperty("ui.test.ide.version", providers.gradleProperty("platformVersion").get())
        useJUnitPlatform()
        dependsOn(tasks.prepareSandbox)

        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
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
