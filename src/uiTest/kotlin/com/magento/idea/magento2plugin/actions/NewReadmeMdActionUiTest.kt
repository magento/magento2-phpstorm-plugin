/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions

import com.magento.idea.magento2plugin.ui.assertGeneratedFile
import com.magento.idea.magento2plugin.ui.invokeProjectViewAction
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test

class NewReadmeMdActionUiTest {
    @Test
    fun `creates module readme from module xml context`() =
        runWebStormUiTest(
            FILE_GENERATORS_FIXTURE,
            "app/code/Acme/Shipping/README.md",
        ) { projectPath ->
            invokeProjectViewAction(
                MODULE_PATH,
                "MagentoCreateReadmeFile",
            )

            assertGeneratedFile(
                projectPath,
                "app/code/Acme/Shipping/README.md",
                "# Acme_Shipping module",
            )
        }
}
