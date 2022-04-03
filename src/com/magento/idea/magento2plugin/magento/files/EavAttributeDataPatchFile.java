/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

public class EavAttributeDataPatchFile extends AbstractPhpFile {
    public static final String HUMAN_READABLE_NAME = "Eav Attribute Data Patch Class";
    public static final String TEMPLATE = "Magento Eav Attribute Data Patch Class";
    public static final String DEFAULT_DIR = "Setup/Patch/Data";

    /**
     * Constructor.
     *
     * @param className String
     */
    public EavAttributeDataPatchFile(final String moduleName, final String className) {
        super(moduleName, className);
    }

    @Override
    public String getDirectory() {
        return DEFAULT_DIR;
    }

    @Override
    public String getHumanReadableName() {
        return HUMAN_READABLE_NAME;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
}
