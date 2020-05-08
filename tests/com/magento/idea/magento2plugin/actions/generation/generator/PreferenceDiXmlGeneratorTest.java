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
    private static final String MODULE = "Foo_Bar";
    private static final String MODULE_DIR = "src/app/code/Foo/Bar/";
    private static final String TARGET_MODEL_ONE_CLASS_FQN = "Foo\\Bar\\Model\\SimpleModelOne";
    private static final String TARGET_MODEL_TWO_CLASS_FQN = "Foo\\Bar\\Model\\SimpleModelTwo";
    private static final String NAMESPACE = "Foo\\Bar\\Model\\Override";

    /**
     * Test preference DI XML file generation.
     */
    public void testGeneratePreferenceDiXml() {
        final String area = Package.Areas.base.toString();
        final PsiFile preferenceDiXmlFile = addPreferenceDiXml(
                TARGET_MODEL_ONE_CLASS_FQN,
                "Foo\\Bar\\Model\\Override\\SimpleModelOne",
                area
        );

        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                preferenceDiXmlFile
        );
    }

    /**
     * Test preference DI XML file generation for adminhtml area.
     */
    public void testGeneratePreferenceDiXmlForAdminhtmlArea() {
        final String area = Package.Areas.adminhtml.toString();
        final PsiFile preferenceDiXmlFile = addPreferenceDiXml(
                TARGET_MODEL_TWO_CLASS_FQN,
                "Foo\\Bar\\Model\\Override\\SimpleModelTwo",
                area
        );

        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                preferenceDiXmlFile
        );
    }

    /**
     * Test the adding of two preferences to one DI XML file.
     */
    public void testAddTwoPreferencesToOneDiXmlFile() {
        final String area = Package.Areas.frontend.toString();
        addPreferenceDiXml(
                TARGET_MODEL_ONE_CLASS_FQN,
                "Foo\\Bar\\Model\\Override\\SimpleModelOne",
                area
        );
        final PsiFile preferenceDiXmlFile = addPreferenceDiXml(
                TARGET_MODEL_TWO_CLASS_FQN,
                "Foo\\Bar\\Model\\Override\\SimpleModelTwo",
                area
        );

        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                preferenceDiXmlFile
        );
    }

    /**
     * Add preference to XML file.
     *
     * @param targetClassFnq Target class FQN
     * @param preferenceFqn Preference FQN
     * @param area Area
     * @return PsiFile
     */
    private PsiFile addPreferenceDiXml(
            final String targetClassFnq,
            final String preferenceFqn,
            final String area
    ) {
        final Project project = myFixture.getProject();
        final PhpClass targetClass = GetPhpClassByFQN.getInstance(project).execute(targetClassFnq);
        final PreferenceDiXmFileData preferenceDiXmlFileData = new PreferenceDiXmFileData(
                MODULE,
                targetClass,
                preferenceFqn,
                NAMESPACE,
                area
        );
        final PreferenceDiXmlGenerator moduleXmlGenerator = new PreferenceDiXmlGenerator(
                preferenceDiXmlFileData,
                project
        );

        return moduleXmlGenerator.generate("test");
    }

    /**
     * Get expected directory based on provided area.
     *
     * @param area Area name
     * @return String
     */
    private String getExpectedDirectory(final String area) {
        String expectedDirectory;

        if (area.equals(Package.Areas.base.toString())) {
            expectedDirectory = MODULE_DIR + Package.MODULE_BASE_AREA_DIR;
        } else {
            expectedDirectory = MODULE_DIR + Package.MODULE_BASE_AREA_DIR + File.separator + area;
        }

        return expectedDirectory;
    }
}
