/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class PhpClassLookupTest extends BasePlatformTestCase {

    public void testExistsRecognizesClassesAndInterfaces() {
        myFixture.addFileToProject("Example.php", "<?php namespace Example; class Model {} interface Contract {}");
        IndexingTestUtil.waitUntilIndexesAreReady(getProject());

        final GetPhpClassByFQN lookup = GetPhpClassByFQN.getInstance(getProject());
        assertTrue(lookup.exists("Example\\Model"));
        assertTrue(lookup.exists("Example\\Contract"));
        assertFalse(lookup.exists("Example\\Missing"));
    }

    public void testContainingFileRequiresPhpClassDeclaration() {
        final GetFirstClassOfFile lookup = GetFirstClassOfFile.getInstance();
        final PsiFile declarations = myFixture.configureByText(
                "Declarations.php", "<?php namespace Example; class First {} class Second {}"
        );
        assertSame(declarations, lookup.getContainingFile(declarations));

        final PsiFile functions = myFixture.configureByText("Functions.php", "<?php function example() {}");
        assertNull(lookup.getContainingFile(functions));
        final PsiFile text = myFixture.configureByText("Example.txt", "class Example {}");
        assertNull(lookup.getContainingFile(text));
    }
}
