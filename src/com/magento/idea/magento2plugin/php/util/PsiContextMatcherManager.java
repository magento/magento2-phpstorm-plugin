package com.magento.idea.magento2plugin.php.util;

import com.magento.idea.magento2plugin.util.PsiContextMatcherI;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 12/18/15.
 */
public class PsiContextMatcherManager {
    private static PsiContextMatcherManager INSTANCE;

    private PsiContextMatcherManager() {}

    public ImplementationMatcher getImplementationMatcherForType(String type) {
        return new ImplementationMatcher(type);
    }

    public static PsiContextMatcherManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PsiContextMatcherManager();
        }

        return INSTANCE;
    }
}
