/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class AclXmlData {
    private final String parentResourceId;
    private final String resourceId;
    private final String resourceTitle;

    /**
     * ACL XML data.
     *
     * @param parentResourceId String
     * @param resourceId String
     * @param resourceTitle String
     */
    public AclXmlData(
            final String parentResourceId,
            final String resourceId,
            final String resourceTitle
    ) {
        this.parentResourceId = parentResourceId;
        this.resourceId = resourceId;
        this.resourceTitle = resourceTitle;
    }

    public String getParentResourceId() {
        return parentResourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceTitle() {
        return resourceTitle;
    }
}
