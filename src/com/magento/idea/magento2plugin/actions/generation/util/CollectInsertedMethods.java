/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectInsertedMethods {
    private static CollectInsertedMethods INSTANCE = null;

    public static CollectInsertedMethods getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new CollectInsertedMethods();
        }
        return INSTANCE;
    }

    @Nullable
    public List<PsiElement> execute(@NotNull PsiFile file, @NotNull CharSequence className, @NotNull Set<CharSequence> methodNames) {
        if (!(file instanceof PhpFile)) {
            return null;
        }
        PhpClass phpClass = PhpPsiUtil.findClass((PhpFile) file, (aClass) -> PhpLangUtil.equalsClassNames(aClass.getNameCS(), className));
        if (phpClass == null) {
            return null;
        } else {
            List<PsiElement> insertedMethods = new ArrayList();
            Method[] ownMethods = phpClass.getOwnMethods();

            for (Method method : ownMethods) {
                if (methodNames.contains(method.getNameCS())) {
                    insertedMethods.add(method);
                }
            }

            return insertedMethods;
        }
    }
}
