/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.validator;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Contributes class existence checks from optional language plugins (e.g. PHP plugin)
 * to shared XML inspections.
 */
public interface ClassExistenceValidator {
    ExtensionPointName<ClassExistenceValidator> EP_NAME = ExtensionPointName.create(
            "com.magento.idea.magento2plugin.classExistenceValidator"
    );

    boolean classExists(@NotNull Project project, @NotNull String fqn);

    static boolean isAvailable() {
        return !EP_NAME.getExtensionList().isEmpty();
    }

    static boolean isClassDeclared(@NotNull Project project, @NotNull String fqn) {
        for (ClassExistenceValidator validator : EP_NAME.getExtensionList()) {
            if (validator.classExists(project, fqn)) {
                return true;
            }
        }
        return false;
    }
}
