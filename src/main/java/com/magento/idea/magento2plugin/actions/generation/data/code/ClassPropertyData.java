/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data.code;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class ClassPropertyData {
    private final List<String> data = new ArrayList<>();

    /**
     * Constructor.
     */
    public ClassPropertyData(
            final String type,
            final String lowerCamelName,
            final String upperCamelName,
            final String lowerSnakeName,
            final String upperSnakeName
    ) {
        data.add(upperSnakeName);
        data.add(lowerSnakeName);
        data.add(type);
        data.add(upperCamelName);
        data.add(lowerCamelName);
    }

    public String string() {
        return StringUtils.join(data, ";");
    }
}
