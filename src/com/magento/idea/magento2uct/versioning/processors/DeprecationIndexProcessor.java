/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class DeprecationIndexProcessor implements IndexProcessor<Map<String, Boolean>> {

    @Override
    public Map<String, Boolean> process(final @NotNull PsiFile file) {
        final Map<String, Boolean> data = new HashMap<>();

        if (!(file instanceof PhpFile)) {
            return data;
        }
        final PhpClass phpClass = GetFirstClassOfFile.getInstance().execute((PhpFile) file);

        if (phpClass == null) {
            return data;
        }

        if (phpClass.isDeprecated()) {
            data.put(phpClass.getFQN(), true);
        }

        for (final Field field : phpClass.getOwnFields()) {
            if (field.isDeprecated()) {
                data.put(field.getFQN(), true);
            }
        }

        for (final Method method : phpClass.getOwnMethods()) {
            if (method.isDeprecated()) {
                data.put(method.getFQN(), true);
            }
        }

        return data;
    }
}
