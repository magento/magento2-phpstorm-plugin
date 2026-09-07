/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import java.util.ArrayList;
import java.util.List;

public enum OverridableFileType {

    CSS("css"),
    LESS("less"),
    JS("js"),
    PHTML("phtml");

    private final String filetype;

    /**
     * Overridable File Type constructor.
     *
     * @param filetype String
     */
    OverridableFileType(final String filetype) {
        this.filetype = filetype;
    }

    /**
     * Get overridable file type.
     *
     * @return String
     */
    public String getType() {
        return filetype;
    }

    /**
     * Checks if file extension is js.
     *
     * @param filetype String
     * @return boolean
     */
    public static Boolean isFileJS(final String filetype) {
        return filetype.equals(OverridableFileType.JS.getType());
    }

    /**
     * Checks is file extension is phtml.
     *
     * @param fileType String
     * @return Boolean
     */
    public static Boolean isFilePhtml(final String fileType) {
        return fileType.equals(OverridableFileType.PHTML.getType());
    }

    /**
     * Checks is file extension is less or css.
     * @param fileType String
     * @return Boolean
     */
    public static Boolean isFileStyle(final String fileType) {
        Boolean result = false;
        final List<String> fileStyleTypes = getStyleFileTypes();
        for (final String styleType : fileStyleTypes) {
            if (styleType.equals(fileType)) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Get overridable file extensions.
     * @return List
     */
    public static List<String> getOverwritableFileExtensions() {
        final List fileTypes = new ArrayList<>();

        fileTypes.add(OverridableFileType.CSS.getType());
        fileTypes.add(OverridableFileType.LESS.getType());
        fileTypes.add(OverridableFileType.PHTML.getType());
        fileTypes.add(OverridableFileType.JS.getType());

        return fileTypes;
    }

    /**
     * Returns styles file extensions.
     * @return List
     */
    public static List<String> getStyleFileTypes() {
        final List styleFileTypes = new ArrayList<>();

        styleFileTypes.add(OverridableFileType.CSS.getType());
        styleFileTypes.add(OverridableFileType.LESS.getType());

        return styleFileTypes;
    }
}
