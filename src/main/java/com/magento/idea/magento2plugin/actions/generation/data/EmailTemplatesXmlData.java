/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class EmailTemplatesXmlData {
    private final String module;
    private final String emailTemplateId;
    private final String label;
    private final String templateFileName;
    private final String type;
    private final String area;

    /**
     * Email templates data constructor.
     *
     * @param module String
     * @param emailTemplateId String
     * @param label String
     * @param templateFileName String
     * @param type String
     * @param area String
     */
    public EmailTemplatesXmlData(
            final String module,
            final String emailTemplateId,
            final String label,
            final String templateFileName,
            final String type,
            final String area
    ) {
        this.module = module;
        this.emailTemplateId = emailTemplateId;
        this.label = label;
        this.templateFileName = templateFileName;
        this.type = type;
        this.area = area;
    }

    /**
     * Get template file name.
     *
     * @return String
     */
    public String getTemplateFileName() {
        return templateFileName;
    }

    /**
     * Get ID.
     *
     * @return String
     */
    public String getId() {
        return emailTemplateId;
    }

    /**
     * Get label.
     *
     * @return String
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get module.
     *
     * @return String
     */
    public String getModule() {
        return module;
    }

    /**
     * Get type.
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return area;
    }
}
