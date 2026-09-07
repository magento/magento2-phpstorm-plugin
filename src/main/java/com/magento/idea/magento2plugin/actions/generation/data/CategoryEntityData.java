/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.magento.idea.magento2plugin.magento.packages.eav.EavEntity;
import java.util.Map;

@SuppressWarnings({"PMD.TooManyFields"})
public class CategoryEntityData implements EavEntityDataInterface {
    private String group;
    private String code;
    private String type;
    private String label;
    private String input;
    private String source;
    private String scope;
    private String dataPatchName;
    private String namespace;
    private String directory;
    private String moduleName;
    private String backendModel;
    private int sortOrder;
    private boolean required;
    private boolean visible;
    private Map<Integer, String> options;
    private Map<Integer, String> optionsSortOrder;

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
    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    @Override
    public void setDataPatchName(final String dataPatchName) {
        this.dataPatchName = dataPatchName;
    }

    @Override
    public void setSource(final String source) {
        this.source = source;
    }

    @Override
    public void setSortOrder(final int sortOrder) {
        this.sortOrder = sortOrder;
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
    public void setRequired(final boolean required) {
        this.required = required;
    }

    @Override
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setBackendModel(final String model) {
        this.backendModel = model;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public void setScope(final String scope) {
        this.scope = scope;
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
        return directory;
    }

    @Override
    public String getDataPatchName() {
        return dataPatchName;
    }

    @Override
    public String getEntityClass() {
        return EavEntity.CATEGORY.getEntityClass();
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getBackendModel() {
        return backendModel;
    }

    @Override
    public int getSortOrder() {
        return sortOrder;
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
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public String getGroup() {
        return group;
    }

    public String getScope() {
        return scope;
    }
}
