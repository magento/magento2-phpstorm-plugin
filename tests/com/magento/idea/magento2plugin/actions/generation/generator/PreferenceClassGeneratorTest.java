/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceFileData;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;

public class PreferenceClassGeneratorTest extends BaseGeneratorTestCase {
    private static final String module = "Foo_Bar";
    private static final String targetSimpleModelOneClassFqn = "Foo\\Bar\\Model\\SimpleModelOne";
    private static final String targetSimpleModelTwoClassFqn = "Foo\\Bar\\Model\\SimpleModelTwo";

    /**
     * Test preference class file generation
     */
    public void testGeneratePreferenceClassFile() {
        PsiFile preferenceClassFile = createPreferenceClassFile(
                targetSimpleModelOneClassFqn,
                "Model/Override",
                "SimpleModelOneOverride",
                "Foo\\Bar\\Model\\Override\\SimpleModelOneOverride",
                "Foo\\Bar\\Model\\Override",
                false
        );
        String filePath = this.getFixturePath("SimpleModelOneOverride.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/Override",
                preferenceClassFile
        );
    }

    /**
     * Test preference class file generation with inheritance
     */
    public void testGeneratePreferenceClassFileWithInheritance() {
        PsiFile preferenceClassFile = createPreferenceClassFile(
                targetSimpleModelTwoClassFqn,
                "Model/Override",
                "SimpleModelTwoOverride",
                "Foo\\Bar\\Model\\Override\\SimpleModelTwoOverride",
                "Foo\\Bar\\Model\\Override",
                true
        );
        String filePath = this.getFixturePath("SimpleModelTwoOverride.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/Override",
                preferenceClassFile
        );
    }

    /**
     * Create preference class file
     *
     * @param targetClassFnq Target Class FQN
     * @param preferenceDirectory Preference Directory
     * @param preferenceClassName Preference Class Name
     * @param preferenceFqn Preference FQN
     * @param namespace Namespace
     * @param inheritClass Inherit target class
     * @return PsiFile
     */
    private PsiFile createPreferenceClassFile(
            String targetClassFnq,
            String preferenceDirectory,
            String preferenceClassName,
            String preferenceFqn,
            String namespace,
            Boolean inheritClass
    ) {
        Project project = myFixture.getProject();
        PhpClass targetClass = GetPhpClassByFQN.getInstance(project).execute(targetClassFnq);
        PreferenceFileData preferenceFileData = new PreferenceFileData(
                preferenceDirectory,
                preferenceClassName,
                module,
                targetClass,
                preferenceFqn,
                namespace,
                inheritClass
        );
        PreferenceClassGenerator preferenceClassGenerator = new PreferenceClassGenerator(preferenceFileData, project);

        return preferenceClassGenerator.generate("test");
    }
}
