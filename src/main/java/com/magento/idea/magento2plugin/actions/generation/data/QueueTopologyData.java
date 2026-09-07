/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class QueueTopologyData {
    private final String exchangeName;
    private final String connectionName;
    private final String bindingId;
    private final String bindingTopic;
    private final String bindingQueue;
    private final String moduleName;

    /**
     * Constructor.
     */
    public QueueTopologyData(
            final String exchangeName,
            final String connectionName,
            final String bindingId,
            final String bindingTopic,
            final String bindingQueue,
            final String moduleName
    ) {
        this.exchangeName = exchangeName;
        this.connectionName = connectionName;
        this.bindingId = bindingId;
        this.bindingTopic = bindingTopic;
        this.bindingQueue = bindingQueue;
        this.moduleName = moduleName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getConnectionName() {
        return connectionName;
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

    public String getModuleName() {
        return moduleName;
    }
}
