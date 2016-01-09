package com.magento.idea.magento2plugin.util;

import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import com.magento.idea.magento2plugin.php.index.ModulePackageFileBasedIndex;
import com.magento.idea.magento2plugin.xml.di.index.PluginToTypeFileBasedIndex;
import com.magento.idea.magento2plugin.xml.di.index.TypeConfigurationFileBasedIndex;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.BlockClassFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.BlockFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.ContainerFileBasedIndex;
import com.magento.idea.magento2plugin.xml.observer.index.EventObserverFileBasedIndex;
import com.magento.idea.magento2plugin.xml.observer.index.EventsDeclarationsFileBasedIndex;
import com.magento.idea.magento2plugin.xml.webapi.index.WebApiTypesFileBasedIndex;

/**
 * Created by dkvashnin on 1/9/16.
 */
public class IndexUtil {
    public static void manualReindex() {
        ID<?,?>[] indexIds = new ID<?,?>[] {
            // php
            ModulePackageFileBasedIndex.NAME,
            EventsDeclarationsFileBasedIndex.NAME,

            // xml configuration
            // di
            PluginToTypeFileBasedIndex.NAME,
            TypeConfigurationFileBasedIndex.NAME,
            VirtualTypesNamesFileBasedIndex.NAME,
            // layouts
            BlockClassFileBasedIndex.NAME,
            BlockFileBasedIndex.NAME,
            ContainerFileBasedIndex.NAME,
            // events
            EventObserverFileBasedIndex.NAME,
            // webapi
            WebApiTypesFileBasedIndex.NAME
        };

        for(ID<?,?> id: indexIds) {
            FileBasedIndexImpl.getInstance().requestRebuild(id);
            FileBasedIndexImpl.getInstance().scheduleRebuild(id, new Throwable());
        }
    }
}
