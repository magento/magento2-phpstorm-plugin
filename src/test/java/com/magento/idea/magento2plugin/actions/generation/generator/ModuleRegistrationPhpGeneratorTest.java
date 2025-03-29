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

    /**
     * Test for generation of registration.php file.
     */
    public void testGenerateRegistrationPhpFile() {
        final String filePath = this.getFixturePath(RegistrationPhp.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final PsiDirectory projectDir = getProjectDirectory();
        final ModuleRegistrationPhpData moduleRegistrationPhpData = new ModuleRegistrationPhpData(
                "Foo",
                "Bar",
                projectDir,
                true
        );
        final ModuleRegistrationPhpGenerator moduleRegistrationPhpGenerator
                = new ModuleRegistrationPhpGenerator(
                    moduleRegistrationPhpData,
                    project
        );
        final PsiFile registrationPhp = moduleRegistrationPhpGenerator.generate("test");
        assertGeneratedFileIsCorrect(
                expectedFile,
                projectDir.getVirtualFile().getPath() + "/Foo/Bar",
                registrationPhp
        );
    }

    /**
     * Test for generation of registration.php file for a module project.
     */
    public void testGenerateRegistrationPhpFileInRoot() {
        final String filePath = this.getFixturePath(RegistrationPhp.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final PsiDirectory projectDir = getProjectDirectory();
        final ModuleRegistrationPhpData moduleRegistrationPhpData = new ModuleRegistrationPhpData(
                "Foo",
                "Bar",
                projectDir,
                false
        );
        final ModuleRegistrationPhpGenerator moduleRegistrationPhpGenerator
                = new ModuleRegistrationPhpGenerator(
                    moduleRegistrationPhpData,
                    project
        );
        final PsiFile registrationPhp = moduleRegistrationPhpGenerator.generate("test");
        assertGeneratedFileIsCorrect(
                expectedFile,
                projectDir.getVirtualFile().getPath(),
                registrationPhp
        );
    }
}
