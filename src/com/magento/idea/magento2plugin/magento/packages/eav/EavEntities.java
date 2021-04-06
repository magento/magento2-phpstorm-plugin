package com.magento.idea.magento2plugin.magento.packages.eav;

public enum EavEntities {
    PRODUCT("\\Magento\\Catalog\\Model\\Product");

    private String entityClass;

    EavEntities(String entityClass) {
        this.entityClass = entityClass;
    }

    public String getEntityClass() {
        return entityClass;
    }
}
