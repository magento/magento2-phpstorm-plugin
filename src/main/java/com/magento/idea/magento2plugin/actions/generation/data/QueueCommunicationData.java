/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class QueueCommunicationData {
    private final String topicName;
    private final String handlerName;
    private final String handlerType;
    private final String handlerMethod;
    private final String moduleName;

    /**
     * Constructor.
     */
    public QueueCommunicationData(
            final String topicName,
            final String handlerName,
            final String handlerType,
            final String handlerMethod,
            final String moduleName
    ) {
        this.topicName = topicName;
        this.handlerName = handlerName;
        this.handlerType = handlerType;
        this.handlerMethod = handlerMethod;
        this.moduleName = moduleName;
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

    public String getModuleName() {
        return moduleName;
    }
}
