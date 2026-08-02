/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions

import com.magento.idea.magento2plugin.ui.assertGeneratedFile
import com.magento.idea.magento2plugin.ui.invokeProjectViewAction
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test

class NewDiXmlActionUiTest {
    @Test
    fun `creates di xml from module context`() = runWebStormUiTest(
        FILE_GENERATORS_FIXTURE,
        "app/code/Acme/Shipping/etc/di.xml",
    ) { projectPath ->
        invokeProjectViewAction(
            MODULE_XML_PATH,
            "MagentoCreateDiFile",
        )

        assertGeneratedFile(
            projectPath,
            "app/code/Acme/Shipping/etc/di.xml",
            "urn:magento:framework:ObjectManager/etc/config.xsd",
        )
    }
}
