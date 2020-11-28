package com.magento.idea.magento2plugin.actions.generation.data;

public class QueuePublisherData {
    private String topicName;
    private String connectionName;
    private String exchangeName;

    public QueuePublisherData(String topicName, String connectionName, String exchangeName) {
        this.topicName = topicName;
        this.connectionName = connectionName;
        this.exchangeName = exchangeName;
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
}
