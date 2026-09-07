/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.php.lang.PhpFileType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventNameIndexTest {

    @Test
    public void generatedMetadataPhpFilesAreExcludedFromIndexing() {
        final EventNameIndex index = new EventNameIndex();
        final VirtualFile file = new PhpVirtualFile(
                "primary|global|webapi_rest|plugin-list.php",
                "//wsl.localhost/Ubuntu/home/pf/projects/mag/src/generated/metadata/"
                        + "primary|global|webapi_rest|plugin-list.php"
        );

        assertFalse(index.getInputFilter().acceptInput(file));
    }

    @Test
    public void regularPhpFilesRemainIndexable() {
        final EventNameIndex index = new EventNameIndex();
        final VirtualFile file = new PhpVirtualFile(
                "Block.php",
                "/var/www/magento/app/code/Vendor/Module/Block/Block.php"
        );

        assertTrue(index.getInputFilter().acceptInput(file));
    }

    private static final class PhpVirtualFile extends LightVirtualFile {
        private final String path;

        private PhpVirtualFile(final String name, final String path) {
            super(name, PhpFileType.INSTANCE, "<?php");
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public FileType getFileType() {
            return PhpFileType.INSTANCE;
        }
    }
}
