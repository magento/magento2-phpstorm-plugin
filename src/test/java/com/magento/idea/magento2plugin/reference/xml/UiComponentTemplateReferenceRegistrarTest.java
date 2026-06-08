/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

public class UiComponentTemplateReferenceRegistrarTest extends ReferenceXmlFixtureTestCase {

    public void testFileTemplateAttributeMustHaveReference() {
        String filePath = this.getFixturePath("test_form.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/template/template2.html");
    }

    public void testItemTemplateMustHaveReference() {
        String filePath = this.getFixturePath("checkout_index_index.xml");
        myFixture.configureByFile(filePath);

        assertHasReferenceToFile("app/code/Foo/Bar/view/frontend/web/template/template2.html");
    }

    public void testItemChildTemplateMustHaveReference() {
        addTemplate("xml-child-template-target", "<span>child</span>");
        myFixture.configureByText(
                "checkout_index_index.xml",
                "<?xml version=\"1.0\"?>\n"
                        + "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <body>\n"
                        + "        <referenceBlock name=\"checkout.root\">\n"
                        + "            <arguments>\n"
                        + "                <argument name=\"jsLayout\" xsi:type=\"array\">\n"
                        + "                    <item name=\"components\" xsi:type=\"array\">\n"
                        + "                        <item name=\"checkout\" xsi:type=\"array\">\n"
                        + "                            <item name=\"childTemplate\" xsi:type=\"string\">"
                        + "Foo_Bar/template/xml-child-template-<caret>target</item>\n"
                        + "                        </item>\n"
                        + "                    </item>\n"
                        + "                </argument>\n"
                        + "            </arguments>\n"
                        + "        </referenceBlock>\n"
                        + "    </body>\n"
                        + "</page>"
        );

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/xml-child-template-target.html"
        );
    }

    public void testItemTemplatesCollectionMustHaveReference() {
        addTemplate("xml-templates-collection-target", "<span>collection</span>");
        myFixture.configureByText(
                "checkout_index_index.xml",
                "<?xml version=\"1.0\"?>\n"
                        + "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "    <body>\n"
                        + "        <referenceBlock name=\"checkout.root\">\n"
                        + "            <arguments>\n"
                        + "                <argument name=\"jsLayout\" xsi:type=\"array\">\n"
                        + "                    <item name=\"components\" xsi:type=\"array\">\n"
                        + "                        <item name=\"checkout\" xsi:type=\"array\">\n"
                        + "                            <item name=\"templates\" xsi:type=\"array\">\n"
                        + "                                <item name=\"custom\" xsi:type=\"string\">"
                        + "Foo_Bar/template/xml-templates-collection-<caret>target</item>\n"
                        + "                            </item>\n"
                        + "                        </item>\n"
                        + "                    </item>\n"
                        + "                </argument>\n"
                        + "            </arguments>\n"
                        + "        </referenceBlock>\n"
                        + "    </body>\n"
                        + "</page>"
        );

        assertHasReferenceToFile(
                "app/code/Foo/Bar/view/frontend/web/template/xml-templates-collection-target.html"
        );
    }

    private void addTemplate(final String name, final String content) {
        myFixture.addFileToProject(
                "app/code/Foo/Bar/view/frontend/web/template/" + name + ".html",
                content
        );
    }
}
