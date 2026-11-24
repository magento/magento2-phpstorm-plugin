/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.intellij.platform.gradle.tasks.ComposedJarTask
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.language.jvm.tasks.ProcessResources
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
apply(from = "gradle/release-version.gradle.kts")
val releaseVersion = version.toString()
val effectivePluginVersion = providers.provider { releaseVersion }

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.intellij.platform.module")
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        intellijPlatform { defaultRepositories() }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }
    tasks.named<ComposedJarTask>("composedJar") {
        archiveBaseName = "${project.group}.${project.name.replace('-', '.')}"
    }
}

project(":mcp-skills") {
    dependencies {
        compileOnly(rootProject)
        intellijPlatform {
            val platformLocalPath = providers.gradleProperty("platformLocalPath")
            if (platformLocalPath.isPresent) {
                local(platformLocalPath)
            } else {
                create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
            }
            bundledPlugin("com.intellij.mcpServer")
        }
    }
}

sourceSets {
    create("coreTest") {
        compileClasspath += sourceSets.test.get().compileClasspath
        runtimeClasspath += sourceSets.test.get().runtimeClasspath - sourceSets.test.get().output
        java.srcDir("src/test/java")
        java.include("com/magento/idea/magento2plugin/BaseProjectTestCase.java")
    }
    create("uiTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

for (suite in listOf("test", "uiTest")) {
    kotlin.sourceSets.named(suite) { kotlin.srcDir("src/maintenanceTest/kotlin") }
}
tasks.named<ProcessResources>("processUiTestResources") {
    from("src/test/resources") { include("maintenance/**") }
}

kotlin.target.compilations.getByName("coreTest").associateWith(kotlin.target.compilations.getByName("main"))
configurations["coreTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["coreTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val uiTestImplementation = configurations.getByName("uiTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}
val uiTestRuntimeOnly = configurations.getByName("uiTestRuntimeOnly") {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    runtimeOnly(project(":mcp-skills"))
    runtimeOnly(project(":php-mcp"))
    runtimeOnly(project(":php-graphql"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit4)
    testCompileOnly(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.vintage.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    intellijPlatform {
        val platformLocalPath = providers.gradleProperty("platformLocalPath")
        if (platformLocalPath.isPresent) {
            local(platformLocalPath)
        } else {
            create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        }

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
        val stagedAlpha = providers.environmentVariable("GITHUB_EVENT_NAME").orNull == "workflow_dispatch" &&
            providers.environmentVariable("GITHUB_RELEASE_PRERELEASE").orNull.toBoolean()
        val repositoryUrl = providers.environmentVariable("GITHUB_SERVER_URL")
            .zip(providers.environmentVariable("GITHUB_REPOSITORY")) { server, repo -> "$server/$repo" }
            .orElse(providers.gradleProperty("pluginRepositoryUrl")).get()
        val installerUrl = "$repositoryUrl/releases/download/$releaseVersion/magento-hce.pyz"
        val pluginId = providers.gradleProperty("pluginGroup").get()
        val hceDownloadUrl = "https://plugins.jetbrains.com/plugin/download?pluginId=$pluginId&version=$releaseVersion-hce&channel=hce-alpha"
        changeNotes = basePluginVersion.map { pluginVersion ->
            val changes = with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
            if (stagedAlpha) {
                val setup = """
                    From the root of a Magento checkout with Composer dependencies installed, run:

                    ```sh
                    installer=$(mktemp) && curl -fLsS '$installerUrl' -o "${'$'}installer" && python3 "${'$'}installer" setup --client codex
                    ```

                    For Claude Code, use `--client claude`. The installer downloads this release’s [HCE plugin ZIP from JetBrains Marketplace]($hceDownloadUrl).
                """.trimIndent()
                changes + markdownToHTML(setup)
            } else {
                changes
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
            val channel = it.replace(Regex("-p\\d+(?=-|$)"), "").substringAfter('-', "").substringBefore('.')
            listOf(channel.replace(Regex("^alpha-\\d+$"), "alpha").ifEmpty { "default" })
        }
    }

    pluginVerification {
        // Verifier scans the optional bundled-skills module even on builds that skip loading it.
        ignoredProblemsFile = layout.projectDirectory.file("gradle/plugin-verifier-ignored-problems.txt")
        // Experimental API use is checked against the specific exceptions in verifyPlugin below.
        failureLevel = VerifyPluginTask.FailureLevel.ALL - setOf(
            VerifyPluginTask.FailureLevel.NOT_DYNAMIC,
            VerifyPluginTask.FailureLevel.EXPERIMENTAL_API_USAGES,
        )
        ides {
            // Use IDE distributions containing the October 2026 eMudhra security fixes.
            create(IntelliJPlatformType.PhpStorm, providers.gradleProperty("platformVersion").get())
            if (providers.gradleProperty("verifyIntellijIdea").map(String::toBoolean).getOrElse(false)) {
                val ideaLocalPath = providers.gradleProperty("ideaPlatformLocalPath")
                if (ideaLocalPath.isPresent) local(ideaLocalPath)
                else create(IntelliJPlatformType.IntellijIdea, providers.gradleProperty("platformVersion").get())
            }
            if (providers.gradleProperty("verifyWebStorm").map(String::toBoolean).getOrElse(false)) {
                create(IntelliJPlatformType.WebStorm, providers.gradleProperty("uiTestPlatformVersion").get())
                create(IntelliJPlatformType.WebStorm, "2026.3")
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
    named<VerifyPluginTask>("verifyPlugin") {
        maxHeapSize = "2g"
        systemProperty("intellij.plugin.verifier.concurrency.level", "1")

        doLast {
            val provider = "com.magento.idea.magento2plugin.mcp.MagentoBundledSkillsProvider"
            val skillsApi = setOf(
                "com.intellij.mcpserver.skills.BundledSkill",
                "com.intellij.mcpserver.skills.BundledSkillsProvider",
                "com.intellij.mcpserver.skills.AgentSkillTarget",
            )
            val externalReferenceHost = "Experimental API interface com.intellij.model.psi.PsiExternalReferenceHost is referenced in "
            val sdkPsiClasses = setOf("com.intellij.psi.xml.XmlAttribute", "com.intellij.psi.xml.XmlNamedReferenceHost",
                "com.intellij.lang.javascript.psi.JSProperty")
            val unexpected = verificationReportsDirectory.get().asFile.walkTopDown()
                .filter { it.name == "experimental-api-usages.txt" }
                .flatMap { it.readLines().asSequence() }
                .filter { it.isNotBlank() }
                .filterNot { warning ->
                    // The provider is optional and loaded only with JetBrains' bundled skills module.
                    warning.startsWith("Experimental API ") && provider in warning && skillsApi.any { it in warning } ||
                        // The stable SDK reports these references within its own PSI classes.
                        sdkPsiClasses.any { warning.startsWith("$externalReferenceHost$it.") }
                }
                .distinct()
                .toList()
            check(unexpected.isEmpty()) {
                "Unexpected experimental API usage:\n${unexpected.joinToString("\n")}"
            }
        }
    }

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
    // 2.20.0 also adds base-IDE Starter dependencies directly to the source set.
    // Keep only the WebStorm-matched dependencies inherited from this testing entry.
    uiTestImplementation.withDependencies {
        removeIf { dependency ->
            TestFrameworkType.Starter.coordinates.any {
                it.groupId == dependency.group && it.artifactId == dependency.name
            }
        }
    }

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
            includeTestsMatching("com.magento.idea.magento2plugin.mcp.MagentoSkillSetupMcpTest")
            includeTestsMatching("com.magento.idea.magento2plugin.mcp.MagentoExternalDiscoveryMcpTest")
            includeTestsMatching("com.magento.idea.magento2plugin.mcp.MagentoCompatibilityCommandsTest")
            if (providers.environmentVariable("MAGENTO_MCP_REAL_RELEASE_TEST").orNull != "true") {
                excludeTestsMatching("*.testHttpMcpWithActualMagentoReleases")
            }
        }
        testLogging { events("passed", "skipped", "failed") }
    }
}

// Development launcher for the same shared plugin in IntelliJ IDEA.
intellijPlatformTesting.runIde.register("runIntellijIdea") {
    type = IntelliJPlatformType.IntellijIdea
    version = providers.gradleProperty("platformVersion")
    providers.gradleProperty("ideaPlatformLocalPath").orNull?.let { localPath.set(file(it)) }
}

// Proves base registrations and shared intelligence load with optional languages disabled.
intellijPlatformTesting.testIde.register("coreTest") {
    type = IntelliJPlatformType.IntellijIdea
    version = providers.gradleProperty("platformVersion")
    providers.gradleProperty("corePlatformLocalPath").orElse(providers.gradleProperty("ideaPlatformLocalPath"))
        .orNull?.let { localPath.set(file(it)) }
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.JUnit5)
    plugins {
        disablePlugins(listOf("JavaScript", "com.jetbrains.php", "com.intellij.css", "com.intellij.lang.jsgraphql"))
    }
    task {
        val core = sourceSets.getByName("coreTest")
        testClassesDirs = core.output.classesDirs
        classpath += core.output
        // Compile against the full SDK, but deny optional language jars to the test parent loader.
        val optionalLanguagePath = Regex("(?i)/plugins/(javascript[^/]*|php|css[^/]*|jsgraphql)/")
        classpath = classpath.filter { !optionalLanguagePath.containsMatchIn(it.invariantSeparatorsPath) }
        useJUnitPlatform()
        systemProperty("intellij.platform.kernel.disable", "true")
        systemProperty("ide.fleet.launch", "false")
    }
}
