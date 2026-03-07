/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DefaultCodeStyleSettingsAdjustmentsUtil;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.project.util.GetProjectBasePath;

public abstract class BaseGeneratorTestCase extends BaseProjectTestCase {
    private static final String TEST_DATA_FOLDER_PATH =  "src/test/resources/testData" + File.separator
            + "actions" + File.separator;
    private static final String FIXTURES_FOLDER_PATH = "generation" + File.separator
            + "generator" + File.separator;

    @BeforeEach
    public void setUp() throws Exception {
        myFixture.setTestDataPath(TEST_DATA_FOLDER_PATH);
        // Reset changed default code style settings to the previous default settings.
        DefaultCodeStyleSettingsAdjustmentsUtil.execute(myFixture.getProject());
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    protected String getFixturePath(final String fileName) {
        return prepareFixturePath(fileName, FIXTURES_FOLDER_PATH);
    }

    @SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
    protected void assertGeneratedFileIsCorrect(
            final PsiFile expectedFile,
            final String expectedDirectory,
            final PsiFile resultFile
    ) {
        com.intellij.openapi.application.ApplicationManager.getApplication().runReadAction(() -> {
            Assertions.assertTrue(resultFile.getContainingDirectory().getVirtualFile().getPath()
                    .endsWith(expectedDirectory));
            Assertions.assertEquals(expectedFile.getText(), resultFile.getText());
            Assertions.assertEquals(expectedFile.getName(), resultFile.getName());
        });
    }

    @SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
    protected void assertGeneratedFileIsCorrect(
            final PsiFile expectedFile,
            final PsiFile resultFile
    ) {
        Assertions.assertEquals(expectedFile.getText(), resultFile.getText());
        Assertions.assertEquals(expectedFile.getName(), resultFile.getName());
    }

    protected PsiDirectory getProjectDirectory() {
        return myFixture.getPsiManager().findDirectory(
                GetProjectBasePath.execute(myFixture.getProject())
        );
    }
}
