/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;

public class PluginDiXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String PLUGIN_TARGET_CLASS_ONE_FNQ
            = "Foo\\Bar\\Model\\PluginTargetClassOne";
    private static final String PLUGIN_TARGET_CLASS_TWO_FNQ
            = "Foo\\Bar\\Model\\PluginTargetClassTwo";
    private static final String PLUGIN_CLASS_ONE_FNQ = "Foo\\Bar\\Plugin\\TestOnePlugin";
    private static final String PLUGIN_CLASS_TWO_FNQ = "Foo\\Bar\\Plugin\\TestTwoPlugin";
    private static final String MODULE = "Foo_Bar";
    private static final String MODULE_DIR = "src/app/code/Foo/Bar/";
    private static final String TEST_PLUGIN_NAME = "test_plugin_name_1";

    /**
     * Test checks whether di.xml is generated correctly for the base area
     */
    public void testGeneratePluginDiXmlFileWithoutSortOrder() {
        final PsiFile expectedFile = myFixture.configureByFile(
                this.getFixturePath(ModuleDiXml.FILE_NAME)
        );
        final String area = Areas.base.toString();

        final PsiFile diXml = addPluginDiXml(
                PLUGIN_TARGET_CLASS_ONE_FNQ,
                area,
                "",
                TEST_PLUGIN_NAME,
                PLUGIN_CLASS_ONE_FNQ
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    /**
     * Test checks whether di.xml is generated correctly for the base area
     */
    public void testGeneratePluginDiXmlFileForBaseArea() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final String area = Areas.base.toString();

        final PsiFile diXml = addPluginDiXml(
                PLUGIN_TARGET_CLASS_ONE_FNQ,
                area,
                "10",
                TEST_PLUGIN_NAME,
                PLUGIN_CLASS_ONE_FNQ
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    /**
     * Test checks whether di.xml is generated correctly for the adminhtml area
     */
    public void testGeneratePluginDiXmlFileForAdminhtmlArea() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final String area = Areas.adminhtml.toString();

        final PsiFile diXml = addPluginDiXml(
                PLUGIN_TARGET_CLASS_TWO_FNQ,
                area,
                "20",
                "test_plugin_name_2",
                PLUGIN_CLASS_TWO_FNQ
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    /**
     * Test checks whether 2 di.xml is generated correctly
     */
    public void testAddTwoPluginsToOneDiXml() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final String area = Areas.frontend.toString();
        addPluginDiXml(
                PLUGIN_TARGET_CLASS_ONE_FNQ,
                area,
                "10",
                TEST_PLUGIN_NAME,
                PLUGIN_CLASS_ONE_FNQ
        );
        final PsiFile diXml = addPluginDiXml(
                PLUGIN_TARGET_CLASS_TWO_FNQ,
                area,
                "20",
                "test_plugin_name_2",
                PLUGIN_CLASS_TWO_FNQ
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    /**
     * Test checks whether 2 di.xml is generated correctly for one target clas
     */
    public void testAddTwoPluginsToOneTargetClass() {
        final String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final String area = Areas.frontend.toString();
        addPluginDiXml(
                PLUGIN_TARGET_CLASS_ONE_FNQ,
                area,
                "10",
                TEST_PLUGIN_NAME,
                PLUGIN_CLASS_ONE_FNQ
        );
        final PsiFile diXml = addPluginDiXml(
                PLUGIN_TARGET_CLASS_ONE_FNQ,
                area,
                "20",
                "test_plugin_name_2",
                PLUGIN_CLASS_TWO_FNQ
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    private PsiFile addPluginDiXml(
            final String targetClassFnq,
            final String area,
            final String sortOrder,
            final String pluginName,
            final String pluginClassFnq
    ) {
        final Project project = myFixture.getProject();
        final PhpClass targetClass = GetPhpClassByFQN.getInstance(project).execute(targetClassFnq);
        final PluginDiXmlData pluginDiXmlData = new PluginDiXmlData(
                area,
                MODULE,
                targetClass,
                sortOrder,
                pluginName,
                pluginClassFnq
        );
        final PluginDiXmlGenerator moduleXmlGenerator = new PluginDiXmlGenerator(
                pluginDiXmlData,
                project
        );

        return moduleXmlGenerator.generate("test");
    }

    private String getExpectedDirectory(final String area) {
        if (area.equals(Areas.base.toString())) {
            return MODULE_DIR + Package.moduleBaseAreaDir;
        }

        return MODULE_DIR + Package.moduleBaseAreaDir + File.separator + area;
    }
}
