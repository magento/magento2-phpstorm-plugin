package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;

public class ModuleXmlGeneratorHaveSetupVersionTest extends BaseGeneratorTestCase {

    /**
     * Test checks whether module.xml is generated correctly.
     */
    public void testGenerateModuleFile() {
        final String filePath = this.getFixturePath(ModuleXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiDirectory projectDir = getProjectDirectory();

        final Project project = myFixture.getProject();
        final ModuleXmlData moduleXmlData = new ModuleXmlData(
                "Test",
                "Module1",
                "1.0.0",
                projectDir,
                true
        );
        final ModuleXmlGenerator moduleXmlGenerator = new ModuleXmlGenerator(
                moduleXmlData,
                project
        );
        final PsiFile moduleXml = moduleXmlGenerator.generate("test");

        assertGeneratedFileIsCorrect(
                expectedFile,
                projectDir.getVirtualFile().getPath()
                        + "/Test/Module1" + File.separator + Package.moduleBaseAreaDir,
                moduleXml
        );
    }
}
