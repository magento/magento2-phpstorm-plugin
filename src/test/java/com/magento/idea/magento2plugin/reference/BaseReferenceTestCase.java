/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.inspections.BaseInspectionsTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.TooManyMethods",
})
public abstract class BaseReferenceTestCase extends BaseInspectionsTestCase {
    private static final String testDataFolderPath = "testData" + File.separator//NOPMD
            + "reference" + File.separator;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    protected void assertHasReferenceToXmlAttributeValue(final String reference) {
        final PsiElement element = getElementFromCaret();
        for (final PsiReference psiReference: element.getReferences()) {
            if (psiReference instanceof PolyVariantReferenceBase) {
                final ResolveResult[] resolveResults
                        = ((PolyVariantReferenceBase) psiReference).multiResolve(true);

                for (final ResolveResult resolveResult : resolveResults) {
                    final PsiElement resolved = resolveResult.getElement();
                    if (!(resolved instanceof XmlAttributeValue)) {
                        continue;
                    }

                    if (((XmlAttributeValue) resolved).getValue().equals(reference)) {
                        return;
                    }
                }
            } else {
                final PsiElement resolved = psiReference.resolve();
                if (!(resolved instanceof XmlAttributeValue)) {
                    continue;
                }

                if (((XmlAttributeValue) resolved).getValue().equals(reference)) {
                    return;
                }
            }
        }
        final String referenceNotFound =
                "Failed that element contains reference to the attribute value `%s`";

        fail(String.format(referenceNotFound, reference));
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    protected void assertHasReferenceToXmlTag(final String tagName) {
        final PsiElement element = getElementFromCaret();
        for (final PsiReference psiReference: element.getReferences()) {
            if (psiReference instanceof PolyVariantReferenceBase) {
                final ResolveResult[] resolveResults
                        = ((PolyVariantReferenceBase) psiReference).multiResolve(true);

                for (final ResolveResult resolveResult : resolveResults) {
                    final PsiElement resolved = resolveResult.getElement();
                    if (!(resolved instanceof XmlTag)) {
                        continue;
                    }

                    if (((XmlTag) resolved).getName().equals(tagName)) {
                        return;
                    }
                }
            } else {
                final PsiElement resolved = psiReference.resolve();
                if (!(resolved instanceof XmlTag)) {
                    continue;
                }

                if (((XmlTag) resolved).getName().equals(tagName)) {
                    return;
                }
            }
        }
        final String referenceNotFound
                = "Failed that element contains reference to the XML tag `%s`";

        fail(String.format(referenceNotFound, tagName));
    }

    protected void assertHasReferenceToFile(final String reference) {
        final PsiElement element = getElementFromCaret();

        assertHasReferenceToFile(reference, Arrays.asList(element.getReferences()));
    }

    protected void assertHasReferenceToFile(
            final String reference,
            final Class<? extends PsiReferenceProvider> providerClass
    ) {
        final PsiElement element = getLeafElementFromCaret();
        final List<PsiReference> references = new ArrayList<>();

        try {
            final PsiReferenceProvider provider = providerClass.getConstructor().newInstance();
            references.addAll(
                    Arrays.asList(
                            provider.getReferencesByElement(element, new ProcessingContext())
                    )
            );
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | InstantiationException exception
        ) {
            references.addAll(Arrays.asList(element.getReferences()));
        }

        assertHasReferenceToFile(reference, references);
    }

    protected void assertHasReferenceToFile(
            final String reference,
            final List<PsiReference> references
    ) {
        for (final PsiReference psiReference : references) {
            final PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof PsiFile)) {
                continue;
            }
            if (((PsiFile) resolved).getVirtualFile().getPath().endsWith(reference)) {
                return;
            }
        }
        final String referenceNotFound = "Failed that element contains reference to the file `%s`";

