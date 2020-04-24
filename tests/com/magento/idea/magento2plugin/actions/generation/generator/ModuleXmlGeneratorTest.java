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
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;

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
        ModuleXmlGenerator moduleXmlGenerator = new ModuleXmlGenerator(moduleXmlData, project);
        PsiFile moduleXml = moduleXmlGenerator.generate("test");

        assertGeneratedFileIsCorrect(
            expectedFile,
            projectDir.getVirtualFile().getPath() +
                "/Test/Module" + File.separator + Package.MODULE_BASE_AREA_DIR,
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
        ModuleXmlGenerator moduleXmlGenerator = new ModuleXmlGenerator(moduleXmlData, project);
        PsiFile moduleXml = moduleXmlGenerator.generate("test");

        assertGeneratedFileIsCorrect(
            expectedFile,
            projectDir.getVirtualFile().getPath() + File.separator + Package.MODULE_BASE_AREA_DIR,
            moduleXml
        );
    }
}
