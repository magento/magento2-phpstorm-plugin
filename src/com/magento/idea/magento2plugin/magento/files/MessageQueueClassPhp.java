/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;
import com.jetbrains.php.lang.PhpLanguage;

public class MessageQueueClassPhp implements ModuleFileInterface {
    public static final String HANDLER_TEMPLATE = "Magento Message Queue Handler Class";
    public static final String CONSUMER_TEMPLATE = "Magento Message Queue Consumer Class";
    public static final String FILE_EXTENSION = "php";
    private String className;
    private final Type type;

    /**
     * Constructor.
     *
     * @param className String
     * @param type Type
     */
    public MessageQueueClassPhp(
            final String className,
            final Type type
    ) {
        this.className = className;
        this.type = type;
    }

    /**
     * Set class name.
     *
     * @param className String
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", className, FILE_EXTENSION);
    }

    @Override
    public String getTemplate() {
        if (type.equals(Type.CONSUMER)) {
            return CONSUMER_TEMPLATE;
        }
        return HANDLER_TEMPLATE;
    }

    @Override
    public Language getLanguage() {
        return PhpLanguage.INSTANCE;
    }

    public enum Type {
        HANDLER,
        CONSUMER
    }
}
