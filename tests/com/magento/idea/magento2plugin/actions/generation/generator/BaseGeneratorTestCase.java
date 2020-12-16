/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightPlatformTestCase;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.project.util.GetProjectBasePath;
import org.junit.After;
import org.junit.Before;

public abstract class BaseGeneratorTestCase extends BaseProjectTestCase {
    private static final String TEST_DATA_FOLDER_PATH = "testData" + File.separator
            + "actions" + File.separator;
    private static final String FIXTURES_FOLDER_PATH = "generation" + File.separator
            + "generator" + File.separator;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(TEST_DATA_FOLDER_PATH);
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        super.tearDown();
        LightPlatformTestCase.closeAndDeleteProject();
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
        assertTrue(resultFile.getContainingDirectory().getVirtualFile().getPath()
                .endsWith(expectedDirectory));
        assertEquals(expectedFile.getText(), resultFile.getText());
        assertEquals(expectedFile.getName(), resultFile.getName());
    }

    protected PsiDirectory getProjectDirectory() {
        return myFixture.getPsiManager().findDirectory(
                GetProjectBasePath.execute(myFixture.getProject())
        );
    }
}
