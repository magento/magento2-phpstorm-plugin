/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class InvalidVirtualTypeSourceClassInspectionTest extends InspectionXmlFixtureTestCase {

    private static final String ARGUMENT_VALUE_IS_EMPTY =
            "inspection.error.idAttributeCanNotBeEmpty";
    private static final String CLASS_DOES_NOT_EXIST =
            "inspection.warning.class.does.not.exist";
    private static final String NOT_EXISTENT_CLASS =
            "Not\\Existent\\Class";
    private static final String EXISTENT_CLASS =
            "Magento\\Catalog\\Plugin\\PluginClass";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(InvalidVirtualTypeSourceClassInspection.class);
    }

    public void testAttrTypeValueIsEmpty() {
        myFixture.configureByText(
                ModuleDiXml.FILE_NAME,
                "<?xml version=\"1.0\"?>\n"
                        + "<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <virtualType name=\"MyVirtualType\" type=\"\"/>\n"
                        + "</config>"
        );

        final String typeAttrIsEmptyMessage = inspectionBundle.message(
                ARGUMENT_VALUE_IS_EMPTY,
                ModuleDiXml.TYPE_ATTR
        );

        assertHasHighlighting(typeAttrIsEmptyMessage);
    }

    public void testClassAttrTypeDoesNotExist() {
        myFixture.configureByText(
                ModuleDiXml.FILE_NAME,
                "<?xml version=\"1.0\"?>\n"
                        + "<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <virtualType name=\"MyVirtualType\" type=\"" + NOT_EXISTENT_CLASS + "\"/>\n"
                        + "</config>"
        );

        final String classDoesNotExistMessage = inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                NOT_EXISTENT_CLASS
        );

        assertHasHighlighting(classDoesNotExistMessage);
    }

    public void testClassAttrTypeExists() {
        myFixture.configureByText(
                ModuleDiXml.FILE_NAME,
                "<?xml version=\"1.0\"?>\n"
                        + "<config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <virtualType name=\"MyVirtualType\" type=\"" + EXISTENT_CLASS + "\"/>\n"
                        + "</config>"
        );

        final String classDoesNotExistMessage = inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                EXISTENT_CLASS
        );

        assertHasNoHighlighting(classDoesNotExistMessage);
    }
}
