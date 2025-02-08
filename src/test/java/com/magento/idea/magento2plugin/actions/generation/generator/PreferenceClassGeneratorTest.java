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
    private static final String MODULE = "Foo_Bar";
    private static final String TARGET_MODEL_ONE_CLASS_FQN = "Foo\\Bar\\Model\\SimpleModelOne";
    private static final String TARGET_MODEL_TWO_CLASS_FQN = "Foo\\Bar\\Model\\SimpleModelTwo";

    /**
     * Test preference class file generation.
     */
    public void testGeneratePreferenceClassFile() {
        final PsiFile preferenceClassFile = createPreferenceClassFile(
                TARGET_MODEL_ONE_CLASS_FQN,
                "Model/Override",
                "SimpleModelOneOverride",
                "Foo\\Bar\\Model\\Override\\SimpleModelOneOverride",
                "Foo\\Bar\\Model\\Override",
                false,
                false
        );
        final String filePath = this.getFixturePath("SimpleModelOneOverride.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/Override",
                preferenceClassFile
        );
    }

    /**
     * Test preference class file generation.
     */
    public void testGeneratePreferenceInterfaceFile() {
        final PsiFile preferenceClassFile = createPreferenceClassFile(
                TARGET_MODEL_ONE_CLASS_FQN,
                "Model",
                "InterfaceOverride",
                "Foo\\Bar\\Model\\InterfaceOverride",
                "Foo\\Bar\\Model",
                false,
                true
        );
        final String filePath = this.getFixturePath("InterfaceOverride.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model",
                preferenceClassFile
        );
    }

    /**
     * Test preference class file generation with inheritance.
     */
    public void testGeneratePreferenceClassFileWithInheritance() {
        final PsiFile preferenceClassFile = createPreferenceClassFile(
                TARGET_MODEL_TWO_CLASS_FQN,
                "Model/Override",
                "SimpleModelTwoOverride",
                "Foo\\Bar\\Model\\Override\\SimpleModelTwoOverride",
                "Foo\\Bar\\Model\\Override",
                true,
                false
        );
        final String filePath = this.getFixturePath("SimpleModelTwoOverride.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Model/Override",
                preferenceClassFile
        );
    }

    /**
     * Create preference class file.
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
            final String targetClassFnq,
            final String preferenceDirectory,
            final String preferenceClassName,
            final String preferenceFqn,
            final String namespace,
            final Boolean inheritClass,
            final Boolean isInterface
    ) {
        final Project project = myFixture.getProject();
        final PhpClass targetClass = GetPhpClassByFQN.getInstance(project).execute(targetClassFnq);
        final PreferenceFileData preferenceFileData = new PreferenceFileData(
                preferenceDirectory,
                preferenceClassName,
                MODULE,
                targetClass,
                preferenceFqn,
                namespace,
                inheritClass,
                isInterface
        );
        final PreferenceClassGenerator preferenceClassGenerator = new PreferenceClassGenerator(
                preferenceFileData,
                project
        );

        return preferenceClassGenerator.generate("test");
    }
}
