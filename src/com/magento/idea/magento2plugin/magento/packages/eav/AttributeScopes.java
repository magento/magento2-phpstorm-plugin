package com.magento.idea.magento2plugin.magento.packages.eav;

public enum AttributeScopes {
    GLOBAL("\\Magento\\Eav\\Model\\Entity\\Attribute\\ScopedAttributeInterface::SCOPE_GLOBAL"),
    STORE("\\Magento\\Eav\\Model\\Entity\\Attribute\\ScopedAttributeInterface::SCOPE_STORE"),
    WEBSITE("\\Magento\\Eav\\Model\\Entity\\Attribute\\ScopedAttributeInterface::SCOPE_WEBSITE");

    private String scope;

    AttributeScopes(final String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}
