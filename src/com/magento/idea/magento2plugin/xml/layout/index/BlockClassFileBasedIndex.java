package com.magento.idea.magento2plugin.xml.layout.index;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/20/15.
 */
public class BlockClassFileBasedIndex extends AbstractComponentNameFileBasedIndex {
    public static final ID<String, Void> NAME = ID.create("com.magento.idea.magento2plugin.xml.layout.index.block_class");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new LayoutDataIndexer("block", "class");
    }
}
