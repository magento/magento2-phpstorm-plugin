/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.indexes.LayoutIndex;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/18/15.
 */
public abstract class NamedComponentIndex extends ScalarIndexExtension<String> {
    private EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return LayoutIndex::isLayoutFile;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
