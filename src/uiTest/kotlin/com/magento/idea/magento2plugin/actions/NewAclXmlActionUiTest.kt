/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions

import com.magento.idea.magento2plugin.ui.assertGeneratedFile
import com.magento.idea.magento2plugin.ui.invokeProjectViewAction
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test

class NewAclXmlActionUiTest {
    @Test
    fun `creates acl xml from module context`() = runWebStormUiTest(
        FILE_GENERATORS_FIXTURE,
        "app/code/Acme/Shipping/etc/acl.xml",
    ) { projectPath ->
        invokeProjectViewAction(
            MODULE_XML_PATH,
            "MagentoCreateAclFile",
        )

        assertGeneratedFile(
            projectPath,
            "app/code/Acme/Shipping/etc/acl.xml",
            "urn:magento:framework:Acl/etc/acl.xsd",
        )
    }
}
