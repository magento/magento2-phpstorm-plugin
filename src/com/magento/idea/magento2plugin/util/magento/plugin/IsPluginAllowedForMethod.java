/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util.magento.plugin;

import com.jetbrains.php.lang.psi.elements.Method;
import com.magento.idea.magento2plugin.magento.files.Plugin;

public class IsPluginAllowedForMethod {
    private static IsPluginAllowedForMethod INSTANCE = null;

    public static IsPluginAllowedForMethod getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new IsPluginAllowedForMethod();
        }
        return INSTANCE;
    }

    public boolean check(Method targetMethod) {
        String targetMethodName = targetMethod.getName();
        if (targetMethodName.equals(Plugin.constructMethodName)) {
            return false;
        }
        if (targetMethod.isFinal()) {
            return false;
        }
        if (targetMethod.isStatic()) {
            return false;
        }
        if (!targetMethod.getAccess().toString().equals(Plugin.publicAccess)) {
            return false;
        }

        return true;
    }
}
