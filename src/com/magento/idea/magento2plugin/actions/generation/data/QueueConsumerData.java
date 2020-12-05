/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class QueueConsumerData {
    private final String consumerName;
    private final String queueName;
    private final String consumerType;
    private final String maxMessages;
    private final String connectionName;
    private final String moduleName;

    /**
     * Constructor.
     */
    public QueueConsumerData(
            final String consumerName,
            final String queueName,
            final String consumerType,
            final String maxMessages,
            final String connectionName,
            final String moduleName
    ) {
        this.consumerName = consumerName;
        this.queueName = queueName;
        this.consumerType = consumerType;
        this.maxMessages = maxMessages;
        this.connectionName = connectionName;
        this.moduleName = moduleName;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public String getMaxMessages() {
        return maxMessages;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public String getModuleName() {
        return moduleName;
    }
}
