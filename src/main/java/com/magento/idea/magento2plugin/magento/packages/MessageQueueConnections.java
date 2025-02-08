/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import java.util.ArrayList;
import java.util.List;

public enum MessageQueueConnections {

    DB("db"),
    AMPQ("amqp");

    private final String type;

    /**
     * Queue connection constructor.
     *
     * @param type String
     */
    MessageQueueConnections(final String type) {
        this.type = type;
    }

    /**
     * Get connection type.
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * Get list of connection types.
     *
     * @return List connection types.
     */
    public static List<String> getList() {
        final List<String> typeList = new ArrayList<>();

        for (final MessageQueueConnections type: MessageQueueConnections.values()) {
            typeList.add(type.getType());
        }

        return typeList;
    }
}
