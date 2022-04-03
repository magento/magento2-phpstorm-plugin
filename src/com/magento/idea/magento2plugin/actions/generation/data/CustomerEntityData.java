/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.magento.idea.magento2plugin.magento.packages.eav.EavEntity;
import java.util.Map;

@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessivePublicCount"})
public class CustomerEntityData implements EavEntityDataInterface {

    private String code;
    private String type;
    private String label;
    private String input;
    private String namespace;
    private String moduleName;
    private String directory;
    private String dataPatchName;
    private String source;
    private int sortOrder;
    private Map<Integer, String> options;
    private String model;
    private Map<Integer, String> optionsSortOrder;
    private boolean required;
    private boolean visible;
    private boolean userDefined;
    private boolean useInAdminhtmlCustomerForm;
    private boolean useInAdminhtmlCheckoutForm;
    private boolean useInCustomerAccountCreateForm;
    private boolean useInCustomerAccountEditForm;
    private boolean usedInGrid;
    private boolean visibleInGrid;
    private boolean filterableInGrid;
    private boolean system;

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
        this.model = model;
    }

    public void setUserDefined(final boolean userDefined) {
        this.userDefined = userDefined;
    }

    public void setUseInAdminhtmlCustomerForm(final boolean useInAdminhtmlCustomerForm) {
        this.useInAdminhtmlCustomerForm = useInAdminhtmlCustomerForm;
    }

    public void setUseInAdminhtmlCheckoutForm(final boolean useInAdminhtmlCheckoutForm) {
        this.useInAdminhtmlCheckoutForm = useInAdminhtmlCheckoutForm;
    }

    public void setUseInCustomerAccountCreateForm(final boolean useInCustomerAccountCreateForm) {
        this.useInCustomerAccountCreateForm = useInCustomerAccountCreateForm;
    }

    public void setUseInCustomerAccountEditForm(final boolean useInCustomerAccountEditForm) {
        this.useInCustomerAccountEditForm = useInCustomerAccountEditForm;
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

    public void setSystem(final boolean system) {
        this.system = system;
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
        return EavEntity.CUSTOMER.getEntityClass();
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getBackendModel() {
        return null;
    }

    @Override
    public int getSortOrder() {
        return sortOrder;
    }

    @Override
    public Map<Integer, String> getOptions() {
        return options;
    }

    public String getModel() {
        return model;
    }

    @Override
    public Map<Integer, String> getOptionsSortOrder() {
        return optionsSortOrder;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public boolean isUserDefined() {
        return userDefined;
    }

    public boolean isUseInAdminhtmlCustomerForm() {
        return useInAdminhtmlCustomerForm;
    }

    public boolean isUseInAdminhtmlCheckoutForm() {
        return useInAdminhtmlCheckoutForm;
    }

    public boolean isUseInCustomerAccountCreateForm() {
        return useInCustomerAccountCreateForm;
    }

    public boolean isUseInCustomerAccountEditForm() {
        return useInCustomerAccountEditForm;
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

    public boolean isSystem() {
        return system;
    }
}
