/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.php;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class MagentoTypeEscapeUtil {

    public static final String FACTORY_PROXY_TYPE_REGEX
            = "(Factory|\\\\Proxy|Factory\\\\Proxy)($|\\.)";
    public static final Pattern FACTORY_PROXY_TYPE_PATTERN
            = Pattern.compile(FACTORY_PROXY_TYPE_REGEX, Pattern.MULTILINE);
    public static final String JAVA_REFERENCE_SEPARATOR = ".";

    private MagentoTypeEscapeUtil() {
    }

    /**
     * Escape Magento Type (convert Factory and Proxy types to the simple type).
     *
     * @param typeFqn String
     *
     * @return String
     */
    public static @NotNull String escape(final @NotNull String typeFqn) {
        final Matcher matcher = FACTORY_PROXY_TYPE_PATTERN.matcher(typeFqn);

        String result = typeFqn;

        while (matcher.find()) {
            final int begin = matcher.start(0);
            final int end = matcher.end(0);

            if (begin < 0 || begin > end || end > result.length()) {
                continue;
            }
            result = result.substring(0, begin) + result.substring(end);
        }

        return typeFqn.equals(result) ? escapeProperty(typeFqn) : escapeProperty(result);
    }

    /**
     * Replace PHP property reference separator with the Intellij based separator.
     *
     * @param typeFqn String
     *
     * @return String
     */
    public static @NotNull String escapeProperty(final @NotNull String typeFqn) {
        if (MagentoReferenceUtil.isReference(typeFqn)) {
            return typeFqn.replace(
                    MagentoReferenceUtil.PHP_REFERENCE_SEPARATOR,
                    JAVA_REFERENCE_SEPARATOR
            );
        }

        return typeFqn;
    }
}
