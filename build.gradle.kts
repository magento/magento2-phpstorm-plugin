/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
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

val uiTestImplementation = configurations.getByName("uiTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}
val uiTestRuntimeOnly = configurations.getByName("uiTestRuntimeOnly") {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit4)
    testCompileOnly(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.vintage.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
    }

    uiTestImplementation(platform(libs.junit.bom))
    uiTestImplementation(libs.junit.jupiter)
    uiTestImplementation(libs.kodein.di)
    uiTestImplementation(libs.kotlinx.coroutines.core)
    uiTestRuntimeOnly(libs.junit.jupiter.engine)
    uiTestRuntimeOnly(kotlin("stdlib"))
    uiTestRuntimeOnly(kotlin("reflect"))
    uiTestRuntimeOnly(libs.teamcity.serviceMessages)

    implementation(libs.json)
    implementation(libs.plexus.utils)
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
            listOf(it.replace(Regex("-p\\d+(?=-|$)"), "").substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }

    pluginVerification {
        // Requiring a restart is intentional; every compatibility/API warning must fail verification.
        failureLevel = VerifyPluginTask.FailureLevel.ALL - VerifyPluginTask.FailureLevel.NOT_DYNAMIC
        ides {
            create(IntelliJPlatformType.PhpStorm, "2026.2.0.1")
            create(IntelliJPlatformType.PhpStorm, "2026.2.3")
            create(IntelliJPlatformType.PhpStorm, "263.6259.29")
            if (providers.gradleProperty("verifyWebStorm").map(String::toBoolean).getOrElse(false)) {
                create(IntelliJPlatformType.WebStorm, "2026.2.0.1")
                create(IntelliJPlatformType.WebStorm, "263.6259.34")
            }
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
        // Keep routine tests offline; enable the real-release MCP test explicitly.
        if (providers.environmentVariable("MAGENTO_MCP_REAL_RELEASE_TEST").orNull != "true") {
            filter.excludeTestsMatching("com.magento.idea.magento2plugin.mcp.MagentoCompatibilityCommandsTest.testHttpMcpWithActualMagentoReleases")
        }
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

intellijPlatformTesting.testIdeUi.register("uiTest") {
    type = IntelliJPlatformType.WebStorm
    version = providers.gradleProperty("uiTestPlatformVersion")
    useInstaller = true

    // Starter and Driver must match the IDE launched by the UI tests.
    testFramework(TestFrameworkType.Starter)
    uiTestImplementation.extendsFrom(configurations.getByName("intellijPlatformTestDependencies_uiTest"))

    task {
        val uiTestSourceSet = sourceSets.getByName("uiTest")
        testClassesDirs = uiTestSourceSet.output.classesDirs
        classpath = uiTestSourceSet.runtimeClasspath
        // The 2026.3 Starter archive extractor uses the platform's NIO filesystem.
        jvmArgs("--add-exports=java.base/sun.nio.fs=ALL-UNNAMED")
        systemProperty("path.to.build.plugin", tasks.prepareSandbox.get().pluginDirectory.get().asFile)
        systemProperty("ui.test.ide.build", providers.gradleProperty("uiTestPlatformVersion").get())
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

// The same headless IDE runtime as `test`, restricted to the real MCP HTTP contract suites.
intellijPlatformTesting.testIde.register("mcpE2eTest") {
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.JUnit5)

    task {
        group = "verification"
        description = "Exercise Magento MCP tools over real localhost HTTP; no AI Assistant or Docker required."
        classpath += sourceSets.test.get().output
        useJUnitPlatform()
        systemProperty("intellij.platform.kernel.disable", "true")
        systemProperty("ide.fleet.launch", "false")
        filter {
            includeTestsMatching("com.magento.idea.magento2plugin.mcp.MagentoMcpHttpTest")
            includeTestsMatching("com.magento.idea.magento2plugin.mcp.MagentoCompatibilityCommandsTest")
            if (providers.environmentVariable("MAGENTO_MCP_REAL_RELEASE_TEST").orNull != "true") {
                excludeTestsMatching("*.testHttpMcpWithActualMagentoReleases")
            }
        }
        testLogging { events("passed", "skipped", "failed") }
    }
}
