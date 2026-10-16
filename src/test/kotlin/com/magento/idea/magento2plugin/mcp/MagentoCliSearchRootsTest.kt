package com.magento.idea.magento2plugin.mcp

import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MagentoCliSearchRootsTest {
    @Test
    fun testRecognizesStandardComposeFilenames() = withFixture { directory ->
        for (name in listOf("compose.yaml", "compose.yml", "docker-compose.yaml", "docker-compose.yml")) {
            val projectRoot = Files.createDirectories(directory.resolve(name).resolve("project"))
            Files.createDirectories(projectRoot.resolve("src"))
            Files.writeString(projectRoot.resolve(name), "services: {}\n")

            assertEquals(listOf(".", "src"), MagentoCliToolQueries.buildSearchRoots(projectRoot, "src"))
        }
    }

    @Test
    fun testChoosesNearestDockerDirectoryInsideProject() = withFixture { projectRoot ->
        Files.createDirectories(projectRoot.resolve("apps/shop/src"))
        Files.writeString(projectRoot.resolve("compose.yaml"), "services: {}\n")
        Files.writeString(projectRoot.resolve("apps/shop/docker-compose.yml"), "services: {}\n")

        assertEquals(
            listOf("apps/shop", "apps/shop/src"),
            MagentoCliToolQueries.buildSearchRoots(projectRoot, "apps/shop/src")
        )
    }

    @Test
    fun testDoesNotSearchAboveProjectBoundary() = withFixture { directory ->
        Files.writeString(directory.resolve("compose.yaml"), "services: {}\n")
        val projectRoot = Files.createDirectories(directory.resolve("project"))

        assertEquals(listOf("."), MagentoCliToolQueries.buildSearchRoots(projectRoot, null))
        Files.createDirectories(projectRoot.resolve("src"))
        assertEquals(listOf("src"), MagentoCliToolQueries.buildSearchRoots(projectRoot, "src"))
    }

    @Test
    fun testDoesNotUseComposeConfigurationOutsideProjectThroughSymlink() = withFixture { directory ->
        val externalCompose = Files.writeString(directory.resolve("compose.yaml"), "services: {}\n")
        val projectRoot = Files.createDirectories(directory.resolve("project"))
        Files.createDirectories(projectRoot.resolve("src"))
        Files.createSymbolicLink(projectRoot.resolve("compose.yaml"), externalCompose)

        assertEquals(listOf("src"), MagentoCliToolQueries.buildSearchRoots(projectRoot, "src"))
    }

    @Test
    fun testRejectsEscapingMagentoDirectories() = withFixture { directory ->
        val projectRoot = Files.createDirectories(directory.resolve("project"))
        val outside = Files.createDirectories(directory.resolve("outside"))
        Files.createSymbolicLink(projectRoot.resolve("src"), outside)

        assertTrue(MagentoCliToolQueries.buildSearchRoots(projectRoot, "src").isEmpty())
        assertTrue(MagentoCliToolQueries.buildSearchRoots(projectRoot, "../outside").isEmpty())
    }

    @Test
    fun testDoesNotDuplicateMagentoRootContainingDockerConfiguration() = withFixture { projectRoot ->
        val magentoRoot = Files.createDirectories(projectRoot.resolve("src"))
        Files.writeString(magentoRoot.resolve("compose.yml"), "services: {}\n")
        Files.writeString(projectRoot.resolve("compose.yaml"), "services: {}\n")

        assertEquals(listOf("src"), MagentoCliToolQueries.buildSearchRoots(projectRoot, "src"))
    }

    private fun withFixture(test: (Path) -> Unit) {
        val directory = Files.createTempDirectory("magento-cli-roots")
        try {
            test(directory)
        } finally {
            Files.walk(directory).use { paths ->
                paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
            }
        }
    }
}
