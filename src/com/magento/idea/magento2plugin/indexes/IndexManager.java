/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.util.indexing.FileBasedIndexImpl;
import com.intellij.util.indexing.ID;
import com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.EventObserverIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.ModulePackageIndex;
import com.magento.idea.magento2plugin.stubs.indexes.PluginIndex;
import com.magento.idea.magento2plugin.stubs.indexes.VirtualTypeIndex;
import com.magento.idea.magento2plugin.stubs.indexes.WebApiTypeIndex;
import com.magento.idea.magento2plugin.stubs.indexes.graphql.GraphQlResolverIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.MagentoLibJsIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.RequireJsIndex;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.ActionGroupIndex;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.DataIndex;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.PageIndex;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.SectionIndex;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.TestNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.PhpClassNameIndex;

public class IndexManager {
    public static void manualReindex() {
        ID<?, ?>[] indexIds = new ID<?, ?>[] {
            ModulePackageIndex.KEY,
            PluginIndex.KEY,
            VirtualTypeIndex.KEY,
            BlockNameIndex.KEY,
            ContainerNameIndex.KEY,
            EventNameIndex.KEY,
            EventObserverIndex.KEY,
            WebApiTypeIndex.KEY,
            ModuleNameIndex.KEY,
            PhpClassNameIndex.KEY,
            RequireJsIndex.KEY,
            MagentoLibJsIndex.KEY,
            ActionGroupIndex.KEY,
            DataIndex.KEY,
            PageIndex.KEY,
            SectionIndex.KEY,
            TestNameIndex.KEY,
            GraphQlResolverIndex.KEY
        };

        for (ID<?, ?> id: indexIds) {
            try {
                FileBasedIndexImpl.getInstance().requestRebuild(id);
                FileBasedIndexImpl.getInstance().scheduleRebuild(id, new Throwable());
            } catch (NullPointerException exception) {
                //that's fine, indexer is not present in map java.util.Map.get
            }
        }
    }
}
