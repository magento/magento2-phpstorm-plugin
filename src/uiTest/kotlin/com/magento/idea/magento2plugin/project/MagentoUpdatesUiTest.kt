/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project

import com.intellij.driver.sdk.getNotifications
import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.waitFor
import com.intellij.driver.client.Remote
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.button
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.nio.file.Files
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Exercises the installed plugin, real HTTP, project detection, action and notification in a PHP-free IDE. */
class MagentoUpdatesUiTest {
    @Test
    fun `notifies about isolated critical patches without a version bump`() =
        checkDistribution("magento-community", "magento/product-community-edition", "2.4.8-p5", "Critical")

    @Test
    fun `checks Adobe Commerce instead of its community dependency`() =
        checkDistribution("magento-commerce", "magento/product-enterprise-edition", "2.4.8-p5", "Critical")

    @Test
    fun `only checks Mage OS when installed and preserves unknown security coverage`() =
        checkDistribution("mage-os", "mage-os/product-community-edition", "3.5.0", "Security coverage is unknown")

    @Test
    fun `detects source checkout but skips update requests in WebStorm`() =
        checkDistribution("magento-community", "magento/magento2ce", "2.4.8", "source checkout", sourceCheckout = true)

    @Test
    fun `shows recent releases in blue separately from applicable security patches`() =
        checkDistribution("magento-community", "magento/product-community-edition", "2.4.8-p5", "Critical", newerVersion = "2.4.9")

    @Test
    fun `skips releases and security advisories older than one month`() =
        checkDistribution("magento-community", "magento/product-community-edition", "2.4.8-p5", "past month", newerVersion = "2.4.9", oldFindings = true)

    @Test
    fun `handles an unavailable feed gracefully in WebStorm`() =
        checkDistribution("magento-community", "magento/product-community-edition", "2.4.8-p5", "unavailable", unavailableFeed = true)

    @Test
    fun `suppresses Composer configured patches and warns when the fix is removed`() =
        checkDistribution("magento-community", "magento/product-community-edition", "2.4.8-p5", "covered by installed fixes or local patch configuration",
            localPatchFiles = mapOf("composer.json" to """{"require":{"cweagans/composer-patches":"^2.0"},"extra":{
                "patches":{"magento/framework":{"Security fix":"https://example.test/TEST-PATCH.patch"}}}}"""))

    @Test
    fun `suppresses Cloud YAML patches and warns when the fix is removed`() =
        checkDistribution("magento-community", "magento/product-community-edition", "2.4.8-p5", "covered by installed fixes or local patch configuration",
            localPatchFiles = mapOf(".magento.env.yaml" to "stage: {build: {QUALITY_PATCHES: [TEST-PATCH]}}"))

