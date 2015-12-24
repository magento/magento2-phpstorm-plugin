package com.magento.idea.magento2plugin.xml.di.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import com.magento.idea.magento2plugin.xml.completion.ClassCompletionProvider;
import com.magento.idea.magento2plugin.xml.completion.CompletionProviderI;
import com.magento.idea.magento2plugin.xml.completion.VirtualTypeCompletionProvider;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkvashnin on 10/15/15.
 */
public class DiCompletionContributor extends CompletionContributor {
    private CompletionProviderI[] completionProviders = new CompletionProviderI[] {
        ClassCompletionProvider.INSTANCE,
        VirtualTypeCompletionProvider.INSTANCE
    };

    public DiCompletionContributor() {
        extend(CompletionType.BASIC,
            XmlPatterns.or(
                XmlHelper.getItemValuePatternForType(XmlHelper.OBJECT_TYPE),
                XmlHelper.getTagAttributePattern(XmlHelper.TYPE_TAG, XmlHelper.NAME_ATTRIBUTE),
                XmlHelper.getTagAttributePattern(XmlHelper.VIRTUAL_TYPE_TAG, XmlHelper.TYPE_ATTRIBUTE),
                XmlHelper.getTagAttributePattern(XmlHelper.PLUGIN_TAG, XmlHelper.TYPE_ATTRIBUTE)
            ),
            new CompletionProvider<CompletionParameters>() {
                public void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
                    PsiElement psiElement = parameters.getOriginalPosition();
                    for (CompletionProviderI completionProvider: completionProviders) {
                        resultSet.addAllElements(completionProvider.collectCompletionResult(psiElement));
                    }
                }
            }
        );

        extend(CompletionType.BASIC,
            XmlHelper.getTagAttributePattern(XmlHelper.PREFERENCE_TAG, XmlHelper.TYPE_ATTRIBUTE),
            new CompletionProvider<CompletionParameters>() {
                public void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
                    PsiElement psiElement = parameters.getOriginalPosition();
                    PhpClass preferenceClass = getPreferenceClass(psiElement);

                    if (preferenceClass == null) {
                        return;
                    }

                    ParentTypeMatcher parentTypeMatcher = new ParentTypeMatcher(preferenceClass);
                    resultSet.addAllElements(ClassCompletionProvider.INSTANCE
                            .collectCompletionResult(psiElement, parentTypeMatcher)
                    );

                    resultSet.addAllElements(VirtualTypeCompletionProvider.INSTANCE
                            .collectCompletionResult(
                                psiElement,
                                new VirtualTypeParentMatcher(parentTypeMatcher, psiElement.getProject())
                            )
                    );
                }
            }
        );

        extend(CompletionType.BASIC,
            XmlHelper.getArgumentValuePatternForType(XmlHelper.OBJECT_TYPE),
            new CompletionProvider<CompletionParameters>() {
                public void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
                    PsiElement psiElement = parameters.getOriginalPosition();

                    PhpClass argumentClass = getArgumentClass(psiElement);

                    if (argumentClass == null) {
                        return;
                    }

                    ParentTypeMatcher parentTypeMatcher = new ParentTypeMatcher(argumentClass);

                    resultSet.addAllElements(ClassCompletionProvider.INSTANCE
                        .collectCompletionResult(psiElement, parentTypeMatcher)
                    );

                    resultSet.addAllElements(VirtualTypeCompletionProvider.INSTANCE
                            .collectCompletionResult(
                                psiElement,
                                new VirtualTypeParentMatcher(parentTypeMatcher, psiElement.getProject())
                            )
                    );
                }
            }
        );
    }

    private class ParentTypeMatcher implements PsiContextMatcherI<PsiElement> {

        private PhpClass parentType;

        public ParentTypeMatcher(PhpClass parentType) {
            this.parentType = parentType;
        }

        @Override
        public boolean match(PsiElement psiElement) {
            if (!(psiElement instanceof PhpClass)) {
                return false;
            }

            return parentType.equals(psiElement)
                || match(((PhpClass)psiElement).getImplementedInterfaces())
                || match(((PhpClass) psiElement).getSuperClass());
        }

        public boolean match(@Nullable PhpClass[] probableMistakes) {
            if (probableMistakes == null) {
                return false;
            }

            boolean result = false;

            for (PhpClass probableMistake : probableMistakes) {
                result = parentType.equals(probableMistake) || match(probableMistake.getImplementedInterfaces());
            }

            return result;
        }
    }

    private class VirtualTypeParentMatcher implements PsiContextMatcherI<String> {
        private ParentTypeMatcher parentTypeMatcher;
        private Project project;

        public VirtualTypeParentMatcher(ParentTypeMatcher parentTypeMatcher, Project project) {
            this.parentTypeMatcher = parentTypeMatcher;
            this.project = project;
        }

        @Override
        public boolean match(String virtualType) {
            List<PhpClass> superParentTypes = VirtualTypesNamesFileBasedIndex.getSuperParentTypes(project, virtualType);

            return parentTypeMatcher.match(
                superParentTypes.toArray(new PhpClass[superParentTypes.size()])
            );
        }
    }

    @Nullable
    private PhpClass getPreferenceClass(PsiElement psiElement) {
        XmlAttribute parentAttribute = PsiTreeUtil.getParentOfType(psiElement, XmlAttribute.class);
        if (parentAttribute == null) {
            return null;
        }

        XmlTag xmlTag = parentAttribute.getParent();
        XmlAttribute preferenceAttr = xmlTag.getAttribute(XmlHelper.FOR_ATTRIBUTE);
        if (preferenceAttr == null) {
            return null;
        }

        XmlAttributeValue valueElement = preferenceAttr.getValueElement();
        PsiReference reference = valueElement.getReference();
        if (reference == null) {
            return null;
        }

        PsiElement element = reference.resolve();
        if (element instanceof PhpClass) {
            return (PhpClass) element;
        }

        return null;
    }

    @Nullable
    private PhpClass getArgumentClass(PsiElement psiElement) {
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
        if (xmlTag == null) {
            return null;
        }

        XmlAttribute preferenceAttr = xmlTag.getAttribute(XmlHelper.NAME_ATTRIBUTE);
        if (preferenceAttr == null) {
            return null;
        }

        XmlAttributeValue valueElement = preferenceAttr.getValueElement();
        PsiReference reference = valueElement.getReference();
        if (reference == null) {
            return null;
        }

        PsiElement element = reference.resolve();
        if (element instanceof Parameter) {
            String stringResolved = ((Parameter) element).getDeclaredType().toStringResolved();
            PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());

            ArrayList<PhpClass> result = new ArrayList<>();
            result.addAll(phpIndex.getClassesByFQN(stringResolved));
            result.addAll(phpIndex.getInterfacesByFQN(stringResolved));

            if (result.size() > 0) {
                return result.get(0);
            }
        }

        return null;
    }
}
