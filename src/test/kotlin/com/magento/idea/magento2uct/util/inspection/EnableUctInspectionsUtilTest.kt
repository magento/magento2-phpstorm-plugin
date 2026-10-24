/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.util.inspection

import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.ex.ApplicationInspectionProfileManager
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolRegistrar
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EnableUctInspectionsUtilTest : BasePlatformTestCase() {
    fun testEnablesEveryUctInspectionAndLeavesOthers() {
        withInitializedInspections {
            val profile = InspectionProfileImpl("UCT test").apply { initInspectionTools(project) }
            val uct = uctInspections(profile)
            assertFalse(uct.any { enabled(profile, it) })
            val other = "CacheableFalseInDefaultLayoutInspection"
            profile.setToolEnabled(other, false, project)

            EnableUctInspectionsUtil.execute(profile, project)

            assertTrue(uct.filterNot { enabled(profile, it) }.toString(), uct.all { enabled(profile, it) })
            assertFalse(enabled(profile, other))
        }
    }

    fun testEnablesInProjectCopyOfSharedProfile() {
        withInitializedInspections {
            val shared = InspectionProfileImpl(
                "UCT shared",
                InspectionToolRegistrar.getInstance(),
                ApplicationInspectionProfileManager.getInstanceImpl()
            ).apply { initInspectionTools(project) }
            val applicationManager = ApplicationInspectionProfileManager.getInstanceImpl()
            val projectManager = ProjectInspectionProfileManager.getInstance(project)
            val previous = projectManager.currentProfile
            applicationManager.addProfile(shared)
            projectManager.useApplicationProfile(shared.name)
            try {
                assertSame(shared, projectManager.currentProfile)
                val uct = uctInspections(shared)

                EnableUctInspectionsUtil.execute(project)

                val current = projectManager.currentProfile
                assertNotSame(shared, current)
                assertTrue(current.isProjectLevel)
                assertTrue(uct.filterNot { enabled(current, it) }.toString(), uct.all { enabled(current, it) })
                assertFalse(uct.any { enabled(shared, it) })
            } finally {
                projectManager.currentProfile.takeIf { it.isProjectLevel }?.let { projectManager.deleteProfile(it) }
                projectManager.setCurrentProfile(previous)
                applicationManager.deleteProfile(shared)
            }
        }
    }

    private fun withInitializedInspections(test: () -> Unit) {
        val initInspections = InspectionProfileImpl.INIT_INSPECTIONS
        InspectionProfileImpl.INIT_INSPECTIONS = true
        try {
            test()
        } finally {
            InspectionProfileImpl.INIT_INSPECTIONS = initInspections
        }
    }

    private fun uctInspections(profile: InspectionProfileImpl): List<String> {
        val uct = profile.getInspectionTools(null).filter { it.groupPath.firstOrNull() == "UCT" }.map { it.shortName }
        assertTrue(uct.toString(), uct.containsAll(listOf("ImportingNonExistentClass", "ExtendingDeprecatedClass", "CalledNonApiMethod")))
        return uct
    }

    private fun enabled(profile: InspectionProfileImpl, shortName: String): Boolean =
        profile.isToolEnabled(HighlightDisplayKey.find(shortName), null)
}
