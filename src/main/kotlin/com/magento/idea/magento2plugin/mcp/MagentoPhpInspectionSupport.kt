/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass

/** PHP PSI stays behind the optional inspection provider. */
internal object MagentoPhpInspectionSupport {
    fun resolveTargetClasses(phpIndex: PhpIndex, className: String): List<PhpClass> {
        val classes = phpIndex.getClassesByFQN(className).toMutableList()
        classes += phpIndex.getInterfacesByFQN(className)
        return classes.distinctBy { it.fqn }
    }

    fun collectClassHierarchy(phpClass: PhpClass, fqns: MutableSet<String>) {
        fqns += phpClass.presentableFQN
        for (parent in phpClass.supers) {
            val parentFqn = parent.fqn.removePrefix("\\")
            if (fqns.add(parentFqn)) {
                collectClassHierarchy(parent, fqns)
            }
        }
    }

    data class PluginMethodMatch(
        val targetFqn: String,
        val data: PluginDeclarationRecord,
        val pluginClass: PhpClass,
        val method: Method,
        val kind: String
    ) {
        val sortOrder: Int
            get() = data.sortOrder

        val scope: String
            get() = data.scope
    }
}
