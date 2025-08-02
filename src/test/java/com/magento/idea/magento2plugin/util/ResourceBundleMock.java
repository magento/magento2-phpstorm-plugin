/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Mock implementation of ResourceBundle for testing.
 * <p>
 * This class provides a dummy ResourceBundle that returns a fixed value for any key,
 * avoiding the need for actual message keys in tests.
 */
public class ResourceBundleMock extends ResourceBundle {
    
    private final String defaultValue;
    
    /**
     * Constructor with default value.
     * 
     * @param defaultValue String value to return for any key
     */
    public ResourceBundleMock(final String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * Default constructor that returns the key as the value.
     */
    public ResourceBundleMock() {
        this.defaultValue = null;
    }
    
    @Override
    protected Object handleGetObject(final String key) {
        return defaultValue != null ? defaultValue : "mocked value for " + key;
    }
    
    @Override
    public Enumeration<String> getKeys() {
        return Collections.emptyEnumeration();
    }
}