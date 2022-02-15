/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.CustomerEntityData;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile;
import com.magento.idea.magento2plugin.magento.files.CustomerEavAttributeDataPatchFile;
import com.magento.idea.magento2plugin.magento.packages.eav.CustomerForm;
import com.magento.idea.magento2plugin.magento.packages.eav.DataPatchDependency;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class CustomerEavAttributePatchGenerator extends EavAttributeSetupPatchGenerator {

    private final EavEntityDataInterface data;

    public CustomerEavAttributePatchGenerator(
            final @NotNull EavEntityDataInterface data,
            final Project project
    ) {
        this(data, project, true);
    }

    /**
     * Php file generator constructor.
     *
     * @param project                Project
     * @param checkFileAlreadyExists boolean
     */
    public CustomerEavAttributePatchGenerator(
            final @NotNull EavEntityDataInterface data,
            final @NotNull Project project,
            final boolean checkFileAlreadyExists
    ) {
        super(data, project, checkFileAlreadyExists);
        this.data = data;
    }

    @Override
    protected AbstractPhpFile initFile() {
        return new CustomerEavAttributeDataPatchFile(data.getModuleName(), data.getDataPatchName());
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        super.fillAttributes(attributes);
        phpClassTypesBuilder
                .append(
                        "EAV_CONFIG_CLASS",
                        DataPatchDependency.EAV_CONFIG.getClassPatch()
                ).append(
                        "ATTRIBUTE_RESOURCE",
                        DataPatchDependency.ATTRIBUTE_RESOURCE.getClassPatch()
                ).append(
                        "CUSTOMER_METADATA_INTERFACE",
                        DataPatchDependency.CUSTOMER_METADATA_INTERFACE.getClassPatch()
        );

        final String selectedCustomerForms = getFormsForAttribute((CustomerEntityData) data);

        if (!selectedCustomerForms.isEmpty()) {
            phpClassTypesBuilder.appendProperty("CUSTOMER_FORMS", selectedCustomerForms);
        }

        phpClassTypesBuilder.mergeProperties(attributes);

        attributes.setProperty(
                "USES",
                PhpClassGeneratorUtil.formatUses(phpClassTypesBuilder.getUses())
        );
    }

    private String getFormsForAttribute(final CustomerEntityData customerEntityData) {
        final List<String> usedInForms = new ArrayList<>();

        if (customerEntityData.isUseInAdminhtmlCustomerForm()) {
            usedInForms.add(
                    "'" + CustomerForm.ADMINHTML_CUSTOMER.getFormCode() + "'"
            );
        }

        if (customerEntityData.isUseInAdminhtmlCheckoutForm()) {
            usedInForms.add(
                    "'" + CustomerForm.ADMINHTML_CHECKOUT.getFormCode() + "'"
            );
        }

        if (customerEntityData.isUseInCustomerAccountCreateForm()) {
            usedInForms.add(
                    "'" + CustomerForm.CUSTOMER_ACCOUNT_CREATE.getFormCode() + "'"
            );
        }

        if (customerEntityData.isUseInCustomerAccountEditForm()) {
            usedInForms.add(
                    "'" + CustomerForm.CUSTOMER_ACCOUNT_EDIT.getFormCode() + "'"
            );
        }

        if (usedInForms.isEmpty()) {
            return "";
        }

        return String.join(",", usedInForms);
    }
}
