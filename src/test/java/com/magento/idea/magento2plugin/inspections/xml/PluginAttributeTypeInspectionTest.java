/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class PluginAttributeTypeInspectionTest extends InspectionXmlFixtureTestCase {

    private static final String ARGUMENT_VALUE_IS_EMPTY =
            "inspection.error.idAttributeCanNotBeEmpty";
    private static final String CLASS_DOES_NOT_EXIST =
            "inspection.warning.class.does.not.exist";
    private static final String EXISTENT_CLASS =
            "Magento\\Catalog\\Plugin\\PluginClass";
    private static final String NOT_EXISTENT_CLASS =
            "Not\\Existent\\Class";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(PluginAttributeTypeInspection.class);
    }

    /**
     * Test for an error for the "type" attribute because it is empty.
     * <plugin name="unique_plugin_name" type=""/>
     */
    public void testAttrArgTypeValueIsEmpty() {
        configureFixture();

        final String forAttrIsEmptyMessage =  inspectionBundle.message(
                ARGUMENT_VALUE_IS_EMPTY,
                ModuleDiXml.TYPE_ATTR
        );

        assertHasHighlighting(forAttrIsEmptyMessage);
    }

    /**
     * Test for no error for the "type" attribute because this class exists.
     * <plugin name="unique_plugin_name" type="Magento\Catalog\Plugin\PluginClass" />
     */
    public void testAttrTypeClassExists() {
        configureFixture();

        final String typeAttrIsEmptyMessage =  inspectionBundle.message(
                ARGUMENT_VALUE_IS_EMPTY,
                ModuleDiXml.TYPE_ATTR
        );

        assertHasNoHighlighting(typeAttrIsEmptyMessage);
    }

    /**
     * Test for throwing an error for a class that does not exist for the "type" attribute.
     */
    public void testClassAttrTypeDoesNotExists() {
        configureFixture();

        final String forClassDoesNotExists =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                NOT_EXISTENT_CLASS
        );

        assertHasHighlighting(forClassDoesNotExists);
    }

    /**
     * Test for the absence of an error in the presence of
     * classes or interfaces specified for plugins.
     */
    public void testClassAttrTypeIsExist() {
        configureFixture();

        final String classOneExists =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                EXISTENT_CLASS
        );

        assertHasNoHighlighting(classOneExists);
    }

    private void configureFixture() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));
    }
}
