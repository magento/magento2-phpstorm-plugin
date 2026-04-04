/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.util;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.Collection;

public class GetPhpClassByFQN {
    private static GetPhpClassByFQN INSTANCE = null;
    private Project project;

    public static GetPhpClassByFQN getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new GetPhpClassByFQN();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    public PhpClass execute(String targetClassName) {
        return ReadAction.compute(() -> {
            final PhpIndex phpIndex = PhpIndex.getInstance(project);
            final Collection<PhpClass> interfaces = phpIndex.getInterfacesByFQN(targetClassName);
            if (!interfaces.isEmpty()) {
                return interfaces.iterator().next();
            }
            final Collection<PhpClass> classes = phpIndex.getClassesByFQN(targetClassName);
            if (classes.isEmpty()) {
                return null;
            }
            return classes.iterator().next();
        });
    }
}
