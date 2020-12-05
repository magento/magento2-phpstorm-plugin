/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class QueuePublisherData {
    private final String topicName;
    private final String connectionName;
    private final String exchangeName;
    private final String moduleName;

    /**
     * Constructor.
     */
    public QueuePublisherData(
            final String topicName,
            final String connectionName,
            final String exchangeName,
            final String moduleName
    ) {
        this.topicName = topicName;
        this.connectionName = connectionName;
        this.exchangeName = exchangeName;
        this.moduleName = moduleName;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getModuleName() {
        return moduleName;
    }
}
