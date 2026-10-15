/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Path

class AppliedPatchTest {
    private val root = Path.of("/magento")
    private val diff = "--- a/example.php\n+++ b/example.php\n@@ -1,3 +1,3 @@\n context\n-unsafe\n+safe\n end\n"
    private fun status(patch: String, text: String?) = AppliedPatch.isApplied(patch, root, { text })

    @Test fun `checks complete post image while allowing CRLF and shifted lines`() {
        assertEquals(true, status(diff, "context\nsafe\nend\n"))
        assertEquals(true, status(diff, "unrelated\ncontext\nsafe\nend\n".replace("\n", "\r\n")))
        assertEquals(false, status(diff, "context\nunsafe\nend\n"))
        assertEquals(false, status(diff, "context\nsafe\ndifferent\n"))
        assertNull(status(diff, null))
    }

    @Test fun `partial multi hunk application does not count`() {
        val patch = diff + "@@ -5,3 +5,3 @@\n other\n-before\n+after\n tail\n"
        assertEquals(false, status(patch, "context\nsafe\nend\n\nother\nbefore\ntail\n"))
        assertEquals(true, status(patch, "context\nsafe\nend\n\nother\nafter\ntail\n"))
    }

    @Test fun `malformed truncated binary and empty diffs are unknown`() {
        for (patch in listOf("", "VULN-12345.patch", diff.substringBefore("+safe"),
            diff.replace("@@ -1,3 +1,3 @@", "@@ -1,4 +1,3 @@"), diff + "GIT binary patch\n")) {
            assertNull(patch, status(patch, "context\nsafe\nend\n"))
        }
    }

    @Test fun `untrusted target paths are not inspected`() {
        for (path in listOf("../../outside.php", "/outside.php", "\"quoted.php\"")) {
            assertNull(AppliedPatch.isApplied(diff.replace("b/example.php", path), root, { fail("Unexpected file read: $it"); null }))
        }
    }

    @Test fun `package relative targets use the Composer install directory`() {
        val packageRoot = root.resolve("vendor/magento/framework")
        assertEquals(true, AppliedPatch.isApplied(diff, root, { path ->
            if (path == packageRoot.resolve("example.php")) "context\nsafe\nend\n" else null
        }, packageRoot))
    }
}
