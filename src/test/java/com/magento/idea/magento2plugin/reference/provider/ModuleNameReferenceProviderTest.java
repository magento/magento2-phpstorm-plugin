/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.IndexingTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleNameReferenceProviderTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Settings.getInstance(getProject()).pluginEnabled = true;
    }

    public void testReferenceCollectionDoesNotAccessProjectOrIndexes() {
        final PsiElement element = new FakePsiElement() {
            @Override
            public String getText() {
                return "\"prefix Vendor_Module::template.phtml\"";
            }

            @Override
            public PsiElement getParent() {
                throw new AssertionError("Reference collection must only inspect the element text");
            }

            @Override
            public Project getProject() {
                throw new AssertionError("Reference collection must not access project indexes");
            }
        };
        final PsiReference reference = assertOneElement(references(element));
        assertEquals(new TextRange(8, 21), reference.getRangeInElement());
        assertTrue(reference.isSoft());
    }

    public void testTextWithoutModuleNameHasNoReference() {
        assertEmpty(references(attribute("template/path.phtml")));
    }

    public void testUnknownModuleIsUnresolvedAndSoft() {
        final PsiReference reference = assertOneElement(references(attribute("Unknown_Module")));
        assertTrue(reference.isSoft());
        assertNull(reference.resolve());
    }

    public void testReferenceResolvesModuleAddedAfterCollection() {
        final PsiReference reference = assertOneElement(references(attribute("Vendor_Module")));
        assertNull(reference.resolve());
        final PsiFile module = addModule("app/code/Vendor/Module");
        assertEquals(module.getContainingDirectory().getParentDirectory(), reference.resolve());
    }

    public void testReferenceResolvesAllModuleRoots() {
        final PsiFile first = addModule("app/code/Vendor/Module");
        final PsiFile second = addModule("vendor/vendor/module");
        final PsiPolyVariantReference reference = (PsiPolyVariantReference) assertOneElement(
                references(attribute("Vendor_Module"))
        );
        final Set<PsiElement> targets = Arrays.stream(reference.multiResolve(false))
                .map(ResolveResult::getElement).collect(Collectors.toSet());
        assertEquals(Set.of(
                first.getContainingDirectory().getParentDirectory(),
                second.getContainingDirectory().getParentDirectory()
        ), targets);
    }

    public void testReferenceCollectedDuringIndexingResolvesAfterwards() {
        final PsiFile module = addModule("app/code/Vendor/Module");
        final PsiElement element = attribute("Vendor_Module");
        final PsiReference[] collected = new PsiReference[1];
        DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
            collected[0] = assertOneElement(references(element));
            assertNull(collected[0].resolve());
        });
        assertEquals(module.getContainingDirectory().getParentDirectory(), collected[0].resolve());
    }

    public void testReferenceDoesNotRetainDeletedModuleTarget() {
        final PsiFile module = addModule("app/code/Vendor/Module");
        final PsiReference reference = assertOneElement(references(attribute("Vendor_Module")));
        assertInstanceOf(reference.resolve(), PsiDirectory.class);
        WriteCommandAction.runWriteCommandAction(getProject(), module::delete);
        IndexingTestUtil.waitUntilIndexesAreReady(getProject());
        assertNull(reference.resolve());
    }

    public void testJavaScriptContributorResolvesModuleRoot() {
        final PsiFile module = addModule("app/code/Vendor/Module");
        myFixture.configureByText("component.js", "const moduleName = 'Vendor_<caret>Module';");
        final PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
        assertTrue(Arrays.stream(element.getReferences()).anyMatch(reference ->
                module.getContainingDirectory().getParentDirectory().equals(reference.resolve())
        ));
    }

    public void testXmlTextContributorResolvesModuleRoot() {
        final PsiFile module = addModule("app/code/Vendor/Module");
        myFixture.configureByText("config.xml", "<config>Vendor_<caret>Module</config>");
        final PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        assertTrue(Arrays.stream(element.getReferences()).anyMatch(reference ->
                module.getContainingDirectory().getParentDirectory().equals(reference.resolve())
        ));
    }

    private PsiElement attribute(final String value) {
        final XmlFile file = (XmlFile) myFixture.configureByText(
                "config.xml", "<config name=\"" + value + "\"/>"
        );
        return file.getRootTag().getAttribute("name").getValueElement();
    }

    private PsiFile addModule(final String root) {
        final PsiFile file = myFixture.addFileToProject(
                root + "/etc/module.xml", "<config><module name=\"Vendor_Module\"/></config>"
        );
        IndexingTestUtil.waitUntilIndexesAreReady(getProject());
        return file;
    }

    private PsiReference[] references(final PsiElement element) {
        return new ModuleNameReferenceProvider().getReferencesByElement(element, new ProcessingContext());
    }
}
