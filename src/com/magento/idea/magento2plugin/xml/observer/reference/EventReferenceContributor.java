package com.magento.idea.magento2plugin.xml.observer.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.magento.idea.magento2plugin.xml.observer.reference.util.ClassResultsFillerWrapper;
import com.magento.idea.magento2plugin.xml.observer.reference.util.FilesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ResolveResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.VirtualTypesResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/2/15.
 */
public class EventReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PhpPatterns
                .phpLiteralExpression()
                .withParent(
                    PlatformPatterns
                        .psiElement(ParameterList.class)
                        .withParent(
                            PhpPatterns
                                .phpFunctionReference()
                        )
                ).withLanguage(PhpLanguage.INSTANCE),
            new EventReferenceProvider(
                new ResolveResultsFiller[] {
                    FilesResultsFiller.INSTANCE,
                    ClassResultsFillerWrapper.INSTANCE
                }
            )
        );
    }

}