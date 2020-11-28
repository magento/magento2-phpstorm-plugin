package com.magento.idea.magento2plugin.actions.generation.data;

public class QueueCommunicationData {
    private String topicName;
    private String handlerName;
    private String handlerType;
    private String handlerMethod;

    public QueueCommunicationData(
            String topicName,
            String handlerName,
            String handlerType,
            String handlerMethod
    ) {
        this.topicName = topicName;
        this.handlerName = handlerName;
        this.handlerType = handlerType;
        this.handlerMethod = handlerMethod;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getHandlerType() {
        return handlerType;
    }

    public String getHandlerMethod() {
        return handlerMethod;
    }
}
