plugins { alias(libs.plugins.kotlin) }

group = "com.magento.idea"
apply(from = "../../gradle/release-version.gradle.kts")
repositories { mavenCentral() }
kotlin { jvmToolchain(25) }

val shared = file("../../src/main")
kotlin.sourceSets.main {
    kotlin.srcDir(shared.resolve("kotlin"))
    kotlin.include("com/magento/idea/magento2plugin/hce/**")
    listOf("MagentoCliEnvironment", "MagentoMagerunNames", "MagentoDdevEnvironment", "MagentoDdevConfiguration", "MagentoDdevCommands").forEach {
        kotlin.include("com/magento/idea/magento2plugin/mcp/$it.kt")
    }
}
sourceSets.main {
    java.srcDir(shared.resolve("java"))
    java.include("com/magento/idea/magento2plugin/project/MagentoCliConfiguration.java")
}

// The core and its tests need no IDE or HCE distribution. Only this host adapter
// compiles against the pinned engine API and is included in the plugin archive.
val host = sourceSets.create("host")
val releaseVersion = version.toString()
tasks.named<org.gradle.language.jvm.tasks.ProcessResources>(host.processResourcesTaskName) {
    inputs.property("pluginVersion", releaseVersion)
    filesMatching("META-INF/plugin.xml") { expand(mapOf("pluginVersion" to releaseVersion)) }
}
kotlin.target.compilations.getByName("host").associateWith(kotlin.target.compilations.getByName("main"))
val hceRuntime = providers.gradleProperty("hceRuntime")
val engineJars = files(providers.provider {
    hceRuntime.orNull?.let { runtime -> fileTree(runtime) {
        include("lib/*.jar", "plugins/mcp/lib/**/*.jar", "plugins/xml/lib/**/*.jar", "plugins/json/lib/**/*.jar")
    } } ?: files()
})
configurations[host.implementationConfigurationName].extendsFrom(configurations.implementation.get())
host.compileClasspath += sourceSets.main.get().output
host.runtimeClasspath += sourceSets.main.get().output

kotlin.sourceSets.named("host") {
    kotlin.srcDir(shared.resolve("kotlin"))
    kotlin.include("com/magento/idea/magento2plugin/hce/**")
    listOf("MagentoModuleDependencyQueries", "MagentoXmlDiagnostics", "MagentoIndexKeys", "MagentoInspectionCommands", "MagentoModuleQueries", "MagentoDiQueries", "MagentoEventQueries",
        "MagentoViewQueries", "MagentoMcpSnapshots", "MagentoMcpSupport", "MagentoMcpSetupGuidance",
        "MagentoMcpReadActionSupport", "MagentoMcpToolsetSupport", "MagentoMethodPluginQueryProvider", "MagentoSkillCommands",
        "MagentoMagerunQueries", "MagentoMagerunSupport", "MagentoMagerunProcess", "MagentoCliToolQueries",
        "MagentoIntelligenceToolset").forEach {
        kotlin.include("com/magento/idea/magento2plugin/mcp/$it.kt")
    }
}
host.java.srcDir(shared.resolve("java"))
listOf("util/magento/ui/TemplatePathResolver", "reference/provider/util/GetModuleFileUtil",
    "reference/provider/util/GetModuleNameUtil", "util/magento/MagentoVfsUtil", "inspections/xml/CacheableFalseInDefaultLayoutInspection", "inspections/xml/ModuleDeclarationInModuleXmlInspection",
    "inspections/xml/fix/XmlRemoveCacheableAttributeQuickFix", "inspections/xml/fix/XmlModuleNameQuickFix",
    "inspections/util/GetEditableModuleNameByRootFileUtil", "bundles/InspectionBundle", "bundles/AbstractBundle",
    "magento/files/LayoutXml", "magento/files/ModuleXml", "project/MagentoSkillInstaller", "indexes/ModuleIndex", "indexes/LayoutIndex", "indexes/UIComponentIndex", "project/Settings",
    "magento/packages/Package", "magento/packages/File", "magento/files/ModuleFileInterface",
    "magento/files/ModuleDiXml", "magento/files/ModuleAclXml", "magento/files/ModuleMenuXml",
    "stubs/indexes/xml/ModuleXmlIndex", "stubs/indexes/xml/ThemeXmlIndex",
    "stubs/indexes/BlockNameIndex", "stubs/indexes/ContainerNameIndex",
    "stubs/indexes/NamedComponentIndex", "stubs/indexes/LayoutDataIndexer", "util/PhpNameUtil",
    "util/RegExUtil", "util/magento/IsFileInEditableModuleUtil", "util/magento/MagentoPathUrlUtil").forEach {
    host.java.include("com/magento/idea/magento2plugin/$it.java")
}

dependencies {
    implementation(libs.json)
    // Standalone module must supply YAML parsing, normally bundled by the IDE.
    implementation("org.snakeyaml:snakeyaml-engine:2.9")
    add(host.compileOnlyConfigurationName, engineJars)
    add(host.implementationConfigurationName, libs.kotlinx.coroutines.core)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
tasks.test { useJUnitPlatform() }
tasks.named("compileHostKotlin") {
    doFirst { check(hceRuntime.isPresent) { "Supply -PhceRuntime=/path/to/intellij-server for host compilation." } }
}
val hcePluginJar = tasks.register<Jar>("hcePluginJar") {
    dependsOn(host.classesTaskName)
    archiveClassifier = "hce"
    from(sourceSets.main.get().output)
    from(host.output)
    from(shared.resolve("resources")) { include("magento2/inspection.properties") }
    from("../../skills") { include("magento-inspect/**", "magento-magerun/**", "magento-scaffold/**"); into("skills") }
}
tasks.register<Zip>("packageHcePlugin") {
    dependsOn(hcePluginJar)
    archiveFileName = "magento-hce.zip"
    destinationDirectory = layout.buildDirectory.dir("distributions")
    from(hcePluginJar) { into("magento-hce/lib") }
    from(configurations.runtimeClasspath.map { jars -> jars.filter {
        it.name.startsWith("json-") || it.name.startsWith("snakeyaml-engine-")
    } }) { into("magento-hce/lib") }
}
