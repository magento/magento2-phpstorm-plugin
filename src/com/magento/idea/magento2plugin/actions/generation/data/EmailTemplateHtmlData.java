/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class EmailTemplateHtmlData {
    private final String module;
    private final String fileName;
    private final String area;
    private final String subject;
    private final String type;

    /**
     * Email template HTML data constructor.
     *
     * @param module String
     * @param area String
     * @param subject String
     * @param type String
     */
    public EmailTemplateHtmlData(
            final String module,
            final String fileName,
            final String area,
            final String subject,
            final String type
    ) {
        this.module = module;
        this.fileName = fileName;
        this.area = area;
        this.subject = subject;
        this.type = type;
    }

    /**
     * Get module name.
     *
     * @return String
     */
    public String getModule() {
        return module;
    }

    /**
     * Get file name.
     *
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get area.
     *
     * @return String
     */
    public String getArea() {
        return area;
    }

    /**
     * Get subject.
     *
     * @return String
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Get type.
     *
     * @return String
     */
    public String getType() {
        return type;
    }
}
