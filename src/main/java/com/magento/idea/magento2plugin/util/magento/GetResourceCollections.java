/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({
        "PMD.NonThreadSafeSingleton",
        "PMD.FieldNamingConventions",
        "PMD.RedundantFieldInitializer"
})
public class GetResourceCollections {
    private static final String ABSTRACT_COLLECTION_FQN =
            "Magento\\Framework\\Model\\ResourceModel\\Db\\Collection\\AbstractCollection";
    private static GetResourceCollections INSTANCE = null;
    private Project project;

    /**
     * Get instance of a class.
     *
     * @param project Project
     *
     * @return GetResourceCollections
     */
    public static GetResourceCollections getInstance(final Project project) {
        if (null == INSTANCE) {
            INSTANCE = new GetResourceCollections();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    /**
     * Get Magento resource collection list.
     *
     * @return List
     */
    public List<PhpClass> execute() {
        final PhpIndex phpIndex = PhpIndex.getInstance(project);
        final Collection<PhpClass> collections = phpIndex.getAllSubclasses(ABSTRACT_COLLECTION_FQN);

        if (collections.isEmpty()) {
            return null;
        }

        final List<PhpClass> resourceCollectionList = new ArrayList<>();
        resourceCollectionList.addAll(collections);

        return resourceCollectionList;
    }
}
