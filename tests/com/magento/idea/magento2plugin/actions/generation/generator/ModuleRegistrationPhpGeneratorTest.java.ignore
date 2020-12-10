/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleRegistrationPhpData;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;

public class ModuleRegistrationPhpGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateRegistrationPhpFile() {
        String filePath = this.getFixturePath(RegistrationPhp.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        Project project = myFixture.getProject();
        PsiDirectory projectDir = getProjectDirectory();
        ModuleRegistrationPhpData moduleRegistrationPhpData = new ModuleRegistrationPhpData(
                "Foo",
                "Bar",
                projectDir,
                true
        );
        ModuleRegistrationPhpGenerator moduleRegistrationPhpGenerator = new ModuleRegistrationPhpGenerator(
                moduleRegistrationPhpData,
                project
        );
        PsiFile registrationPhp = moduleRegistrationPhpGenerator.generate("test");
        assertGeneratedFileIsCorrect(
                expectedFile,
                projectDir.getVirtualFile().getPath() + "/Foo/Bar",
                registrationPhp
        );
    }

    public void testGenerateRegistrationPhpFileInRoot()
    {
        String filePath = this.getFixturePath(RegistrationPhp.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        Project project = myFixture.getProject();
        PsiDirectory projectDir = getProjectDirectory();
        ModuleRegistrationPhpData moduleRegistrationPhpData = new ModuleRegistrationPhpData(
                "Foo",
                "Bar",
                projectDir,
                false
        );
        ModuleRegistrationPhpGenerator moduleRegistrationPhpGenerator = new ModuleRegistrationPhpGenerator(
                moduleRegistrationPhpData,
                project
        );
        PsiFile registrationPhp = moduleRegistrationPhpGenerator.generate("test");
        assertGeneratedFileIsCorrect(
                expectedFile,
                projectDir.getVirtualFile().getPath(),
                registrationPhp
        );
    }
}
