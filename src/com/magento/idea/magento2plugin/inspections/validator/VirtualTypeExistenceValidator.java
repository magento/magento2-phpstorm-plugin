/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.validator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.VirtualTypeIndex;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class VirtualTypeExistenceValidator implements InspectionValidator {

    private final Project project;

    public VirtualTypeExistenceValidator(final @NotNull Project project) {
        this.project = project;
    }

    @Override
    public boolean validate(final String value) {
        if (value == null) {
            return false;
        }
        final @NotNull Collection<String> virtualTypes = FileBasedIndex.getInstance().getValues(
                VirtualTypeIndex.KEY,
                value,
                GlobalSearchScope.allScope(project)
        );

        return !virtualTypes.isEmpty();
    }
}
