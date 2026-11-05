/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.reference.provider.PhpClassReferenceProvider;
import java.io.EOFException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhpClassReferenceProviderTest extends ReferenceXmlFixtureTestCase {

    private static final String CLASS_NAME = "NamespaceProbe\\MixedCase\\Subject";

    public void testMixedCaseNamespacesResolveFromXmlAttribute() {
        assertNamespaceAndClassReferences(createSource(CLASS_NAME, false), CLASS_NAME);
    }

    public void testMixedCaseNamespacesResolveFromXmlText() {
        assertNamespaceAndClassReferences(createSource(CLASS_NAME, true), CLASS_NAME);
    }

    public void testNamespaceReferencesInXmlAttributeIgnoreCase() {
        final String value = "namespaceprobe\\mixedcase\\subject";
        assertNamespaceAndClassReferences(createSource(value, false), value);
    }

    public void testNamespaceReferencesInXmlTextIgnoreCase() {
        final String value = "NAMESPACEPROBE\\MIXEDCASE\\SUBJECT";
        assertNamespaceAndClassReferences(createSource(value, true), value);
    }

    public void testClassIndexReadFailurePreservesNamespaceReferences() {
        final PsiElement source = createSource(CLASS_NAME, false);
        assertNamespaceAndClassReferences(source, CLASS_NAME);
        final AtomicBoolean classLookupReached = new AtomicBoolean();
        final PhpClassReferenceProvider provider = new PhpClassReferenceProvider() {
            @Override
            protected Collection<PhpClass> getClassesByFqn(
                    final PhpIndex phpIndex,
                    final String classFqn
            ) {
                classLookupReached.set(true);
                throw new UncheckedIOException(new EOFException("Simulated index read failure"));
            }
        };

        final PsiReference[] references = provider.getReferencesByElement(
                source, new ProcessingContext()
        );

        assertTrue("The failing class lookup must be reached", classLookupReached.get());
        assertEquals("Both namespace references must survive", 2, references.length);
        assertReference(references, source, "NamespaceProbe", "\\NamespaceProbe", PhpNamespace.class);
        assertReference(references, source, "MixedCase", "\\NamespaceProbe\\MixedCase", PhpNamespace.class);
    }

    public void testNamespaceIndexCancellationPropagates() {
        final PsiElement source = createSource(CLASS_NAME, false);
        final ProcessCanceledException cancellation = new ProcessCanceledException();
        final PhpClassReferenceProvider provider = new PhpClassReferenceProvider() {
            @Override
            protected boolean hasNamespaceInIndex(final String namespaceId, final Project project) {
                throw cancellation;
            }
        };

        assertCancellationPropagates(provider, source, cancellation);
    }

    public void testClassIndexCancellationPropagates() {
        final PsiElement source = createSource(CLASS_NAME, true);
        final ProcessCanceledException cancellation = new ProcessCanceledException();
        final PhpClassReferenceProvider provider = new PhpClassReferenceProvider() {
            @Override
            protected Collection<PhpClass> getClassesByFqn(
                    final PhpIndex phpIndex,
                    final String classFqn
            ) {
                throw cancellation;
            }
        };

        assertCancellationPropagates(provider, source, cancellation);
    }

    public void testDiXmlTypeNameMustIgnorePhpNamespaceIndexFailures() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));

        final PsiElement element = myFixture.getFile()
                .findElementAt(myFixture.getCaretOffset())
                .getParent();
        final PsiReference[] references = new FaultyPhpClassReferenceProvider()
                .getReferencesByElement(element, new ProcessingContext());

        assertEmpty(references);
    }

    private PsiElement createSource(final String className, final boolean inXmlText) {
        myFixture.addFileToProject("app/code/NamespaceProbe/ParentNamespace.php",
                "<?php namespace NamespaceProbe; class Anchor {}");
        myFixture.addFileToProject("app/code/NamespaceProbe/MixedCase/Subject.php",
                "<?php namespace NamespaceProbe\\MixedCase; class Subject {}");
        final String declaration = inXmlText
                ? "<argument name=\"subject\" xsi:type=\"object\">" + className + "</argument>"
                : "<type name=\"" + className + "\"/>";
        final XmlFile file = (XmlFile) myFixture.configureByText("di.xml",
                "<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + declaration + "</config>");

        if (inXmlText) {
            return file.getRootTag().findFirstSubTag("argument")
                    .getValue().getTextElements()[0].getFirstChild();
        }

        return file.getRootTag().findFirstSubTag("type").getAttribute("name").getValueElement();
    }

    private void assertNamespaceAndClassReferences(final PsiElement source, final String value) {
        final PsiReference[] references = source.getReferences();
        final String[] parts = value.split("\\\\");
        assertReference(references, source, parts[0], "\\NamespaceProbe", PhpNamespace.class);
        assertReference(references, source, parts[1], "\\NamespaceProbe\\MixedCase", PhpNamespace.class);
        assertReference(references, source, parts[2], "\\" + CLASS_NAME, PhpClass.class);
    }

    private void assertReference(
            final PsiReference[] references,
            final PsiElement source,
            final String segment,
            final String targetFqn,
            final Class<? extends PhpNamedElement> targetType
    ) {
        for (final PsiReference reference : references) {
            if (!(reference instanceof PolyVariantReferenceBase)) {
                continue;
            }
            for (final ResolveResult result : ((PolyVariantReferenceBase) reference).multiResolve(false)) {
                final PsiElement target = result.getElement();
                if (targetType.isInstance(target)
                        && targetFqn.equals(((PhpNamedElement) target).getFQN())) {
                    assertEquals(segment, reference.getRangeInElement().substring(source.getText()));
                    return;
                }
            }
        }
        fail("Missing reference from " + source.getText() + " to " + targetFqn);
    }

    private void assertCancellationPropagates(
            final PhpClassReferenceProvider provider,
            final PsiElement source,
            final ProcessCanceledException cancellation
    ) {
        try {
            provider.getReferencesByElement(source, new ProcessingContext());
            fail("Index cancellation must propagate");
        } catch (ProcessCanceledException actual) {
            assertSame(cancellation, actual);
        }
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
