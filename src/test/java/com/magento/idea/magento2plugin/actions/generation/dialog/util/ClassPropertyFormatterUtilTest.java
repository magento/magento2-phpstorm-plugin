/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.util;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import junit.framework.TestCase;

public class ClassPropertyFormatterUtilTest extends TestCase {

    public void testFormatPropertiesSkipsBlankRows() {
        final DefaultTableModel table = new DefaultTableModel(
                new Object[][] {
                        {"", "string"},
                        {"   ", "int"},
                        {"description", null},
                        {"product_name", "string"},
                },
                new Object[] {"Name", "Type"}
        );

        final List<String> properties = ClassPropertyFormatterUtil.formatProperties(table);

        assertEquals(1, properties.size());
        assertEquals(
                "PRODUCT_NAME;product_name;string;ProductName;productName",
                properties.get(0)
        );
    }

    public void testFormatSinglePropertyTrimsInput() {
        assertEquals(
                "PRODUCT_NAME;product_name;string;ProductName;productName",
                ClassPropertyFormatterUtil.formatSingleProperty(" product_name ", " string ")
        );
    }
}
