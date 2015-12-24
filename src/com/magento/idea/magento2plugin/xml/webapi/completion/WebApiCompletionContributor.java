package com.magento.idea.magento2plugin.xml.webapi.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.xml.completion.InterfaceCompletionProvider;
import com.magento.idea.magento2plugin.xml.webapi.XmlHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by mslabko on 10/15/15.
 */
public class WebApiCompletionContributor extends CompletionContributor {
    public WebApiCompletionContributor() {
        // add services to list
        extend(
            CompletionType.BASIC,
            XmlHelper.getTagAttributePattern(XmlHelper.SERVICE_TAG, XmlHelper.CLASS_ATTRIBUTE),
            new CompletionProvider<CompletionParameters>() {
                public void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
                    PsiElement psiElement = parameters.getOriginalPosition();

                    resultSet.addAllElements(InterfaceCompletionProvider.INSTANCE.collectCompletionResult(psiElement));
                }
            }
        );

        // Add service methods to list
        extend(
            CompletionType.BASIC,
            XmlHelper.getTagAttributePattern(XmlHelper.SERVICE_TAG, XmlHelper.METHOD_ATTRIBUTE),
            new CompletionProvider<CompletionParameters>() {
                public void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {

                    PsiElement psiElement = parameters.getOriginalPosition();

                    PhpClass serviceInterface = getServiceInterface(psiElement);
                    if (serviceInterface != null) {
                        for (Method serviceMethod : serviceInterface.getMethods()) {

                            resultSet.addElement(
                                LookupElementBuilder
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
        XmlAttribute classAttribute = XmlHelper.getInterfaceAttributeByMethod(psiElement);
        if (classAttribute == null) {
            return null;
        }

        XmlAttributeValue xmlAttributeValue = classAttribute.getValueElement();

        if (xmlAttributeValue == null) {
            return null;
        }

        PsiReference reference = xmlAttributeValue.getReference();
        if (reference == null) {
            return null;
        }

        PsiElement element = reference.resolve();

        if (element instanceof PhpClass) {
            return (PhpClass) element;
        }

        return null;
    }
}
