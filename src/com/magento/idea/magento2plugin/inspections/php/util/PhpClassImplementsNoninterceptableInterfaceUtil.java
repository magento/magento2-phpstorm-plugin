/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php.util;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PhpClassImplementsNoninterceptableInterfaceUtil {
    /**
     * Check whether class implements NoninterceptableInterface.
     *
     * @param phpClass PhpClass
     * @return bool
     */
    public static boolean execute(@NotNull PhpClass phpClass) {
        PhpClass[] interfaces = phpClass.getImplementedInterfaces();
        if (interfaces.length == 0) {
            return false;
        }
        for (PhpClass targetInterfaceClass: interfaces) {
            if (!targetInterfaceClass.getFQN().equals(Plugin.NON_INTERCEPTABLE_FQN)) {
                continue;
            }
            return true;
        }

        return false;
    }
}
