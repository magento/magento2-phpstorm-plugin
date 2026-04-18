/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.reference.provider.PhpClassReferenceProvider;
import java.util.Collection;

public class PhpClassReferenceProviderTest extends ReferenceXmlFixtureTestCase {

    public void testDiXmlTypeNameMustIgnorePhpNamespaceIndexFailures() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));

        final PsiElement element = myFixture.getFile()
                .findElementAt(myFixture.getCaretOffset())
                .getParent();
        final PsiReference[] references = new FaultyPhpClassReferenceProvider()
                .getReferencesByElement(element, new ProcessingContext());

        assertEmpty(references);
    }

    private static final class FaultyPhpClassReferenceProvider extends PhpClassReferenceProvider {
        @Override
        protected boolean hasNamespaceInIndex(
                final String namespaceIdentifier,
                final com.intellij.openapi.project.Project project
        ) {
            throw new RuntimeException("Simulated Php namespace index failure");
        }

        @Override
        protected Collection<PhpNamespace> getNamespacesByName(
                final PhpIndex phpIndex,
                final String namespaceId
        ) {
            throw new AssertionError("Should not query namespaces after the first simulated failure");
        }
    }
}
