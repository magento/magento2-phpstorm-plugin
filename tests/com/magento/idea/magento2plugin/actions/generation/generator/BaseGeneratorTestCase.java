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
import org.junit.Before;

abstract public class BaseGeneratorTestCase extends BaseProjectTestCase {
    private static final String testDataFolderPath = "testData" + File.separator + "actions" + File.separator;
    private static final String fixturesFolderPath = "generation" + File.separator + "generator" + File.separator;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        LightPlatformTestCase.closeAndDeleteProject();
    }

    protected String getFixturePath(String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }

    protected void assertGeneratedFileIsCorrect(
        PsiFile expectedFile,
        String expectedDirectory,
        PsiFile resultFile) {

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
