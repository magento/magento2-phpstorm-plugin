/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.IndexingTestUtil;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import java.util.Collection;

public class XmlVirtualTypeIndexTest extends BaseProjectTestCase {

    private PsiFile firstDeclaration;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        firstDeclaration = myFixture.addFileToProject(
                "app/code/Portable/First/etc/di.xml",
                "<config><virtualType name=\"PortableVirtualOne\" type=\"Unknown\\Source\"/></config>"
        );
        myFixture.addFileToProject(
                "app/code/Portable/Second/etc/frontend/di.xml",
                "<config><virtualType name=\"PortableVirtualTwo\" type=\"Unknown\\Source\"/></config>"
        );
        IndexingTestUtil.waitUntilIndexesAreReady(getProject());
    }

    public void testNamesRespectPrefixAndFileScope() {
        final XmlVirtualTypeIndex index = new XmlVirtualTypeIndex(getProject());
        final GlobalSearchScope scope = GlobalSearchScope.fileScope(
                getProject(), firstDeclaration.getVirtualFile()
        );

        assertSameElements(
                index.getAllVirtualTypeElementNames(new PlainPrefixMatcher("Portable"), scope),
                "PortableVirtualOne"
        );
        assertEmpty(index.getAllVirtualTypeElementNames(new PlainPrefixMatcher("Other"), scope));
        assertEmpty(index.getVirtualTypeElements("PortableVirtualTwo", scope));
    }

    public void testTypeAttributeCompletionUsesXmlDeclarations() {
        myFixture.configureByText(
                "di.xml", "<config><preference for=\"Unknown\\Target\" type=\"Portable<caret>\"/></config>"
        );
        myFixture.completeBasic();

        assertContainsElements(
                myFixture.getLookupElementStrings(), "PortableVirtualOne", "PortableVirtualTwo"
        );
    }

    public void testObjectArgumentCompletionUsesXmlDeclarations() {
        myFixture.configureByText(
                "di.xml",
                "<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + "<type name=\"Unknown\\Target\"><arguments><argument name=\"value\" "
                        + "xsi:type=\"object\">Portable<caret></argument></arguments></type></config>"
        );
        myFixture.completeBasic();

        assertContainsElements(
                myFixture.getLookupElementStrings(), "PortableVirtualOne", "PortableVirtualTwo"
        );
    }

    public void testTypeAttributeNavigationUsesXmlDeclarations() {
        myFixture.configureByText(
                "di.xml",
                "<config><preference for=\"Unknown\\Target\" type=\"PortableVirtual<caret>One\"/></config>"
        );

        assertResolvesToFirstDeclaration(
                myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent()
        );
    }

    public void testObjectArgumentNavigationUsesXmlDeclarations() {
        myFixture.configureByText(
                "di.xml",
                "<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + "<type name=\"Unknown\\Target\"><arguments><argument name=\"value\" "
                        + "xsi:type=\"object\">PortableVirtual<caret>One</argument></arguments></type></config>"
        );

        assertResolvesToFirstDeclaration(
                myFixture.getFile().findElementAt(myFixture.getCaretOffset())
        );
    }

    public void testLegacyDiLookupDelegatesToXmlDeclarations() {
        final Collection<PsiElement> declarations = DiIndex.getInstance(getProject())
                .getVirtualTypeElements("PortableVirtualOne", GlobalSearchScope.allScope(getProject()));

        assertSize(1, declarations);
        assertEquals(firstDeclaration, declarations.iterator().next().getContainingFile());
    }

    private void assertResolvesToFirstDeclaration(final PsiElement source) {
        for (final PsiReference reference : source.getReferences()) {
            if (!(reference instanceof PsiPolyVariantReference)) {
                continue;
            }
            for (final ResolveResult result : ((PsiPolyVariantReference) reference).multiResolve(false)) {
                final PsiElement target = result.getElement();

                if (target != null && firstDeclaration.equals(target.getContainingFile())) {
                    return;
                }
            }
        }

        fail("Expected navigation to the XML virtual-type declaration");
    }
}
