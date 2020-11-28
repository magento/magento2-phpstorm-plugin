package com.magento.idea.magento2plugin.actions.generation.data;

public class QueueTopologyData {
    private String exchangeName;
    private String bindingId;
    private String bindingTopic;
    private String bindingQueue;

    public QueueTopologyData(String exchangeName, String bindingId, String bindingTopic, String bindingQueue) {
        this.exchangeName = exchangeName;
        this.bindingId = bindingId;
        this.bindingTopic = bindingTopic;
        this.bindingQueue = bindingQueue;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getBindingId() {
        return bindingId;
    }

    public String getBindingTopic() {
        return bindingTopic;
    }

    public String getBindingQueue() {
        return bindingQueue;
    }
}
