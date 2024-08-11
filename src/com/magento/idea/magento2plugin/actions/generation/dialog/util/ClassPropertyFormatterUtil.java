/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.util;

import com.google.common.base.CaseFormat;
import com.magento.idea.magento2plugin.actions.generation.data.code.ClassPropertyData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.StringUtils;

public final class ClassPropertyFormatterUtil {

    private ClassPropertyFormatterUtil() {}

    /**
     * Format table properties to ClassPropertyData list.
     *
     * @param table DefaultTableModel
     *
     * @return List
     */
    public static List<String> formatProperties(final DefaultTableModel table) {
        final List<String> properties = new ArrayList<>();

        for (int index = 0; index < table.getRowCount(); index++) {
            final String name = table.getValueAt(index, 0).toString();
            final String type = table.getValueAt(index, 1).toString();

            properties.add(ClassPropertyFormatterUtil.formatSingleProperty(name, type));
        }

        return properties;
    }

    /**
     * Format single php class property.
     *
     * @param name String
     * @param type String
     *
     * @return String
     */
    public static String formatSingleProperty(final String name, final String type) {
        return new ClassPropertyData(
                type,
                CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name),
                CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name),
                name,
                formatNameToConstant(name)
        ).string();
    }

    /**
     * Gets properties as a string, ready for templating.
     * "UPPER_SNAKE;lower_snake;type;UpperCamel;lowerCamel".
     */
    public static String joinProperties(final List<String> properties) {
        return StringUtils.join(properties, ",");
    }

    /**
     * Format property field to constant format with specified type.
     *
     * @param name String
     *
     * @return String
     */
    public static String formatNameToConstant(final String name, final String typeFqn) {
        return String.format(
                "%s::%s",
                PhpClassGeneratorUtil.getNameFromFqn(typeFqn),
                formatNameToConstant(name)
        );
    }

    /**
     * Format property field to constant format.
     *
     * @param name String
     *
     * @return String
     */
    public static String formatNameToConstant(final String name) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE, name);
    }
}
