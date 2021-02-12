/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.ui;

/**
 * Data Models for storing ComboBox UI component item data.
 */
public class ComboBoxItemData {
    private final String key;
    private final String text;

    /**
     * Constructor.
     *
     * @param key String
     * @param text String
     */
    public ComboBoxItemData(final String key, final String text) {
        this.key = key;
        this.text = text;
    }

    /**
     * Get key.
     *
     * @return String
     */
    public String getKey() {
        return key;
    }

    /**
     * Get Text.
     *
     * @return String
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return this.getText();
    }
}
