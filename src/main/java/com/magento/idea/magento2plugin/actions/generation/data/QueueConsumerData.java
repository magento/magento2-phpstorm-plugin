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
    private final String handler;

    /**
     * Constructor.
     */
    public QueueConsumerData(
            final String consumerName,
            final String queueName,
            final String consumerType,
            final String maxMessages,
            final String connectionName,
            final String moduleName,
            final String handler
    ) {
        this.consumerName = consumerName;
        this.queueName = queueName;
        this.consumerType = consumerType;
        this.maxMessages = maxMessages;
        this.connectionName = connectionName;
        this.moduleName = moduleName;
        this.handler = handler;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getConsumerClass() {
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

    public String getHandler() {
        return handler;
    }
}
