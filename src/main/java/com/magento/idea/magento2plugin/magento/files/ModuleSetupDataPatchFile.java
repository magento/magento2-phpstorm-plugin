/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;
import org.jetbrains.annotations.NotNull;

public class ModuleSetupDataPatchFile implements ModuleFileInterface {

    public static final String FILE_SUFFIX = "Patch";
    public static final String EXTENSION = ".php";
    public static final String TEMPLATE = "Magento Module Setup Patch File";
    private final String className;

    /**
     * Constructor.
     *
     * @param className String
     */
    public ModuleSetupDataPatchFile(
            final @NotNull String className
    ) {
        this.className = resolveClassNameFromInput(className);
    }

    /**
     * Resolve class name from user input.
     *
     * @param input String
     *
     * @return String
     */
    public static String resolveClassNameFromInput(final String input) {
        if (input.length() <= FILE_SUFFIX.length()) {
            return input + FILE_SUFFIX;
        }
        final String suffix = input.substring(input.length() - FILE_SUFFIX.length());

        if (FILE_SUFFIX.equals(suffix)) {
            return input;
        } else {
            return input + FILE_SUFFIX;
        }
    }

    @Override
    public String getFileName() {
        return className + EXTENSION;
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
