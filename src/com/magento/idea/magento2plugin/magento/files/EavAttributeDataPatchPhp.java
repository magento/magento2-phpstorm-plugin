package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class EavAttributeDataPatchPhp implements ModuleFileInterface {
    public static final String TEMPLATE = "Magento Eav Attribute Data Patch Class";
    public static final String DEFAULT_DIR = "Setup/Patch/Data";
    private static EavAttributeDataPatchPhp INSTANCE = null;
    private String fileName;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static EavAttributeDataPatchPhp getInstance(final String className) {
        if (null == INSTANCE) {
            INSTANCE = new EavAttributeDataPatchPhp();
        }

        INSTANCE.setFileName(className.concat(".php"));

        return INSTANCE;
    }

    @Override
    public String getFileName() {
        return fileName;
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
