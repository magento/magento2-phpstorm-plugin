/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.AclXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleAclXml;

public class AclXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String CONFIG_ACL_ID = "Magento_Config::config";
    private static final String FOO_BAR_MANAGE_ACL_ID = "Foo_Bar::manage";
    private static final String FOO_BAR_MANAGE_ACL_TITLE = "Bar Management";

    /**
     * Test checks whether acl.xml file generated correctly.
     */
    public void testGenerateAclXmlFile() {
        final String filePath = this.getFixturePath(ModuleAclXml.FILE_NAME);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                generateAclXmlFile(
                        CONFIG_ACL_ID,
                        FOO_BAR_MANAGE_ACL_ID,
                        FOO_BAR_MANAGE_ACL_TITLE
                )
        );
    }

    /**
     * Test checks whether acl.xml file generated correctly
     * for non existed parent.
     */
    public void testGenerateAclXmlFileForNonExistedParent() {
        final String filePath = this.getFixturePath(ModuleAclXml.FILE_NAME);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                generateAclXmlFile(
                        "Non_Existed::test",
                        "Foo_Bar::test",
                        "Non Existed Test"
                )
        );
    }

    /**
     * Test adding new acl rule to existing acl.xml file.
     */
    public void testGetAclXmlFileAndGenerateNewAcl() {
        final String filePath = this.getFixturePath(ModuleAclXml.FILE_NAME);
        generateAclXmlFile(CONFIG_ACL_ID, FOO_BAR_MANAGE_ACL_ID, FOO_BAR_MANAGE_ACL_TITLE);

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(filePath),
                EXPECTED_DIRECTORY,
                generateAclXmlFile(
                        FOO_BAR_MANAGE_ACL_ID,
                        "Foo_Bar::manage_save",
                        "Save"
                )
        );
    }

    /**
     * Generate ACL XML file.
     *
     * @param parentResourceId String
     * @param resourceId String
     * @param resourceTitle String
     *
     * @return PsiFile
     */
    private PsiFile generateAclXmlFile(
            final String parentResourceId,
            final String resourceId,
            final String resourceTitle
    ) {
        final AclXmlGenerator aclXmlGenerator = new AclXmlGenerator(
                new AclXmlData(parentResourceId, resourceId, resourceTitle),
                MODULE_NAME,
                myFixture.getProject()
        );
        return aclXmlGenerator.generate("test");
    }
}
