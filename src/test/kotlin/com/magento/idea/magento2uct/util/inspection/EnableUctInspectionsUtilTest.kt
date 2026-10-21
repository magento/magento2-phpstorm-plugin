/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.util.inspection

import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EnableUctInspectionsUtilTest : BasePlatformTestCase() {
    fun testEnablesEveryUctInspectionAndLeavesOthers() {
        val initInspections = InspectionProfileImpl.INIT_INSPECTIONS
        InspectionProfileImpl.INIT_INSPECTIONS = true
        try {
            verifyEnabling()
        } finally {
            InspectionProfileImpl.INIT_INSPECTIONS = initInspections
        }
    }

    private fun verifyEnabling() {
        val profile = InspectionProfileImpl("UCT test").apply { initInspectionTools(project) }
        val uct = profile.getInspectionTools(null).filter { it.groupPath.firstOrNull() == "UCT" }.map { it.shortName }
        assertTrue(uct.toString(), uct.containsAll(listOf("ImportingNonExistentClass", "ExtendingDeprecatedClass", "CalledNonApiMethod")))
        assertFalse(uct.any { enabled(profile, it) })
        val other = "CacheableFalseInDefaultLayoutInspection"
        profile.setToolEnabled(other, false, project)

        EnableUctInspectionsUtil.execute(profile, project)

        assertTrue(uct.filterNot { enabled(profile, it) }.toString(), uct.all { enabled(profile, it) })
        assertFalse(enabled(profile, other))
    }

    private fun enabled(profile: InspectionProfileImpl, shortName: String): Boolean =
        profile.isToolEnabled(HighlightDisplayKey.find(shortName), null)
}
