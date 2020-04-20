/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import java.io.File;

abstract public class BaseGeneratorTestCase extends BaseProjectTestCase {
    private static final String testDataFolderPath = "testData" + File.separator + "actions" + File.separator;
    private static final String fixturesFolderPath = "generation" + File.separator + "generator" + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected String getFixturePath(String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }

    protected void assertGeneratedFileIsCorrect(
        PsiFile expectedFile,
        String expectedDirectory,
        PsiFile resultFile) {

        assertEquals(expectedDirectory, resultFile.getContainingDirectory().getVirtualFile().getPresentableUrl());
        assertEquals(expectedFile.getText(), resultFile.getText());
        assertEquals(expectedFile.getName(), resultFile.getName());
    }
}
