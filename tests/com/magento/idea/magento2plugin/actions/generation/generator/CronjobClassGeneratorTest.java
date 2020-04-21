/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CronjobClassData;

public class CronjobClassGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateFile() {
        String filePath = this.getFixturePath("CleanTableCronjob.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);

        Project project = myFixture.getProject();
        CronjobClassData cronjobClassData = new CronjobClassData(
            "CleanTableCronjob",
            "Cron/Test",
            "Foo\\Bar\\Cron\\Test",
            "Foo_Bar"
        );
        CronjobClassGenerator cronjobClassGenerator = new CronjobClassGenerator(project, cronjobClassData);
        PsiFile cronJobFile = cronjobClassGenerator.generate("test");

        assertGeneratedFileIsCorrect(expectedFile, "src/app/code/Foo/Bar/Cron/Test", cronJobFile);
    }
}
