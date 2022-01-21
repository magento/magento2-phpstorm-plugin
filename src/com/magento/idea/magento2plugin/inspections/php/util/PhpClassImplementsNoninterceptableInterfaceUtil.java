/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php.util;

import com.intellij.util.SlowOperations;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PhpClassImplementsNoninterceptableInterfaceUtil {

    private PhpClassImplementsNoninterceptableInterfaceUtil() {}

    /**
     * Check whether class implements NoninterceptableInterface.
     *
     * @param phpClass PhpClass
     *
     * @return bool
     */
    public static boolean execute(final @NotNull PhpClass phpClass) {
        final PhpClass[] interfaces = SlowOperations.allowSlowOperations(
                phpClass::getImplementedInterfaces
        );

        if (interfaces.length == 0) {
            return false;
        }

        for (final PhpClass targetInterfaceClass: interfaces) {
            if (Plugin.NON_INTERCEPTABLE_FQN.equals(targetInterfaceClass.getFQN())) {
                return true;
            }
        }

        return false;
    }
}
