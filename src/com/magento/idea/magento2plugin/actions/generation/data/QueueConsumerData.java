package com.magento.idea.magento2plugin.actions.generation.data;

public class QueueConsumerData {
    private String consumerName;
    private String queueName;
    private String consumerType;
    private String maxMessages;
    private String connectionName;
    private String moduleName;

    public QueueConsumerData(
            String consumerName,
            String queueName,
            String consumerType,
            String maxMessages,
            String connectionName,
            String moduleName
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
