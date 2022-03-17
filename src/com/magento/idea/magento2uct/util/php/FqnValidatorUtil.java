/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.php;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.Collection;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;

public final class FqnValidatorUtil {

    private FqnValidatorUtil() {}

    /**
     * Check if provided string is a valid FQN.
     *
     * @param fqnCandidate String
     * @param project Project
     *
     * @return boolean
     */
    public static boolean validate(
            final @NotNull String fqnCandidate,
            final @NotNull Project project
    ) {
        String safeFqn = MagentoTypeEscapeUtil.escapeProperty(fqnCandidate);

        if (isFactoryOrProxy(safeFqn)) {
            safeFqn = MagentoTypeEscapeUtil.escape(safeFqn);
        }
        final Collection<PhpClass> classes = PhpIndex.getInstance(project).getAnyByFQN(safeFqn);

        return !classes.isEmpty();
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
