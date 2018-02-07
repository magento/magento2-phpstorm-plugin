package com.magento.idea.magento2plugin.indexes;

import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpClassNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.*;

/**
 * Created by dkvashnin on 1/9/16.
 */
public class IndexManager {
    public static void manualReindex() {
        ID<?, ?>[] indexIds = new ID<?, ?>[] {
            // php
            ModulePackageIndex.KEY,
            // xml|di configuration
            PluginIndex.KEY,
            VirtualTypeIndex.KEY,
            // layouts
            BlockNameIndex.KEY,
            ContainerNameIndex.KEY,
            // events
            EventNameIndex.KEY,
            EventObserverIndex.KEY,
            // webapi
            WebApiTypeIndex.KEY,
            ModuleNameIndex.KEY,
            PhpClassNameIndex.KEY
        };

        for (ID<?, ?> id: indexIds) {
            FileBasedIndexImpl.getInstance().requestRebuild(id);
            FileBasedIndexImpl.getInstance().scheduleRebuild(id, new Throwable());
        }
    }
}
