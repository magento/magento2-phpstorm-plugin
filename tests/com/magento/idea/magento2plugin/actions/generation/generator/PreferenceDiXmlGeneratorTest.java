/**
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

    private String getExpectedDirectory(String area)
    {
        if (area.equals(Package.Areas.base.toString())) {
            return moduleDir + Package.MODULE_BASE_AREA_DIR;
        }

        return moduleDir + Package.MODULE_BASE_AREA_DIR + File.separator + area;
    }
}
