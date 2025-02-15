/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

import groovy.json.JsonSlurper
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("checkstyle")
    id("pmd")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.0")

    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.codehaus.plexus:plexus-utils:3.4.0")

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        plugin("com.intellij.lang.jsgraphql", "243.21565.122")
        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)

        phpstorm("2024.3")
        bundledPlugin("com.jetbrains.php")
        bundledPlugin("com.intellij.copyright")
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

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
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
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
        channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
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
        useJUnitPlatform()
    }

    checkstyle {
        toolVersion = "8.31"
        isIgnoreFailures = false
        maxWarnings = 0
        configFile = rootProject.file("${rootDir}/gradle-tasks/checkstyle/checkstyle.xml")
    }

    pmd {
        toolVersion = "6.21.0"
        isConsoleOutput = true
        ruleSetFiles = files("${rootDir}/gradle-tasks/pmd/ruleset.xml")
        ruleSets = listOf()
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
                    )
                }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}

// Configure Checkstyle tasks
tasks.withType(Checkstyle::class).configureEach {
    // Specify all files that should be checked
    classpath = files()
    setSource("${project.rootDir}")
}

// Execute Checkstyle on all files
tasks.register<Checkstyle>("checkstyle") {
    // Task-specific configuration can go here if necessary
}

// Execute Checkstyle on all modified files
tasks.register<Checkstyle>("checkstyleCI") {
    val changedFiles = getChangedFiles()
    include(changedFiles)
}

// Configure PMD tasks
tasks.withType(Pmd::class).configureEach {
    // Specify all files that should be checked
    classpath = files()
    setSource("${project.rootDir}")
}

// Execute PMD on all files
tasks.register<Pmd>("pmd") {
    // Task-specific configuration can go here if necessary
}

// Execute PMD on all modified files
tasks.register<Pmd>("pmdCI") {
    val changedFiles = getChangedFiles()
    include(changedFiles)
}

/**
 * Get all files that are changed but not deleted nor renamed.
 * Compares to master or the specified target branch.
 *
 * @return list of all changed files
 */
fun getChangedFiles(): List<String> {
    val modifiedFilesJson = System.getenv("MODIFIED_FILES")
    val files = mutableListOf<String>()

    if (modifiedFilesJson == null) {
        return files
    }

    println("Modified Files: $modifiedFilesJson")

    // Parse the JSON string into a list of files
    val modifiedFiles = JsonSlurper().parseText(modifiedFilesJson) as List<*>

    modifiedFiles.forEach {
        files.add(it.toString())
    }

    // Return the list of touched files
    return files
}

kover {
    currentProject {
        instrumentation {
            excludedClasses.add("org.apache.velocity.*")
        }
    }
}
