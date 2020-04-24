/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.CronGroupIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CronGroupIndex {

    private static CronGroupIndex INSTANCE;

    private Project project;

    private CronGroupIndex() {}

    public static CronGroupIndex getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new CronGroupIndex();
        }

        INSTANCE.project = project;

        return INSTANCE;
    }

    /**
     * Retrieve list of cron groups
     *
     * @return List<String>
     */
    public List<String> getGroups() {
        FileBasedIndex index = FileBasedIndex.getInstance();

        List<String> cronGroups = new ArrayList<>(index.getAllKeys(CronGroupIndexer.KEY, project));

        // todo: needs to show custom cron groups first and only than groups from bundled core modules
        Collections.sort(cronGroups);

       return cronGroups;
    }
}
