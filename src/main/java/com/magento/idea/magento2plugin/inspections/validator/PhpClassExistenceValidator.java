/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.validator;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class PhpClassExistenceValidator implements InspectionValidator, ClassExistenceValidator {

    private final PhpIndex phpIndex;

    public PhpClassExistenceValidator() {
        phpIndex = null;
    }

    public PhpClassExistenceValidator(final @NotNull Project project) {
        phpIndex = PhpIndex.getInstance(project);
    }

    @Override
    public boolean validate(final String value) {
        if (value == null || phpIndex == null) {
            return false;
        }
        final @NotNull Collection<PhpClass> classes = phpIndex.getClassesByFQN(value);
        final @NotNull Collection<PhpClass> interfaces = phpIndex.getInterfacesByFQN(value);

        return !classes.isEmpty() || !interfaces.isEmpty();
    }

    @Override
    public boolean classExists(final @NotNull Project project, final @NotNull String fqn) {
        final PhpIndex index = PhpIndex.getInstance(project);
        final Collection<PhpClass> classes = index.getClassesByFQN(fqn);
        final Collection<PhpClass> interfaces = index.getInterfacesByFQN(fqn);
        return !classes.isEmpty() || !interfaces.isEmpty();
    }
}
