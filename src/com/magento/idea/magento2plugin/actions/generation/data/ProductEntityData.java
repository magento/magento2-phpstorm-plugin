/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.magento.idea.magento2plugin.magento.files.EavAttributeDataPatchFile;
import com.magento.idea.magento2plugin.magento.packages.eav.EavEntity;
import java.util.Map;

@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessivePublicCount"})
public class ProductEntityData implements EavEntityDataInterface {
    private String group;
    private String code;
    private String type;
    private String label;
    private String input;
    private String source;
    private String scope;
    private String applyTo;
    private String backendModel;
    private boolean required;
    private boolean usedInGrid;
    private boolean visibleInGrid;
    private boolean filterableInGrid;
    private boolean visible;
    private boolean htmlAllowedOnFront;
    private boolean visibleOnFront;
    private int sortOrder;
    private Map<Integer, String> options;
    private Map<Integer, String> optionsSortOrder;

    private String dataPatchName;
    private String namespace;
    private String directory;
    private String moduleName;

    /**
     * Constructor.
     */
    public ProductEntityData() {
        this.required = false;
        this.usedInGrid = false;
        this.visibleInGrid = false;
        this.filterableInGrid = false;
        this.visible = true;
        this.htmlAllowedOnFront = false;
        this.visibleOnFront = false;
        this.sortOrder = 0;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    @Override
    public void setCode(final String code) {
        this.code = code;
    }

    @Override
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public void setLabel(final String label) {
        this.label = label;
    }

    @Override
    public void setInput(final String input) {
        this.input = input;
    }

    @Override
    public void setSource(final String source) {
        this.source = source;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public void setApplyTo(final String applyTo) {
        this.applyTo = applyTo;
    }

    @Override
    public void setRequired(final boolean required) {
        this.required = required;
    }

    public void setUsedInGrid(final boolean usedInGrid) {
        this.usedInGrid = usedInGrid;
    }

    public void setVisibleInGrid(final boolean visibleInGrid) {
        this.visibleInGrid = visibleInGrid;
    }

    public void setFilterableInGrid(final boolean filterableInGrid) {
        this.filterableInGrid = filterableInGrid;
    }

    @Override
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setBackendModel(final String model) {
        this.backendModel = model;
    }

    public void setHtmlAllowedOnFront(final boolean htmlAllowedOnFront) {
        this.htmlAllowedOnFront = htmlAllowedOnFront;
    }

    public void setVisibleOnFront(final boolean visibleOnFront) {
        this.visibleOnFront = visibleOnFront;
    }

    @Override
    public void setSortOrder(final int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public void setDataPatchName(final String dataPatchName) {
        this.dataPatchName = dataPatchName;
    }

    @Override
    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    @Override
    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setOptions(final Map<Integer, String> options) {
        this.options = options;
    }

    @Override
    public void setOptionsSortOrder(final Map<Integer, String> optionsSortOrder) {
        this.optionsSortOrder = optionsSortOrder;
    }

    @Override
    public Map<Integer, String> getOptions() {
        return options;
    }

    @Override
    public Map<Integer, String> getOptionsSortOrder() {
        return optionsSortOrder;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getInput() {
        return input;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String getDirectory() {
        if (directory == null) {
            directory = EavAttributeDataPatchFile.DEFAULT_DIR;
        }

        return directory;
    }

    @Override
    public String getDataPatchName() {
        return dataPatchName;
    }

    @Override
    public String getEntityClass() {
        return EavEntity.PRODUCT.getEntityClass();
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getBackendModel() {
        return backendModel;
    }

    public String getScope() {
        return scope;
    }

    public String getApplyTo() {
        return applyTo;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    public boolean isUsedInGrid() {
        return usedInGrid;
    }

    public boolean isVisibleInGrid() {
        return visibleInGrid;
    }

    public boolean isFilterableInGrid() {
        return filterableInGrid;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public boolean isHtmlAllowedOnFront() {
        return htmlAllowedOnFront;
    }

    public boolean isVisibleOnFront() {
        return visibleOnFront;
    }

    @Override
    public int getSortOrder() {
        return sortOrder;
    }
}
