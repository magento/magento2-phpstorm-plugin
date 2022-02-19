/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.eav;

public enum CustomerForm {

    ADMINHTML_CHECKOUT("adminhtml_checkout"),
    ADMINHTML_CUSTOMER("adminhtml_customer"),
    ADMINHTML_CUSTOMER_ADDRESS("adminhtml_customer_address"),
    CUSTOMER_ACCOUNT_CREATE("customer_account_create"),
    CUSTOMER_ACCOUNT_EDIT("customer_account_edit"),
    CUSTOMER_ADDRESS_EDIT("customer_address_edit"),
    CUSTOMER_REGISTER_ADDRESS("customer_register_address");

    private final String formCode;

    CustomerForm(final String formCode) {
        this.formCode = formCode;
    }

    public String getFormCode() {
        return this.formCode;
    }
}
