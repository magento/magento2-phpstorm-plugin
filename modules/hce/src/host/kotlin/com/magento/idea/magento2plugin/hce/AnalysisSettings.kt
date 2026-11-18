/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.hce

import com.magento.idea.magento2plugin.project.Settings

/** Keep static indexes populated in host indexing contexts. Tool access uses durable project settings. */
class AnalysisSettings : Settings() {
    init { pluginEnabled = true }
}
