/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.php.util;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

public class PhpClassImplementsInterfaceUtil {

    public static boolean execute(@NotNull PhpClass interfaceClass, @NotNull PhpClass subClass) {
        if (!interfaceClass.isInterface()) {
            return false;
        }
        PhpClass[] interfaces = subClass.getImplementedInterfaces();
        if (interfaces.length == 0) {
            return false;
        }
        for (PhpClass targetInterfaceClass: interfaces) {
            if (!targetInterfaceClass.getFQN().equals(interfaceClass.getFQN())) {
                continue;
            }
            return true;
        }

        return false;
    }
}
