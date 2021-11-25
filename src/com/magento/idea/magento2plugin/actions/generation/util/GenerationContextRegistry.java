/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.util;

import com.magento.idea.magento2plugin.actions.generation.context.GenerationContext;

/**
 * This class used to share some generation context data through
 * generation lifetime to not overweight data objects.
 */
public final class GenerationContextRegistry {

    private static final GenerationContextRegistry INSTANCE = new GenerationContextRegistry();
    private GenerationContext context;

    private GenerationContextRegistry() {}

    /**
     * Get generation context registry.
     *
     * @return GenerationContextRegistry
     */
    public static GenerationContextRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Set context.
     *
     * @param context GenerationContext
     */
    public void setContext(final GenerationContext context) {
        this.context = context;
    }

    /**
     * Get context.
     *
     * @return GenerationContext
     */
    public GenerationContext getContext() {
        return context;
    }
}
