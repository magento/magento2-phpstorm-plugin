/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;

public class TypeConfigurationTagTypesInspectionTest extends InspectionXmlFixtureTestCase {

    private static final String CLASS_DOES_NOT_EXIST = "inspection.warning.class.does.not.exist";
    private static final String NOT_EXISTENT_CLASS = "Not\\Existent\\Class";
    private static final String NOT_EXISTENT_VIRTUAL_TYPE = "NotExistentVirtualType";
    // This virtual type is created in the scope of this inspection and
    // it is in the ./testData/project/magento2/app/code/Foo/Bar/etc/di.xml file.
    private static final String EXISTENT_VIRTUAL_TYPE = "VirtualProductRepository";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(InvalidDependencyInjectionTypeInspection.class);
    }

    /**
     * Test type doesn't exists highlighting: <type name="TestingType"/>.
     */
    public void testNameAttributeValueTypeDoesNotExist() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                NOT_EXISTENT_CLASS
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Test type exists: <type name="TestingType"/>.
     */
    public void testNameAttributeValueTypeExists() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                "Magento\\Catalog\\Api\\ProductRepositoryInterface"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Test argument factory type exists: <argument xsi:type="object">TestingTypeFactory</argument>.
     */
    public void testArgumentFactoryTypeExists() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                "Magento\\Catalog\\Api\\ProductRepositoryInterfaceFactory"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Test proxy type doesn't exist highlighting:
     * <item name="element2" xsi:type="object">TestType\Proxy</item>.
     */
    public void testRecursivelyArgumentProxyTypeDoesNotExist() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                NOT_EXISTENT_CLASS.concat("\\Proxy")
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Test proxy type exists.
     * <item name="element2" xsi:type="object">TestType\Proxy</item>
     */
    public void testRecursivelyArgumentProxyTypeExists() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                "Magento\\Catalog\\Api\\ProductRepositoryInterfaceFactory\\Proxy"
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Test proxy type doesn't exist highlighting:
     * <item name="element2" xsi:type="object">TestType\Proxy</item>.
     */
    public void testRecursivelyArgumentVirtualTypeDoesNotExist() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                NOT_EXISTENT_VIRTUAL_TYPE
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Test virtual type exists.
     * <item name="element2" xsi:type="object">TestVirtualType</item>
     */
    public void testRecursivelyArgumentVirtualTypeExists() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                EXISTENT_VIRTUAL_TYPE
        );

        assertHasNoHighlighting(errorMessage);
    }

    private void configureFixture() {
        myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME));
    }
}
