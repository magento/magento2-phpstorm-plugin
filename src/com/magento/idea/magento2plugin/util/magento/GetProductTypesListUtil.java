/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ProductTypeIndex;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class GetProductTypesListUtil {

    private GetProductTypesListUtil() {}

    /**
     * Product types util.
     *
     * @param project Project
     * @return List
     */
    public static List<String> execute(final Project project) {
        final Collection<String> productTypesList =
                FileBasedIndex.getInstance().getAllKeys(ProductTypeIndex.KEY, project);

        return productTypesList.stream().sorted().collect(Collectors.toList());
    }
}
