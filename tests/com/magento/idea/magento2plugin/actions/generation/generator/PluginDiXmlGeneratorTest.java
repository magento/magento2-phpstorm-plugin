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
    private static final String pluginTargetClassOneFnq = "Foo\\Bar\\Model\\PluginTargetClassOne";
    private static final String pluginTargetClassTwoFnq = "Foo\\Bar\\Model\\PluginTargetClassTwo";
    private static final String pluginClassOneFnq = "Foo\\Bar\\Plugin\\TestOnePlugin";
    private static final String pluginClassTwoFnq = "Foo\\Bar\\Plugin\\TestTwoPlugin";
    private static final String module = "Foo_Bar";
    private static final String moduleDir = "src/app/code/Foo/Bar/";

    public void testGeneratePluginDiXmlFileForBaseArea()
    {
        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        String area = Areas.base.toString();

        PsiFile diXml = addPluginDiXml(
                pluginTargetClassOneFnq,
                area,
                "10",
                "test_plugin_name_1",
                pluginClassOneFnq
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    public void testGeneratePluginDiXmlFileForAdminhtmlArea()
    {
        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        String area = Areas.adminhtml.toString();

        PsiFile diXml = addPluginDiXml(
                pluginTargetClassTwoFnq,
                area,
                "20",
                "test_plugin_name_2",
                pluginClassTwoFnq
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    public void testAddTwoPluginsToOneDiXml()
    {
        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        String area = Areas.frontend.toString();
        addPluginDiXml(
                pluginTargetClassOneFnq,
                area,
                "10",
                "test_plugin_name_1",
                pluginClassOneFnq
        );
        PsiFile diXml = addPluginDiXml(
                pluginTargetClassTwoFnq,
                area,
                "20",
                "test_plugin_name_2",
                pluginClassTwoFnq
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    public void testAddTwoPluginsToOneTargetClass()
    {
        String filePath = this.getFixturePath(ModuleDiXml.FILE_NAME);
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        String area = Areas.frontend.toString();
        addPluginDiXml(
                pluginTargetClassOneFnq,
                area,
                "10",
                "test_plugin_name_1",
                pluginClassOneFnq
        );
        PsiFile diXml = addPluginDiXml(
                pluginTargetClassOneFnq,
                area,
                "20",
                "test_plugin_name_2",
                pluginClassTwoFnq
        );

        assertGeneratedFileIsCorrect(
                expectedFile,
                getExpectedDirectory(area),
                diXml
        );
    }

    private PsiFile addPluginDiXml(
            String targetClassFnq,
            String area,
            String SortOrder,
            String pluginName,
            String pluginClassFnq
    ) {
        Project project = myFixture.getProject();
        PhpClass targetClass = GetPhpClassByFQN.getInstance(project).execute(targetClassFnq);
        PluginDiXmlData pluginDiXmlData = new PluginDiXmlData(
                area,
                module,
                targetClass,
                SortOrder,
                pluginName,
                pluginClassFnq
        );
        PluginDiXmlGenerator moduleXmlGenerator = new PluginDiXmlGenerator(pluginDiXmlData, project);

        return moduleXmlGenerator.generate("test");
    }

    private String getExpectedDirectory(String area)
    {
        if (area.equals(Areas.base.toString())) {
            return moduleDir + Package.moduleBaseAreaDir;
        }

        return moduleDir + Package.moduleBaseAreaDir + File.separator + area;
    }
}
