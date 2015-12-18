package com.magento.idea.magento2plugin.php.util;

import com.magento.idea.magento2plugin.util.PsiContextMatcherI;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 12/18/15.
 */
public class PsiContextMatcherManager {
    private static PsiContextMatcherManager INSTANCE;

    private Map<String, PsiContextMatcherI> map = new HashMap<>();

    private PsiContextMatcherManager() {}

    public ImplementationMatcher getImplementationMatcherForType(String type) {
        if (map.containsKey(type)) {

            PsiContextMatcherI psiContextMatcherI = map.get(type);
            if (psiContextMatcherI instanceof ImplementationMatcher) {
                return (ImplementationMatcher)psiContextMatcherI;
            }
        }
        ImplementationMatcher implementationMatcher = new ImplementationMatcher(type);
        map.put(type, implementationMatcher);

        return implementationMatcher;
    }

    public static PsiContextMatcherManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PsiContextMatcherManager();
        }

        return INSTANCE;
    }
}
