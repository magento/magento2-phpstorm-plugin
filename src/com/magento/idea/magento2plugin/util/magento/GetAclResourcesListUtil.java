/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.AclResourceIndex;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GetAclResourcesListUtil {
    private static final String TEST_MODULE_ID_PART = "Magento_TestModule";
    private static final GetAclResourcesListUtil INSTANCE = new GetAclResourcesListUtil();

    /**
     * Get GetAclResourcesListUtil instance.
     *
     * @return GetAclResourcesUtil
     */
    public static GetAclResourcesListUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Get acl resources list.
     *
     * @param project Project
     *
     * @return List
     */
    public List<String> execute(final Project project) {
        final Collection<String> allAclResources =
                FileBasedIndex.getInstance().getAllKeys(AclResourceIndex.KEY, project);
        return sortAclResources(filterTestModules(allAclResources));
    }

    /**
     * Filter test modules from acl resources collection.
     *
     * @param allAclResources Collection
     *
     * @return List
     */
    private List<String> filterTestModules(final Collection<String> allAclResources) {
        return allAclResources
                .stream()
                .filter(aclResource -> !aclResource.contains(TEST_MODULE_ID_PART))
                .collect(Collectors.toList());
    }

    /**
     * Sort acl resources alphabetically.
     *
     * @param aclResources List
     *
     * @return List
     */
    private List<String> sortAclResources(final List<String> aclResources) {
        return aclResources.stream().sorted().collect(Collectors.toList());
    }
}
