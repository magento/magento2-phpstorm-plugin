/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util.php;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.elements.ParameterList;

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
