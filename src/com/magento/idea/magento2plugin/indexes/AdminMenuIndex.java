/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.AdminMenuIndexer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AdminMenuIndex {
    private static AdminMenuIndex INSTANCE;

    private Project project;

    private AdminMenuIndex() {
    }

    /**
     * Return admin menu item index class instance.
     *
     * @param project Project
     * @return AdminMenuIndex instance
     */
    public static AdminMenuIndex getInstance(final Project project) {
        if (null == INSTANCE) {
            INSTANCE = new AdminMenuIndex();
        }
        INSTANCE.project = project;

        return INSTANCE;
    }

    /**
     * Get admin menu items.
     *
     * @return List<String> List of menu items
     */
    public List<String> getAdminMenuItems() {
        FileBasedIndex index = FileBasedIndex.getInstance();

        List<String> adminMenu = new ArrayList<>(index.getAllKeys(
                AdminMenuIndexer.KEY,
                project)
        );
        Collections.sort(adminMenu);

        return adminMenu;
    }
}
