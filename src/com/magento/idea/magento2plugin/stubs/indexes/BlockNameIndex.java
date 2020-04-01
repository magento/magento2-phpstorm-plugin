/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class BlockNameIndex extends NamedComponentIndex {
    public static final ID<String, Void> KEY = ID.create("com.magento.idea.magento2plugin.stubs.indexes.block_name");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new LayoutDataIndexer("block", "name");
    }
}
