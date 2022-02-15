/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider.mftf;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.MftfTest;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.TestExtendsIndex;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TestExtendedByReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        final List<PsiReference> extendedByReferences = new ArrayList<>();
        final String testName = StringUtil.unquoteString(element.getText());
        final Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        TestExtendsIndex.KEY,
                        testName,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(element.getProject()),
                                XmlFileType.INSTANCE
                        )
                );
        final PsiManager psiManager = PsiManager.getInstance(element.getProject());
        final List<PsiElement> psiElements = new ArrayList<>();

        for (final VirtualFile virtualFile: containingFiles) {
            final PsiFile file = psiManager.findFile(virtualFile);

            if (!(file instanceof XmlFile)) {
                continue;
            }
            final XmlFile xmlFile = (XmlFile) file;

            final Collection<XmlAttributeValue> valueElements = XmlPsiTreeUtil
                    .findAttributeValueElements(
                            xmlFile,
                            MftfTest.TEST_TAG,
                            MftfTest.EXTENDS_ATTRIBUTE,
                            testName
                    );

            psiElements.addAll(valueElements);
        }

        if (!psiElements.isEmpty()) {
            extendedByReferences.add(new PolyVariantReferenceBase(element, psiElements));
        }

        return extendedByReferences.toArray(new PsiReference[0]);
    }
}
