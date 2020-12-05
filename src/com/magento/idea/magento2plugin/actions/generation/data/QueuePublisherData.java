/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class QueuePublisherData {
    private String topicName;
    private String connectionName;
    private String exchangeName;
    private String moduleName;

    /**
     * Constructor.
     */
    public QueuePublisherData(
            String topicName,
            String connectionName,
            String exchangeName,
            String moduleName
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
