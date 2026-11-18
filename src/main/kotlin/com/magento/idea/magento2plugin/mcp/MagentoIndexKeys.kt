/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.intellij.util.indexing.ID

/** Host adaptation for key enumeration only; index data and query semantics remain shared. */
internal fun interface MagentoIndexKeys {
    fun keys(id: ID<String, *>, project: Project): Collection<String>
}
