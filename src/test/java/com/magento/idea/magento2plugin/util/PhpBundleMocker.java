/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for mocking the PhpBundle in tests.
 * <p>
 * This class provides methods to set up a mock for the PhpBundle,
 * which will return a fixed value for any key, avoiding the need for actual message keys in tests.
 */
public final class PhpBundleMocker {
    
    private static final String PHP_BUNDLE_NAME = "messages.PhpBundle";
    private static final String DEFAULT_MOCK_VALUE = "mocked value";
    
    private PhpBundleMocker() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Set up a mock for the PhpBundle that returns a fixed value for any key.
     * 
     * @param mockValue The value to return for any key
     * @throws Exception If an error occurs while setting up the mock
     */
    public static void mockPhpBundle(final String mockValue) throws Exception {
        // Create a mock bundle
        final ResourceBundle mockBundle = new ResourceBundleMock(mockValue);
        
        // Clear the ResourceBundle cache to ensure our mock is used
        clearResourceBundleCache();
        
        // Install our custom ResourceBundle.Control that returns the mock bundle for PhpBundle
        ResourceBundle.getBundle(PHP_BUNDLE_NAME, new ResourceBundle.Control() {
            @Override
            public ResourceBundle newBundle(
                    final String baseName,
                    final Locale locale,
                    final String format,
                    final ClassLoader loader,
                    final boolean reload
            ) throws IllegalAccessException, InstantiationException, IOException {
                if (PHP_BUNDLE_NAME.equals(baseName)) {
                    return mockBundle;
                }
                return super.newBundle(baseName, locale, format, loader, reload);
            }
        });
    }
    
    /**
     * Set up a mock for the PhpBundle that returns "mocked value for [key]" for any key.
     * 
     * @throws Exception If an error occurs while setting up the mock
     */
    public static void mockPhpBundle() throws Exception {
        mockPhpBundle(DEFAULT_MOCK_VALUE);
    }
    
    /**
     * Clear the ResourceBundle cache to ensure our mock is used.
     * 
     * @throws Exception If an error occurs while clearing the cache
     */
    private static void clearResourceBundleCache() throws Exception {
        try {
            // Get the cacheList field from ResourceBundle
            final Field cacheListField = ResourceBundle.class.getDeclaredField("cacheList");
            cacheListField.setAccessible(true);
            
            // Get the cache map
            final Map<?, ?> cacheList = (Map<?, ?>) cacheListField.get(null);
            
            // Clear the cache
            cacheList.clear();
        } catch (final NoSuchFieldException e) {
            // If cacheList field is not found, try the newer implementation (Java 9+)
            try {
                // Get the clearCache method
                final Method clearCacheMethod = 
                        ResourceBundle.class.getDeclaredMethod("clearCache");
                clearCacheMethod.setAccessible(true);
                
                // Call the method to clear the cache
                clearCacheMethod.invoke(null);
            } catch (final NoSuchMethodException e2) {
                throw new Exception("Failed to clear ResourceBundle cache", e2);
            }
        }
    }
}