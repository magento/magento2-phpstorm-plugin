package com.magento.idea.magento2plugin.actions.generation.data;

public class QueueConsumerData {
    private String consumerName;
    private String queueName;
    private String consumerType;
    private String maxMessages;
    private String connectionName;

    public QueueConsumerData(
            String consumerName,
            String queueName,
            String consumerType,
            String maxMessages,
            String connectionName
    ) {
        this.consumerName = consumerName;
        this.queueName = queueName;
        this.consumerType = consumerType;
        this.maxMessages = maxMessages;
        this.connectionName = connectionName;
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
}