    private fun checkDistribution(distribution: String, packageName: String, version: String, expected: String,
                                  sourceCheckout: Boolean = false, newerVersion: String? = null, oldFindings: Boolean = false,
                                  unavailableFeed: Boolean = false, localPatchFiles: Map<String, String> = emptyMap()) {
        val requests = CopyOnWriteArrayList<String>()
        val date = LocalDate.now(ZoneOffset.UTC).let { if (oldFindings) it.minusMonths(1).minusDays(1) else it.minusDays(1) }
        val extraRelease = newerVersion?.let { """, "$it":{"version":"$it","releaseDate":"$date"}""" } ?: ""
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/api/v1/") { exchange ->
            val path = exchange.requestURI.path
            requests.add(path)
            if (unavailableFeed) {
                exchange.sendResponseHeaders(404, -1)
                exchange.close()
                return@createContext
            }
            val body = when (path) {
                "/api/v1/$distribution/versions" -> """{"data":{"$version":{"version":"$version","releaseDate":"2026-05-12","isFutureVersion":false,"isEOLVersion":false,"isLatestVersion":true,"isSecureVersion":true}$extraRelease}}"""
                "/api/v1/$distribution/versions/$version/security" -> if (distribution == "mage-os") {
                    """{"data":{"distribution":"mage-os","version":"$version","status":"unknown","vulnerableTo":[],"fixedBy":[],"recommendedVersion":null}}"""
                } else {
                    """{"data":{"distribution":"$distribution","version":"$version","status":"vulnerable","vulnerableTo":[{"id":"APSB-TEST","severity":"critical","publishedDate":"$date","cves":["CVE-TEST-1"],"fixedIn":null,"isolatedPatches":[{"id":"TEST-PATCH","appliesTo":["$version"],"url":"https://example.test/patch"}]}],"fixedBy":[],"recommendedVersion":"$version"}}"""
                }
                else -> """{"error":"Unexpected distribution"}"""
            }
            val bytes = body.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(if (path.contains("/$distribution/")) 200 else 404, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            runWebStormUiTest("file-generators", prepareProject = { root ->
                val community = if (distribution == "magento-commerce") {
                    """,{"name":"magento/product-community-edition","version":"$version"}"""
                } else ""
                root.resolve("composer.lock").writeText("""{"packages":[{"name":"$packageName","version":"$version"}$community]}""")
                if (sourceCheckout) {
                    root.resolve("composer.lock").writeText("""{"packages":[]}""")
                    root.resolve("composer.json").writeText("""{"name":"$packageName","version":"$version"}""")
                    Files.createDirectories(root.resolve("app/code/Magento/Store"))
                    Files.createDirectories(root.resolve("lib/internal/Magento/Framework"))
                }
                localPatchFiles.forEach { (path, body) -> root.resolve(path).writeText(body) }
                Files.createDirectories(root.resolve(".idea"))
                root.resolve(".idea/workspace.xml").writeText("""
                    <project version="4">
                      <component name="MagentoUpdateSettings">
                        <option name="feedUrl" value="http://127.0.0.1:${server.address.port}/api/v1" />
                      </component>
                    </project>
                """.trimIndent())
            }) { root ->
                // Enabling support triggers automatic checks; unknown coverage is available on demand.
                if (distribution == "mage-os" || sourceCheckout || oldFindings || unavailableFeed || localPatchFiles.isNotEmpty()) invokeAction("Magento.CheckUpdates")
                waitFor("distribution-specific update notification", 2.minutes) {
                    getNotifications().any {
                        it.getGroupId() == "Magento Updates" && it.getContent().contains(expected) &&
                            (unavailableFeed || it.getContent().contains(version))
                    }
                }
                if (sourceCheckout) {
                    check(requests.isEmpty()) { "Source checkout must not fetch updates: $requests" }
                    return@runWebStormUiTest
                }
                if (unavailableFeed) {
                    val notification = getNotifications().last { it.getGroupId() == "Magento Updates" }
                    check(cast(notification.getType(), UpdateNotificationType::class).name() == "INFORMATION")
                    check(notification.getContent().contains("two minutes"))
                    check(requests.isNotEmpty())
                    return@runWebStormUiTest
                }
                check(requests.any { it.endsWith("/security") }) { "Security endpoint was not checked: $requests" }
                check(requests.all { it.startsWith("/api/v1/$distribution/") }) {
                    "Notifier queried another distribution: $requests"
                }
                if (localPatchFiles.isNotEmpty()) {
                    check(getNotifications().filter { it.getGroupId() == "Magento Updates" }.all {
                        !it.getContent().contains("APSB-TEST") &&
                            cast(it.getType(), UpdateNotificationType::class).name() == "INFORMATION"
                    }) { "A configured security patch must not produce a security alert" }
                    localPatchFiles.keys.forEach { Files.delete(root.resolve(it)) }
                    invokeAction("Magento.CheckUpdates")
                    waitFor("security warning after removing local patch configuration", 2.minutes) {
                        getNotifications().any {
                            it.getGroupId() == "Magento Updates" && it.getContent().contains("APSB-TEST") &&
                                cast(it.getType(), UpdateNotificationType::class).name() == "ERROR"
                        }
                    }
                    return@runWebStormUiTest
                }
                val content = getNotifications().last { it.getGroupId() == "Magento Updates" }.getContent()
                if (oldFindings) {
                    check(getNotifications().filter { it.getGroupId() == "Magento Updates" }.all {
                        !it.getContent().contains("APSB-TEST") && !it.getContent().contains("Newer release available") &&
                            cast(it.getType(), UpdateNotificationType::class).name() == "INFORMATION"
                    })
                } else if (distribution != "mage-os") {
                    check(content.contains("APSB-TEST"))
                    check(content.contains("No complete local patch coverage"))
                    val security = getNotifications().last { it.getGroupId() == "Magento Updates" && it.getContent().contains("APSB-TEST") }
                    check(cast(security.getType(), UpdateNotificationType::class).name() == "ERROR")
                    check(!security.getContent().contains("Newer release available"))
                    if (newerVersion != null) {
                        val release = getNotifications().last { it.getGroupId() == "Magento Updates" && it.getContent().contains("Newer release available") }
                        check(cast(release.getType(), UpdateNotificationType::class).name() == "INFORMATION")
                        check(!release.getContent().contains("APSB-TEST"))
                        check(release.getContent().contains(newerVersion))
                    }
                }
                if (distribution == "magento-community") {
                    val requestsBeforeManualCheck = requests.size
                    ideFrame {
                        openSettingsDialog()
                        settingsDialog {
                            searchTextField.text = "Magento"
                            waitFor("Magento settings", 30.seconds) {
                                settingsTree.collectExpandedPaths().any { it.path.last() == "Magento" }
                            }
                            settingsTree.clickRow { it == "Magento" }
                            content {
                                val checkButton = button("Check Magento updates").waitFound()
                                withContext(OnDispatcher.EDT) {
                                    driver.cast(checkButton.component, UpdateCheckButton::class).doClick()
                                }
                            }
                        }
                    }
                    waitFor("manual settings check bypasses the metadata cache", 30.seconds) {
                        requests.size >= requestsBeforeManualCheck + 2
                    }
                }
            }
        } finally {
            server.stop(0)
        }
    }
}

@Remote("javax.swing.JButton")
private interface UpdateCheckButton {
    fun doClick()
}

@Remote("com.intellij.notification.NotificationType")
private interface UpdateNotificationType {
    fun name(): String
}
