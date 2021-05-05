/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.magento.idea.magento2plugin.magento.files.ModuleWebApiXmlFile;

public class WebApiServiceInspectionTest extends InspectionXmlFixtureTestCase {

    private static final String CLASS_DOES_NOT_EXIST =
            "inspection.warning.class.does.not.exist";

    private static final String METHOD_DOES_NOT_EXIST =
            "inspection.warning.method.does.not.exist";

    private static final String METHOD_SHOULD_HAVE_PUBLIC_ACCESS =
            "inspection.warning.method.should.have.public.access";

    private static final String NOT_EXISTENT_CLASS =
            "Not\\Existent\\Class";

    private static final String NOT_EXISTENT_METHOD =
            "notExistent";

    private static final String NOT_PUBLIC_METHOD =
            "myProtected";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(WebApiServiceInspection.class);
    }

    /**
     * Inspection highlights warning if the class attribute in the service tag contains
     * name of the not existent class.
     */
    public void testNotExistentClass() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                NOT_EXISTENT_CLASS
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning if the service class exists.
     */
    public void testExistentClass() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                CLASS_DOES_NOT_EXIST,
                NOT_EXISTENT_CLASS
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection highlights warning if the method attribute in the service tag contains
     * name of the not existent method.
     */
    public void testNotExistentMethod() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                METHOD_DOES_NOT_EXIST,
                NOT_EXISTENT_METHOD
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning if the service method exists.
     */
    public void testExistentMethod() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                METHOD_DOES_NOT_EXIST,
                NOT_EXISTENT_METHOD
        );

        assertHasNoHighlighting(errorMessage);
    }

    /**
     * Inspection highlights warning if the method attribute in the service tag contains
     * name of the method with not public access.
     */
    public void testNotPublicMethod() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                METHOD_SHOULD_HAVE_PUBLIC_ACCESS,
                NOT_PUBLIC_METHOD
        );

        assertHasHighlighting(errorMessage);
    }

    /**
     * Inspection skips warning if the service method has public access.
     */
    public void testPublicMethod() {
        configureFixture();

        final String errorMessage =  inspectionBundle.message(
                METHOD_SHOULD_HAVE_PUBLIC_ACCESS,
                NOT_PUBLIC_METHOD
        );

        assertHasNoHighlighting(errorMessage);
    }

    private void configureFixture() {
        myFixture.configureByFile(getFixturePath(ModuleWebApiXmlFile.FILE_NAME));
    }
}
