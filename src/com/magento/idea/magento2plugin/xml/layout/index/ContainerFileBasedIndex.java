package com.magento.idea.magento2plugin.xml.layout.index;

import com.intellij.util.indexing.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class ContainerFileBasedIndex extends AbstractComponentNameFileBasedIndex {
    public static final ID<String, Void> NAME = ID.create("com.magento.idea.magento2plugin.xml.layout.index.container_name");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new LayoutDataIndexer("container", "name");
    }

    @Override
    public int getVersion() {
        return 1;
    }
}