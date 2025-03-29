/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.xml.DiArgumentData;
import com.magento.idea.magento2plugin.actions.generation.data.xml.DiArrayValueData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.ArgumentInjectionGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.DiArgumentType;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("PMD.TooManyMethods")
public class ArgumentInjectionGeneratorTest extends BaseGeneratorTestCase {

    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc/frontend";
    private static final String EXPECTED_DIR_FOR_REPLACING = "src/app/code/Foo/Bar/etc/crontab";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String TARGET_CLASS = "Foo\\Bar\\Model\\Test";
    private static final String STRING_PARAMETER = "name";
    private static final String STRING_VALUE = "test";
    private static final String BOOL_PARAMETER = "isEmpty";
    private static final String BOOL_VALUE = "false";
    private static final String NUMBER_PARAMETER = "age";
    private static final String NUMBER_VALUE = "12";
    private static final String INIT_PARAM_PARAMETER = "defaultArea";
    private static final String INIT_PARAM_VALUE = "Foo\\Bar\\Model\\AreaTest::DEFAULT_AREA";
    private static final String CONST_PARAMETER = "defaultService";
    private static final String CONST_VALUE = "Foo\\Bar\\Model\\ServiceTest::DEFAULT_SERVICE";
    private static final String NULL_PARAMETER = "object";
    private static final String NULL_VALUE = "";
    private static final String OBJECT_PARAMETER = "object";
    private static final String OBJECT_VALUE = "Foo\\Bar\\Model\\Service";
    private static final String ARRAY_PARAMETER = "methods";
    private static final Areas TEST_AREA = Areas.frontend;

    /**
     * Tested string value injection.
     */
    public void testInjectStringValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                STRING_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.STRING,
                                STRING_VALUE
                        )
                )
        );
    }

    /**
     * Tested boolean value injection.
     */
    public void testInjectBooleanValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                BOOL_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.BOOLEAN,
                                BOOL_VALUE
                        )
                )
        );
    }

    /**
     * Tested number value injection.
     */
    public void testInjectNumberValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                NUMBER_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.NUMBER,
                                NUMBER_VALUE
                        )
                )
        );
    }

    /**
     * Tested init_parameter value injection.
     */
    public void testInjectInitParameterValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                INIT_PARAM_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.INIT_PARAMETER,
                                INIT_PARAM_VALUE
                        )
                )
        );
    }

    /**
     * Tested constant value injection.
     */
    public void testInjectConstValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                CONST_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.CONST,
                                CONST_VALUE
                        )
                )
        );
    }

    /**
     * Tested null value injection.
     */
    public void testInjectNullValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                NULL_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.NULL,
                                NULL_VALUE
                        )
                )
        );
    }

    /**
     * Tested object value injection.
     */
    public void testInjectObjectValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                OBJECT_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.OBJECT,
                                OBJECT_VALUE
                        )
                )
        );
    }

    /**
     * Tested array value injection.
     */
    public void testInjectArrayValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                ARRAY_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.ARRAY,
                                getArrayValue()
                        )
                )
        );
    }

    /**
     * Tested nested array value injection.
     */
    public void testInjectNestedArrayValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIRECTORY,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                ARRAY_PARAMETER,
                                TEST_AREA,
                                DiArgumentType.ARRAY,
                                getNestedArrayValue()
                        )
                )
        );
    }

    /**
     * Tested object value replacing with the null value injection.
     */
    public void testReplaceObjectValueWithNullValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIR_FOR_REPLACING,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                OBJECT_PARAMETER,
                                Areas.crontab,
                                DiArgumentType.NULL,
                                NULL_VALUE
                        )
                )
        );
    }

    /**
     * Tested object value replacing with the object proxy value injection.
     */
    public void testReplaceObjectValueWithProxyValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIR_FOR_REPLACING,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                OBJECT_PARAMETER,
                                Areas.crontab,
                                DiArgumentType.OBJECT,
                                OBJECT_VALUE + "\\Proxy"
                        )
                )
        );
    }

    /**
     * Tested object value replacing with the object factory value injection.
     */
    public void testReplaceObjectValueWithFactoryValue() {
        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(getFixturePath(ModuleDiXml.FILE_NAME)),
                EXPECTED_DIR_FOR_REPLACING,
                injectConstructorArgument(
                        new DiArgumentData(
                                MODULE_NAME,
                                TARGET_CLASS,
                                OBJECT_PARAMETER,
                                Areas.crontab,
                                DiArgumentType.OBJECT,
                                OBJECT_VALUE + "Factory"
                        )
                )
        );
    }

    private PsiFile injectConstructorArgument(
            final DiArgumentData data
    ) {
        final ArgumentInjectionGenerator generator = new ArgumentInjectionGenerator(
                data,
                myFixture.getProject()
        );

        return generator.generate("test");
    }

    private String getArrayValue() {
        final List<DiArrayValueData.DiArrayItemData> items = new ArrayList<>();
        items.add(new DiArrayValueData.DiArrayItemData("method1", DiArgumentType.STRING, "QW1"));
        items.add(new DiArrayValueData.DiArrayItemData("method2", DiArgumentType.STRING, "QW2"));
        items.add(new DiArrayValueData.DiArrayItemData("method3", DiArgumentType.STRING, "QW3"));
        items.add(new DiArrayValueData.DiArrayItemData("method4", DiArgumentType.STRING, "QW4"));
        items.add(new DiArrayValueData.DiArrayItemData("method5", DiArgumentType.STRING, "QW5"));
        final DiArrayValueData arrayValueData = new DiArrayValueData();
        arrayValueData.setItems(items);

        return arrayValueData.convertToXml(arrayValueData);
    }

    private String getNestedArrayValue() {
        final List<DiArrayValueData.DiArrayItemData> items = new ArrayList<>();
        items.add(new DiArrayValueData.DiArrayItemData("method1", DiArgumentType.STRING, "QW1"));
        items.add(new DiArrayValueData.DiArrayItemData("method2", DiArgumentType.STRING, "QW2"));
        items.add(new DiArrayValueData.DiArrayItemData("method3", DiArgumentType.STRING, "QW3"));
        items.add(new DiArrayValueData.DiArrayItemData("method4", DiArgumentType.STRING, "QW4"));
        final DiArrayValueData.DiArrayItemData nestedItem = new DiArrayValueData.DiArrayItemData(
                "nested",
                DiArgumentType.ARRAY,
                ""
        );

        final DiArrayValueData nestedItemsHolder = new DiArrayValueData();
        final List<DiArrayValueData.DiArrayItemData> nestedItems = new ArrayList<>();
        nestedItems.add(
                new DiArrayValueData.DiArrayItemData("nested1", DiArgumentType.STRING, "NT1")
        );
        nestedItems.add(
                new DiArrayValueData.DiArrayItemData("nested2", DiArgumentType.BOOLEAN, "true")
        );
        nestedItems.add(
                new DiArrayValueData.DiArrayItemData("nested3", DiArgumentType.NULL, "")
        );
        nestedItemsHolder.setItems(nestedItems);
        nestedItem.setChildren(nestedItemsHolder);
        items.add(nestedItem);

        final DiArrayValueData arrayValueData = new DiArrayValueData();
        arrayValueData.setItems(items);

        return arrayValueData.convertToXml(arrayValueData);
    }
}
