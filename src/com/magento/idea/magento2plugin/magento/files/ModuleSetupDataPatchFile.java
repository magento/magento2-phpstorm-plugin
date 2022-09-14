/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import org.jetbrains.annotations.NotNull;

public class ModuleSetupDataPatchFile implements ModuleFileInterface {

    public static final String FILE_NAME = "Patch.php";
    public static final String TEMPLATE = "Magento Module Setup Patch File";
    private final String className;

    public ModuleSetupDataPatchFile(
            final @NotNull String className
    ) {
        this.className = className;
    }

    @Override
    public String getFileName() {
        return className + FILE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }
}
