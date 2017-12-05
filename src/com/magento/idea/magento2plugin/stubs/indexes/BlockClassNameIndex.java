package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.lang.PhpLangUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/20/15.
 */
public class BlockClassNameIndex extends NamedComponentIndex {
    public static final ID<String, Void> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.block_class_name");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new LayoutDataIndexer("block", "class", PhpLangUtil::toPresentableFQN);
    }
}
