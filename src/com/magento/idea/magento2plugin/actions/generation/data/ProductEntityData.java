package com.magento.idea.magento2plugin.actions.generation.data;

import com.magento.idea.magento2plugin.magento.packages.eav.EavEntities;

public class ProductEntityData implements EavEntityDataInterface {
    private String group;
    private String code;
    private String type;
    private String label;
    private String input;
    private String source = null;
    private String scope;
    private boolean isRequired = false;
    private boolean isUsedInGrid = false;
    private boolean isVisibleInGrid = false;
    private boolean isFilterableInGrid = false;
    private boolean isVisible = true;
    private boolean isHtmlAllowedOnFront = false;
    private boolean isVisibleOnFront = false;
    private int sortOrder = 0;

    private String dataPatchName;
    private String namespace;
    private String directory;
    private String moduleName;

    public void setGroup(String group) {
        this.group = group;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public void setUsedInGrid(boolean usedInGrid) {
        isUsedInGrid = usedInGrid;
    }

    public void setVisibleInGrid(boolean visibleInGrid) {
        isVisibleInGrid = visibleInGrid;
    }

    public void setFilterableInGrid(boolean filterableInGrid) {
        isFilterableInGrid = filterableInGrid;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setHtmlAllowedOnFront(boolean htmlAllowedOnFront) {
        isHtmlAllowedOnFront = htmlAllowedOnFront;
    }

    public void setVisibleOnFront(boolean visibleOnFront) {
        isVisibleOnFront = visibleOnFront;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setDataPatchName(String dataPatchName) {
        this.dataPatchName = dataPatchName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getInput() {
        return input;
    }

    public String getGroup() {
        return group;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDirectory() {
        return directory;
    }

    public String getDataPatchName() {
        return dataPatchName;
    }

    public String getSource() {
        return source;
    }

    public String getScope() {
        return scope;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isUsedInGrid() {
        return isUsedInGrid;
    }

    public boolean isVisibleInGrid() {
        return isVisibleInGrid;
    }

    public boolean isFilterableInGrid() {
        return isFilterableInGrid;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isHtmlAllowedOnFront() {
        return isHtmlAllowedOnFront;
    }

    public boolean isVisibleOnFront() {
        return isVisibleOnFront;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    @Override
    public String getEntityClass() {
        return EavEntities.PRODUCT.getEntityClass();
    }
}
