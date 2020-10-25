/*
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
import com.magento.idea.magento2plugin.stubs.indexes.xml.AclResourceIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.DeclarativeSchemaElementsIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.PhpClassNameIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.UIComponentIndex;

@SuppressWarnings({"PMD.ClassNamingConventions", "PMD.UseUtilityClass"})
public class IndexManager {

    /**
     * Refresh Magento 2 indexes.
     */
    public static void manualReindex() {
        final ID<?, ?>[] indexIds = new ID<?, ?>[] {//NOPMD
            // php
            ModulePackageIndex.KEY,
            // xml|di configuration
            PluginIndex.KEY,
            VirtualTypeIndex.KEY,
            DeclarativeSchemaElementsIndex.KEY,
            // layouts
            BlockNameIndex.KEY,
            ContainerNameIndex.KEY,
            UIComponentIndex.KEY,
            // events
            EventNameIndex.KEY,
            EventObserverIndex.KEY,
            // webapi
            WebApiTypeIndex.KEY,
            ModuleNameIndex.KEY,
            PhpClassNameIndex.KEY,
            //acl
            AclResourceIndex.KEY,
            //menu
            MenuIndex.KEY,
            //require_js
            RequireJsIndex.KEY,
            MagentoLibJsIndex.KEY,
            // mftf
            ActionGroupIndex.KEY,
            DataIndex.KEY,
            PageIndex.KEY,
            SectionIndex.KEY,
            TestNameIndex.KEY,
            //graphql
            GraphQlResolverIndex.KEY
        };

        for (final ID<?, ?> id: indexIds) {
            try {
                FileBasedIndexImpl.getInstance().requestRebuild(id);
                FileBasedIndexImpl.getInstance().scheduleRebuild(id, new Throwable());//NOPMD
            } catch (NullPointerException exception) { //NOPMD
                //that's fine, indexer is not present in map java.util.Map.get
            }
        }
    }
}
