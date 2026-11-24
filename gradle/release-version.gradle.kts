import java.util.Properties

val baseVersion = providers.gradleProperty("pluginVersion").orElse(
    providers.fileContents(layout.projectDirectory.file("../../gradle.properties")).asText.map { text ->
        Properties().apply { load(text.reader()) }.getProperty("pluginVersion")
    }
).get()
version = if (providers.environmentVariable("GITHUB_RELEASE_PRERELEASE").orNull.toBoolean()) {
    val tag = providers.environmentVariable("RELEASE_TAG").orNull
        ?: throw GradleException("RELEASE_TAG is required when publishing a GitHub prerelease.")
    require(tag.matches(Regex("${Regex.escape(baseVersion)}-alpha(-[0-9]+)?"))) {
        "Expected an alpha release tag for $baseVersion, got '$tag'."
    }
    tag
} else {
    baseVersion
}
