/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.php;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import java.util.Collection;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;

public final class ReferenceResolverUtil {

    private ReferenceResolverUtil() {
    }

    /**
     * Resolve reference.
     *
     * @param reference PhpReference
     *
     * @return PsiElement
     */
    public static PsiElement resolve(final @NotNull PhpReference reference) {
        final String fqn = reference.getFQN();

        if (fqn == null) {
            return null;
        }
        PsiElement resolved = null;

        if (isFactoryOrProxy(fqn)) {
            final Collection<PhpClass> classes = PhpIndex.getInstance(reference.getProject())
                    .getAnyByFQN(
                            MagentoTypeEscapeUtil.escape(reference.getFQN())
                    );

            if (!classes.isEmpty()) {
                resolved = classes.stream().iterator().next();
            }
        } else {
            resolved = reference.resolve();
        }

        return resolved;
    }

    /**
     * Check if provided FQN is a Factory or Proxy.
     *
     * @param fqn String
     *
     * @return boolean
     */
    private static boolean isFactoryOrProxy(final @NotNull String fqn) {
        final Matcher matcher = MagentoTypeEscapeUtil.FACTORY_PROXY_TYPE_PATTERN.matcher(fqn);

        return matcher.find();
    }
}
