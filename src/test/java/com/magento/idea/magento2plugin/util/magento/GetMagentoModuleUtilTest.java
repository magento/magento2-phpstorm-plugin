/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;

public class GetMagentoModuleUtilTest extends BaseProjectTestCase {

    public void testGetByContextDoesNotUseRegistrationPhp() {
        final PsiFile registrationFile = myFixture.addFileToProject(
                "app/code/Foo/WithoutModuleXml/registration.php",
                """
                <?php
                ComponentRegistrar::register(
                    ComponentRegistrar::MODULE,
                    'Foo_WithoutModuleXml',
                    __DIR__
                );
                """
        );

        assertNull(GetMagentoModuleUtil.getByContext(
                registrationFile.getContainingDirectory(),
                getProject()
        ));
    }

    public void testGetByContextReturnsNullForModuleXmlWithoutModuleName() {
        final PsiFile moduleFile = myFixture.addFileToProject(
                "app/code/Foo/WithoutName/etc/module.xml",
                "<config><module/></config>"
        );
        final PsiDirectory moduleDirectory = moduleFile.getContainingDirectory()
                .getParentDirectory();

        assertNull(GetMagentoModuleUtil.getByContext(moduleDirectory, getProject()));
    }

    public void testGetByContextResolvesModuleFromModuleXml() {
        final PsiFile moduleFile = myFixture.addFileToProject(
                "app/code/Foo/FromXml/etc/module.xml",
                "<config><module name=\"Foo_FromXml\"/></config>"
        );
        final PsiFile nestedFile = myFixture.addFileToProject(
                "app/code/Foo/FromXml/view/frontend/layout/example.xml",
                "<page/>"
        );

        final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                .getByContext(nestedFile.getContainingDirectory(), getProject());

        assertNotNull(moduleData);
        assertEquals("Foo_FromXml", moduleData.getName());
        assertEquals(moduleFile.getContainingDirectory(), moduleData.getConfigDir());
        assertEquals(
                nestedFile.getContainingDirectory().getParentDirectory().getParentDirectory(),
                moduleData.getViewDir()
        );
    }
}
