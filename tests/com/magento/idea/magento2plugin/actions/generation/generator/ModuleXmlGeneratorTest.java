/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;

public class ModuleXmlGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateModuleFile() {
        String filePath = this.getFixturePath(ModuleXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        PsiDirectory projectDir = getProjectDirectory();

        Project project = myFixture.getProject();
        ModuleXmlData moduleXmlData = new ModuleXmlData(
            "Test",
            "Module",
            projectDir,
            true
        );
        ModuleXmlGenerator cronjobClassGenerator = new ModuleXmlGenerator(moduleXmlData, project);
        PsiFile moduleXml = cronjobClassGenerator.generate("test");

        assertGeneratedFileIsCorrect(
            expectedFile,
            projectDir.getVirtualFile().getPath() + "/Test/Module/etc",
            moduleXml
        );
    }

    public void testGenerateFileInRoot() {
        String filePath = this.getFixturePath(ModuleXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        PsiDirectory projectDir = getProjectDirectory();

        Project project = myFixture.getProject();
        ModuleXmlData moduleXmlData = new ModuleXmlData(
            "Test",
            "Module",
            projectDir,
            false
        );
        ModuleXmlGenerator cronjobClassGenerator = new ModuleXmlGenerator(moduleXmlData, project);
        PsiFile moduleXml = cronjobClassGenerator.generate("test");

        assertGeneratedFileIsCorrect(
            expectedFile,
            projectDir.getVirtualFile().getPath() + "/etc",
            moduleXml
        );
    }
}
