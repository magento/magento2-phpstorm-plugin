/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.database;

import java.util.LinkedList;
import java.util.List;

public enum TableEngines {
    INNODB("innodb"),
    MEMORY("memory");

    private final String engine;

    /**
     * Table Engines ENUM constructor.
     *
     * @param tableEngine String
     */
    TableEngines(final String tableEngine) {
        engine = tableEngine;
    }

    /**
     * Get table engine name.
     *
     * @return String
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Get table available/supported engines list.
     *
     * @return List of available engines.
     */
    public static List<String> getTableEnginesList() {
        final List<String> availableEngines = new LinkedList<>();

        for (final TableEngines engine : TableEngines.values()) {
            availableEngines.add(engine.getEngine());
        }

        return availableEngines;
    }
}
