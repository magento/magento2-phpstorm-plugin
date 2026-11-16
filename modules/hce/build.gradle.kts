plugins { alias(libs.plugins.kotlin) }

group = "com.magento.idea"
version = "0.1.0"
repositories { mavenCentral() }
kotlin { jvmToolchain(25) }

val shared = file("../../src/main")
kotlin.sourceSets.main {
    kotlin.srcDir(shared.resolve("kotlin"))
    kotlin.include("com/magento/idea/magento2plugin/hce/**")
    listOf("MagentoCliEnvironment", "MagentoMagerunNames", "MagentoDdevEnvironment", "MagentoDdevConfiguration").forEach {
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
val hceRuntime = providers.gradleProperty("hceRuntime")
val engineJars = files(providers.provider {
    hceRuntime.orNull?.let { runtime -> fileTree(runtime) {
        include("lib/*.jar", "plugins/mcp/lib/**/*.jar")
    } } ?: files()
})
configurations[host.implementationConfigurationName].extendsFrom(configurations.implementation.get())
host.compileClasspath += sourceSets.main.get().output
host.runtimeClasspath += sourceSets.main.get().output

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
