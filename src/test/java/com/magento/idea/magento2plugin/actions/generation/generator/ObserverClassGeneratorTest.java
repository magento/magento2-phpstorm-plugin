/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverFileData;

public class ObserverClassGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateObserverClass()
    {
        Project project = myFixture.getProject();
        ObserverFileData observerData = new ObserverFileData(
                "Observer",
                "TestObserver",
                "Foo_Bar",
                "test_event",
                "Foo\\Bar\\Observer\\TestObserver",
                "Foo\\Bar\\Observer"
        );
        ObserverClassGenerator observerClassGenerator = new ObserverClassGenerator(
                observerData,
                project
        );
        PsiFile observerClassFile = observerClassGenerator.generate("test");

        String filePath = this.getFixturePath("TestObserver.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Observer",
                observerClassFile
        );
    }
}