        fail(String.format(referenceNotFound, reference));
    }

    protected void assertHasReferenceToXmlFile(final String fileName) {
        final PsiElement element = getElementFromCaret();
        for (final PsiReference psiReference : element.getReferences()) {
            final PsiElement resolved = psiReference.resolve();
            if (!(resolved instanceof XmlFile)) {
                continue;
            }

            if (((XmlFile) resolved).getName().equals(fileName)) {
                return;
            }
        }
        final String referenceNotFound
                = "Failed that element contains reference to the XML tag `%s`";

        fail(String.format(referenceNotFound, fileName));
    }

    protected void assertHasReferenceToDirectory(final String directoryName) {
        for (final PsiReference psiReference : getElementFromCaret().getReferences()) {
            final PsiElement resolvedElement = psiReference.resolve();
            if (resolvedElement instanceof PsiDirectoryImpl
                    && ((PsiDirectoryImpl) resolvedElement).getName().equals(directoryName)) {
                return;
            }
        }

        final String referenceNotFound
                = "Failed that element contains reference to the directory `%s`";
        fail(String.format(referenceNotFound, directoryName));
    }

    protected void assertHasNoReferenceToDirectory(final String directoryName) {
        for (final PsiReference psiReference : getElementFromCaret().getReferences()) {
            final PsiElement resolvedElement = psiReference.resolve();
            if (resolvedElement instanceof PsiDirectoryImpl
                    && ((PsiDirectoryImpl) resolvedElement).getName().equals(directoryName)) {
                final String referenceNotFound
                        = "Failed that element does not contain reference to the directory `%s`";
                fail(String.format(referenceNotFound, directoryName));
            }
        }
    }

    @SuppressWarnings("PMD")
    protected void assertHasReferencePhpClass(final String phpClassFqn) {
        final PsiElement element = getElementFromCaret();
        final PsiReference[] references = element.getReferences();
        String result = ((PhpClass) references[references.length - 1]
                .resolve())
                .getPresentableFQN();
        assertEquals(
                phpClassFqn,
                result
        );
    }

    protected void assertHasReferencetoConstructorParameter(
            final String argumentClassFqn,
            final String argumentName
    ) {
        final PsiElement element = getElementFromCaret();
        final @Nullable PsiReference reference = element.getReference();

        if (reference == null) {
            final String referenceNotFound
                    = "Failed that element does not contain and reference";
            fail(referenceNotFound);
        }

        final String parameterClassFqn = ((Parameter) reference.resolve())
                .getLocalType().toStringResolved();
        final String parameterName = ((Parameter) reference.resolve()).getName();

        assertEquals("Class name in argument equals class name in parameter",
                parameterClassFqn,
                argumentClassFqn);
        assertEquals("Variable name in argument equals variable name in parameter",
                parameterName,
                argumentName);
    }

    protected void assertHasReferenceToClassMethod(
            final String className,
            final String methodName
    ) {
        final PsiElement element = getElementFromCaret();
        final PsiReference[] references = element.getReferences();
        final String actualClassName = ((PhpClass) references[references.length - 1].resolve()
                .getParent()).getPresentableFQN();
        final String actualMethodName = ((Method) references[references.length - 1].resolve())
                .getName();

        assertEquals(
                "Class name",
                className,
                actualClassName
        );
        assertEquals(
                "Method name",
                methodName,
                actualMethodName
        );
    }

    protected void assertHasReferenceToMethodArgument(final String argument) {
        final PsiReference[] references = getElementFromCaret().getReferences();
        final String actualArgument
                = StringUtil.unquoteString(references[references.length - 1].resolve().getText());
        final PsiElement parameterList = references[0].resolve().getParent();

        if (!(parameterList instanceof ParameterList)) {
            fail("Element doesn't have a reference to a method parameter");
        }

        assertEquals(
                "Event dispatch argument",
                argument,
                actualArgument
        );
    }

    protected void assertEmptyReference() {
        final PsiElement element = getElementFromCaret();
        assertEmpty(element.getReferences());
    }

    private PsiElement getElementFromCaret() {
        return myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
    }

    private PsiElement getLeafElementFromCaret() {
        return myFixture.getFile().findElementAt(myFixture.getCaretOffset());
    }
}
