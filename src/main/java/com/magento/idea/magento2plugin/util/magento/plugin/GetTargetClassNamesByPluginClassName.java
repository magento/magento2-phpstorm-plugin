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
 * Returns all targets class names for the plugin.
 */
public class GetTargetClassNamesByPluginClassName {
    private final Project project;

    public GetTargetClassNamesByPluginClassName(final Project project) {
        this.project = project;
    }

    /**
     * Get current class names for the plugin.
     *
     * @param currentClassName plugin class name.
     * @return list of plugin classes
     */
    public List<String> execute(final String currentClassName) {
        final List<String> targetClassNames = new ArrayList<>();
        final Collection<String> allKeys = FileBasedIndex.getInstance()
                .getAllKeys(PluginIndex.KEY, project);

        for (final String targetClassName : allKeys) {
            final List<Set<PluginData>> pluginsList = FileBasedIndex.getInstance()
                    .getValues(
                        PluginIndex.KEY,
                        targetClassName,
                        GlobalSearchScope.allScope(project)
                    );
            if (pluginsList.isEmpty()) {
                continue;
            }
            for (final Set<PluginData> plugins : pluginsList) {
                for (final PluginData plugin : plugins) {
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
