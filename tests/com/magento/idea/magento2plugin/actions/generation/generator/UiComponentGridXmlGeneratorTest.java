/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentGridToolbarData;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.util.ArrayList;

public class UiComponentGridXmlGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_DIRECTORY = "src/app/code/Foo/Bar/";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String COMPONENT_NAME = "custom_entity_grid";
    private static final String ENTITY_NAME = "Book";
    private static final String ID_FIELD_NAME = "entity_id";
    private static final String ACL = "Foo_Bar::custom_entity";
    private static final String DATA_PROVIDER_NAME = "DataProvider";
    private static final String DATA_PROVIDER_PATH = "Ui/Listing";

    /**
     * Test UI component listing file generation for specific area.
     */
    public void testGenerateUiGridForBaseArea() {
        final String area = Areas.base.toString();
        final PsiFile cronGroupsXmlFile = generateComponentGridXml(
                area,
                false
        );
        final String filePath = this.getFixturePath(String.format("%s.%s", COMPONENT_NAME, "xml"));
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(expectedFile, getExpectedDirectory(area), cronGroupsXmlFile);
    }

    /**
     * Test UI component listing file generation without toolbar.
     */
    public void testGenerateUiGridWithoutToolbar() {
        final String area = Areas.adminhtml.toString();
        final PsiFile cronGroupsXmlFile = generateComponentGridXml(
                area,
                true
        );
        final String filePath = this.getFixturePath(String.format("%s.%s", COMPONENT_NAME, "xml"));
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(expectedFile, getExpectedDirectory(area), cronGroupsXmlFile);
    }

    /**
     * Test UI component listing file generation with actions column.
     */
    public void testGenerateUiGridForBaseAreaWithActionsColumnColumn() {
        final String area = Areas.base.toString();
        final PsiFile cronGroupsXmlFile = generateComponentGridXmlWithActionsColumn(area);
        final String filePath = this.getFixturePath(String.format("%s.%s", COMPONENT_NAME, "xml"));
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(expectedFile, getExpectedDirectory(area), cronGroupsXmlFile);
    }

    /**
     * Generate UI Component Grid Xml file.
     *
     * @param area String
     * @param addToolbar boolean
     *
     * @return PsiFile
     */
    private PsiFile generateComponentGridXml(
            final String area,
            final boolean addToolbar
    ) {
        final Project project = myFixture.getProject();
        final UiComponentGridToolbarData uiGridToolbarData = new UiComponentGridToolbarData(
                addToolbar,
                true,
                true,
                true,
                true,
                true
        );
        final UiComponentGridData uiGridData = new UiComponentGridData(
                MODULE_NAME,
                area,
                COMPONENT_NAME,
                ID_FIELD_NAME,
                ACL,
                DATA_PROVIDER_NAME,
                DATA_PROVIDER_PATH,
                uiGridToolbarData
        );
        final UiComponentGridXmlGenerator uiGridXmlGenerator = new UiComponentGridXmlGenerator(
                uiGridData,
                project
        );

        return uiGridXmlGenerator.generate("test");
    }

    /**
     * Generate UI Component Grid Xml file with actions column.
     *
     * @param area String
     *
     * @return PsiFile
     */
    private PsiFile generateComponentGridXmlWithActionsColumn(
            final String area
    ) {
        final Project project = myFixture.getProject();
        final UiComponentGridToolbarData uiGridToolbarData = new UiComponentGridToolbarData(
                false,
                true,
                true,
                true,
                true,
                true
        );
        final UiComponentGridData uiGridData = new UiComponentGridData(
                MODULE_NAME,
                area,
                COMPONENT_NAME,
                ID_FIELD_NAME,
                ACL,
                DATA_PROVIDER_NAME,
                DATA_PROVIDER_PATH,
                ENTITY_NAME,
                uiGridToolbarData,
                new ArrayList<>()
        );
        final UiComponentGridXmlGenerator uiGridXmlGenerator = new UiComponentGridXmlGenerator(
                uiGridData,
                project
        );

        return uiGridXmlGenerator.generate("test");
    }

    /**
     * Get expected directory.
     *
     * @param area Area code
     * @return String
     */
    private String getExpectedDirectory(final String area) {
        return String.format(
                "%s%s%s%s%s%s",
                MODULE_DIRECTORY,
                Package.moduleViewDir,
                File.separator,
                area,
                File.separator,
                Package.moduleViewUiComponentDir
        );
    }
}
