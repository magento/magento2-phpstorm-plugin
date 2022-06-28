/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex;
import com.magento.idea.magento2plugin.stubs.indexes.data.PluginData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Returns all targets class names for the plugin
 */
public class GetTargetClassNamesByPluginClassName {
    private static GetTargetClassNamesByPluginClassName INSTANCE = null;
    private Project project;

    public static GetTargetClassNamesByPluginClassName getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new GetTargetClassNamesByPluginClassName();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    public ArrayList<String> execute(String currentClassName) {
        ArrayList<String> targetClassNames = new ArrayList<>();
        Collection<String> allKeys = FileBasedIndex.getInstance()
                .getAllKeys(PluginIndex.KEY, project);

        for (String targetClassName : allKeys) {
            List<Set<PluginData>> pluginsList = FileBasedIndex.getInstance()
                    .getValues(com.magento.idea.magento2plugin.stubs.indexes.PluginIndex.KEY, targetClassName, GlobalSearchScope.allScope(project));
            if (pluginsList.isEmpty()) {
                continue;
            }
            for (Set<PluginData> plugins : pluginsList) {
                for (PluginData plugin : plugins) {
                    if (!plugin.getType().equals(currentClassName)) {
                        continue;
                    }
                    targetClassNames.add(targetClassName);
                }
            }
        }

        return targetClassNames;
    }
}
