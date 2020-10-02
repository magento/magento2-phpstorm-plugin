package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

import com.magento.idea.magento2plugin.util.RegExUtil;

public class AclResourceIdRule implements ValidationRule {
    public static final String MESSAGE = "validator.magentoAclResourceIdInvalid";
    private static final ValidationRule instance = new AclResourceIdRule();

    @Override
    public boolean check(String value) {
        return value.matches(RegExUtil.Magento.ACL_RESOURCE_ID);
    }

    public static ValidationRule getInstance() {
        return instance;
    }
}
