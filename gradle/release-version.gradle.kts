import java.util.Properties

val baseVersion = providers.gradleProperty("pluginVersion").orElse(
    providers.fileContents(layout.projectDirectory.file("../../gradle.properties")).asText.map { text ->
        Properties().apply { load(text.reader()) }.getProperty("pluginVersion")
    }
).get()
version = when {
    !providers.environmentVariable("GITHUB_RELEASE_PRERELEASE").orNull.toBoolean() -> baseVersion
    providers.environmentVariable("GITHUB_EVENT_NAME").orNull == "workflow_dispatch" -> {
        val tag = providers.environmentVariable("RELEASE_TAG").orNull
            ?: throw GradleException("RELEASE_TAG is required when staging a paired alpha.")
        require(tag.matches(Regex("${Regex.escape(baseVersion)}-alpha(-[0-9]+)?"))) {
            "Expected an alpha release tag for $baseVersion, got '$tag'."
        }
        tag
    }
    else -> {
        val releaseId = providers.environmentVariable("GITHUB_RELEASE_ID").orNull
            ?: throw GradleException("GITHUB_RELEASE_ID is required when publishing a desktop prerelease.")
        "$baseVersion-alpha.$releaseId"
    }
}
