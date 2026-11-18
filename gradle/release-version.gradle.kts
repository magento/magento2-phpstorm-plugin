import java.util.Properties

val baseVersion = providers.gradleProperty("pluginVersion").orElse(
    providers.fileContents(layout.projectDirectory.file("../../gradle.properties")).asText.map { text ->
        Properties().apply { load(text.reader()) }.getProperty("pluginVersion")
    }
).get()
version = if (providers.environmentVariable("GITHUB_RELEASE_PRERELEASE").orNull.toBoolean()) {
    val releaseId = providers.environmentVariable("GITHUB_RELEASE_ID").orNull
        ?: throw GradleException("GITHUB_RELEASE_ID is required when publishing a GitHub prerelease.")
    "$baseVersion-alpha.$releaseId"
} else {
    baseVersion
}
