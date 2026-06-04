/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.magento.idea.magento2plugin.stubs.indexes.ModulePackageIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.JsMixinIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.KnockoutTemplateIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.MagentoLibJsIndex;
import com.magento.idea.magento2plugin.stubs.indexes.js.RequireJsIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ThemeXmlIndex;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"PMD.ClassNamingConventions", "PMD.UseUtilityClass"})
public class IndexManager {
    private static final Logger LOGGER = Logger.getInstance(IndexManager.class);

    private static final String[] OPTIONAL_INDEX_CLASSES = {
            "com.magento.idea.magento2plugin.stubs.indexes.PluginIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.VirtualTypeIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.DeclarativeSchemaElementsIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.SystemXmlSectionIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.SystemXmlGroupIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.SystemXmlFieldIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.BlockNameIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.ContainerNameIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.UIComponentIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.EventObserverIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.WebApiTypeIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.PhpClassNameIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.AclResourceIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.MenuIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.mftf.ActionGroupIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.mftf.DataIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.mftf.PageIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.mftf.SectionIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.mftf.TestNameIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.mftf.TestExtendsIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.graphql.GraphQlResolverIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.ProductTypeIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.LayoutBlockTemplateIndex",
            "com.magento.idea.magento2plugin.stubs.indexes.xml.LayoutTemplateBlockIndex"
    };

    /**
     * Refresh Magento 2 indexes.
     */
    public static void manualReindex() {
        LOGGER.info("Magento manual reindex requested");
        final List<ID<?, ?>> indexIds = new ArrayList<>(List.of(
                ModulePackageIndex.KEY,
                ModuleXmlIndex.KEY,
                ThemeXmlIndex.KEY,
                RequireJsIndex.KEY,
                JsMixinIndex.KEY,
                KnockoutTemplateIndex.KEY,
                MagentoLibJsIndex.KEY
        ));
        indexIds.addAll(getOptionalIndexIds());

        for (final ID<?, ?> id : indexIds) {
            try {
                FileBasedIndex.getInstance().requestRebuild(id);
                LOGGER.info("Magento index rebuild requested: " + id.getName());
            } catch (NullPointerException exception) { //NOPMD
                LOGGER.info("Magento index is not registered in this IDE, skipping rebuild: " + id.getName());
            } catch (RuntimeException exception) { //NOPMD
                LOGGER.warn("Unable to request Magento index rebuild: " + id.getName(), exception);
            }
        }
        LOGGER.info("Magento manual reindex request completed");
    }

    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "unchecked"})
    private static List<ID<?, ?>> getOptionalIndexIds() {
        final List<ID<?, ?>> indexIds = new ArrayList<>();

        for (final String className : OPTIONAL_INDEX_CLASSES) {
            try {
                final Class<?> indexClass = Class.forName(className);
                final Field keyField = indexClass.getField("KEY");
                indexIds.add((ID<?, ?>) keyField.get(null));
            } catch (Throwable ignored) { //NOPMD
                // Optional IDE/plugin dependencies are absent. Skip their indexes.
            }
        }

        return indexIds;
    }
}
