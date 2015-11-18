package com.magento.idea.magento2plugin.xml.layout.index;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
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

    @Override
    protected String getComponentName() {
        return "container";
    }
}