package com.magento.idea.magento2plugin.xml.observer;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.elements.ParameterList;

/**
 * Created by dkvashnin on 11/5/15.
 */
public class PhpPatternsHelper {
    public static final ElementPattern<? extends PsiElement> STRING_METHOD_ARGUMENT =
        PhpPatterns
            .phpLiteralExpression()
            .withParent(
                PlatformPatterns
                    .psiElement(ParameterList.class)
                    .withParent(
                        PhpPatterns
                            .phpFunctionReference()
                    )
            ).withLanguage(PhpLanguage.INSTANCE);
}
