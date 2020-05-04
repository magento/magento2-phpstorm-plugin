/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;

public class PreferenceDiXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String module = "Foo_Bar";
    private static final String moduleDir = "src/app/code/Foo/Bar/";
    private static final String targetSimpleModelOneClassFqn = "Foo\\Bar\\Model\\SimpleModelOne";
    private static final String targetSimpleModelTwoClassFqn = "Foo\\Bar\\Model\\SimpleModelTwo";

    /**
     * Test preference DI XML file generation
     */
    public void testGeneratePreferenceDiXml()
    {
        String area = Package.Areas.base.toString();
        PsiFile preferenceDiXmlFile = addPreferenceDiXml(
                targetSimpleModelOneClassFqn,
                "Foo\\Bar\\Model\\Override\\SimpleModelOne",
                area,
                "Foo\\Bar\\Model\\Override"
        );

        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                preferenceDiXmlFile
        );
    }

    /**
     * Test preference DI XML file generation for adminhtml area
     */
    public void testGeneratePreferenceDiXmlForAdminhtmlArea()
    {
        String area = Package.Areas.adminhtml.toString();
        PsiFile preferenceDiXmlFile = addPreferenceDiXml(
                targetSimpleModelTwoClassFqn,
                "Foo\\Bar\\Model\\Override\\SimpleModelTwo",
                area,
                "Foo\\Bar\\Model\\Override"
        );

        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                preferenceDiXmlFile
        );
    }

    /**
     * Test the adding of two preferences to one DI XML file
     */
    public void testAddTwoPreferencesToOneDiXmlFile()
    {
        String area = Package.Areas.frontend.toString();
        addPreferenceDiXml(
                targetSimpleModelOneClassFqn,
                "Foo\\Bar\\Model\\Override\\SimpleModelOne",
                area,
                "Foo\\Bar\\Model\\Override"
        );
        PsiFile preferenceDiXmlFile = addPreferenceDiXml(
                targetSimpleModelTwoClassFqn,
                "Foo\\Bar\\Model\\Override\\SimpleModelTwo",
                area,
                "Foo\\Bar\\Model\\Override"
        );

        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                preferenceDiXmlFile
        );
    }

    /**
     * Add preference to XML file
     *
     * @param targetClassFnq Target class FQN
     * @param preferenceFqn Preference FQN
     * @param area Area
     * @param namespace Namespace
     * @return PsiFile
     */
    private PsiFile addPreferenceDiXml(
            String targetClassFnq,
            String preferenceFqn,
            String area,
            String namespace
    ) {
        Project project = myFixture.getProject();
        PhpClass targetClass = GetPhpClassByFQN.getInstance(project).execute(targetClassFnq);
        PreferenceDiXmFileData preferenceDiXmlFileData = new PreferenceDiXmFileData(
                module,
                targetClass,
                preferenceFqn,
                namespace,
                area
        );
        PreferenceDiXmlGenerator moduleXmlGenerator = new PreferenceDiXmlGenerator(preferenceDiXmlFileData, project);

        return moduleXmlGenerator.generate("test");
    }

    /**
     * Get expected directory based on provided area
     *
     * @param area Area name
     * @return String
     */
    private String getExpectedDirectory(String area)
    {
        if (area.equals(Package.Areas.base.toString())) {
            return moduleDir + Package.MODULE_BASE_AREA_DIR;
        }

        return moduleDir + Package.MODULE_BASE_AREA_DIR + File.separator + area;
    }
}
