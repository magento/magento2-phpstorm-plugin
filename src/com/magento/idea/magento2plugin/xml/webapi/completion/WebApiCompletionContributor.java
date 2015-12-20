package com.magento.idea.magento2plugin.xml.webapi.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.xml.completion.InterfaceCompletionProvider;
import com.magento.idea.magento2plugin.xml.completion.CompletionProviderI;
import com.magento.idea.magento2plugin.xml.webapi.XmlHelper;
import org.jetbrains.annotations.NotNull;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * Created by mslabko on 10/15/15.
 */
public class WebApiCompletionContributor extends CompletionContributor {
    private CompletionProviderI[] completionProviders = new CompletionProviderI[] {
            InterfaceCompletionProvider.INSTANCE
    };

    public WebApiCompletionContributor() {
        // add services to list
        extend(CompletionType.BASIC,
                XmlPatterns.or(
                        XmlHelper.getArgumentValuePatternForType("object"),
                        XmlHelper.getItemValuePatternForType("object"),
                        XmlHelper.getTagAttributePattern(XmlHelper.SERVICE_TAG, XmlHelper.CLASS_ATTRIBUTE)
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
        // Add service methods to list
        extend(
                CompletionType.BASIC,
                XmlPatterns.or(
                        XmlHelper.getTagAttributePattern(XmlHelper.SERVICE_TAG, XmlHelper.METHOD_ATTRIBUTE)
                ),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {

                        PsiElement psiElement = parameters.getOriginalPosition();

                        PhpClass serviceInterface = getServiceInterface(psiElement);
                        if (serviceInterface != null) {
                            for (Method serviceMethod : serviceInterface.getMethods()) {
                                resultSet.addElement(LookupElementBuilder
                                        .create(serviceMethod.getName())
                                        .withIcon(PhpIcons.METHOD_ICON)
                                );
                            }
                        }
                    }
                }
        );
    }

    @Nullable
    private PhpClass getServiceInterface(PsiElement psiElement) {
        XmlAttribute methodAttribute = PsiTreeUtil.getParentOfType(psiElement, XmlAttribute.class);
        if (methodAttribute == null) {
            return null;
        }
        XmlTag serviceTag = PsiTreeUtil.getParentOfType(methodAttribute, XmlTag.class);
        if (serviceTag == null) {
            return null;
        }
        XmlAttribute serviceAttribute = serviceTag.getAttribute(XmlHelper.CLASS_ATTRIBUTE);
        if (serviceAttribute == null) {
            return null;
        }
        String className = serviceAttribute.getValue();
        if (className == null) {
            return null;
        }
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());
        Iterator<PhpClass> iterator = phpIndex.getInterfacesByFQN(PhpLangUtil.toFQN(className)).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
}
