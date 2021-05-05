/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.plugin;

import com.jetbrains.php.lang.psi.elements.Method;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;

public final class IsPluginAllowedForMethodUtil {

    private IsPluginAllowedForMethodUtil() {}

    /**
     * Checks whether a plugin allowed for a method.
     *
     * @param targetMethod Method
     * @return boolean
     */
    public static boolean check(final Method targetMethod) {
        final String targetMethodName = targetMethod.getName();
        if (targetMethodName.equals(MagentoPhpClass.CONSTRUCT_METHOD_NAME)) {
            return false;
        }
        if (targetMethod.isFinal()) {
            return false;
        }
        if (targetMethod.isStatic()) {
            return false;
        }
        return targetMethod.getAccess().toString().equals(AbstractPhpFile.PUBLIC_ACCESS);
    }
}
