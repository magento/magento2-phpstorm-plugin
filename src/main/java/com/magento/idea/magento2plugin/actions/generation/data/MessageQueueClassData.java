/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import com.magento.idea.magento2plugin.magento.files.MessageQueueClassPhp;

@SuppressWarnings({"PMD.DataClass"})
public class MessageQueueClassData {
    private final String name;
    private final String namespace;
    private final String path;
    private final String fqn;
    private final MessageQueueClassPhp.Type type;

    /**
     * MessageQueueClassData constructor.
     *
     * @param name String
     * @param namespace String
     * @param path String
     * @param fqn String
     * @param type MessageQueueClassPhp.Type
     */
    public MessageQueueClassData(
            final String name,
            final String namespace,
            final String path,
            final String fqn,
            final MessageQueueClassPhp.Type type
    ) {
        this.name = name;
        this.namespace = namespace;
        this.path = path;
        this.fqn = fqn;
        this.type = type;
    }

    /**
     * Get data provider class name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get data provider class namespace.
     *
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Get path.
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * Get FQN.
     *
     * @return String
     */
    public String getFqn() {
        return fqn;
    }

    /**
     * Get Type.
     *
     * @return MessageQueueClassPhp.Type
     */
    public MessageQueueClassPhp.Type getType() {
        return type;
    }
}
