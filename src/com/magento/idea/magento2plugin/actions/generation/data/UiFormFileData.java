/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class UiFormFileData {
    private final String formName;
    private final String formArea;

    /**
     * UI Form data file constructor.
     *
     * @param formName String
     * @param formArea String
     */
    public UiFormFileData(
            final String formName,
            final String formArea
    ) {
        this.formName = formName;
        this.formArea = formArea;
    }

    /**
     * Get action directory.
     *
     * @return String
     */
    public String getFormName() {
        return formName;
    }

    /**
     * Get controller area.
     *
     * @return String
     */
    public String getFormArea() {
        return formArea;
    }
}
