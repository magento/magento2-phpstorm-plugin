/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;

public class GetMagentoModuleUtilTest extends BaseProjectTestCase {

    public void testGetByContextReturnsNullForRegisterWithoutParameters() {
        final PsiDirectory moduleDirectory = addRegistrationFile("ComponentRegistrar::register();");

        assertNull(GetMagentoModuleUtil.getByContext(moduleDirectory, getProject()));
    }

    public void testGetByContextReturnsNullForRegisterWithOneParameter() {
        final PsiDirectory moduleDirectory = addRegistrationFile(
                "ComponentRegistrar::register(ComponentRegistrar::MODULE);"
        );

        assertNull(GetMagentoModuleUtil.getByContext(moduleDirectory, getProject()));
    }

    public void testGetByContextSkipsMalformedRegisterCall() {
        final PsiDirectory moduleDirectory = addRegistrationFile(
                """
                ComponentRegistrar::register();
                ComponentRegistrar::register(
                    ComponentRegistrar::MODULE,
                    'Foo_Malformed',
                    __DIR__
                );
                """
        );

        final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                .getByContext(moduleDirectory, getProject());

        assertNotNull(moduleData);
        assertEquals("Foo_Malformed", moduleData.getName());
    }

    private PsiDirectory addRegistrationFile(final String registerCalls) {
        final PsiFile registrationFile = myFixture.addFileToProject(
                "app/code/Foo/Malformed/registration.php",
                "<?php\n\n"
                        + "use Magento\\Framework\\Component\\ComponentRegistrar;\n\n"
                        + registerCalls
                        + "\n"
        );

        return registrationFile.getContainingDirectory();
    }
}
