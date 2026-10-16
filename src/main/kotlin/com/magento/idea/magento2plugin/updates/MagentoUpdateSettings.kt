/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.updates

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import java.net.URI

@Service(Service.Level.PROJECT)
@State(name = "MagentoUpdateSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class MagentoUpdateSettings : PersistentStateComponent<MagentoUpdateSettings.Options> {
    class Options {
        var enabled: Boolean = true
        var feedUrl: String = DEFAULT_FEED_URL
        var lastNotifiedFingerprints: MutableMap<String, String> = linkedMapOf()
        var snoozedFingerprints: MutableMap<String, String> = linkedMapOf()
        var snoozedUntil: MutableMap<String, Long> = linkedMapOf()
    }

    private var options = Options()
    override fun getState(): Options = options
    override fun loadState(state: Options) { options = state }

    companion object {
        const val DEFAULT_FEED_URL = "https://magento.watch/api/v1"
        @JvmStatic fun normalizeFeedUrl(value: String): String = value.trim().trimEnd('/').ifBlank { DEFAULT_FEED_URL }
        @JvmStatic fun isValidFeedUrl(value: String): Boolean = try {
            val uri = URI(normalizeFeedUrl(value))
            val local = uri.host in setOf("localhost", "127.0.0.1", "[::1]")
            !uri.host.isNullOrBlank() && uri.rawUserInfo == null && uri.rawFragment == null && uri.rawQuery == null &&
                (uri.scheme == "https" || (uri.scheme == "http" && local))
        } catch (_: Exception) { false }
    }
}
