/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class UiComponentFormGeneratorTest extends BaseGeneratorTestCase {

    private static final String EXPECTED_DIRECTORY =
            "src/app/code/Foo/Bar/view/adminhtml/ui_component";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String ROUTE = "customroute";
    private static final String FORM_NAME = "my_form";
    private static final String FILE_NAME = "my_form.xml";
    private static final String LABEL = "My Form";

    /**
     * Test generating layout XML file.
     */
    public void testGenerateFormXmlFile() {
        final String filePath = this.getFixturePath(FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();

        final List<UiComponentFormButtonData> buttons = getButtons();
        final List<UiComponentFormFieldsetData> fieldsets = getFieldsets();
        final List<UiComponentFormFieldData> fields = getFields();

        final UiComponentFormFileData uiComponentFormData = new UiComponentFormFileData(
                FORM_NAME,
                Areas.adminhtml.toString(),
                MODULE_NAME,
                LABEL,
                buttons,
                fieldsets,
                fields,
                ROUTE,
                "MyEntity",
                "Save",
                "DataProvider",
                "Ui/MyEntity"
        );
        final UiComponentFormGenerator uiComponentFormGenerator = new UiComponentFormGenerator(
                uiComponentFormData,
                project
        );

        final PsiFile file = uiComponentFormGenerator.generate("test");
        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }

    /**
     * Get fields data.
     *
     * @return List
     */
    protected @NotNull List<UiComponentFormFieldData> getFields() {
        final List<UiComponentFormFieldData> fields = new ArrayList<>();

        fields.add(new UiComponentFormFieldData(
                "my_field",
                "My Field",
                "10",
                "General",
                "input",
                "text",
                "entity"
        ));
        fields.add(new UiComponentFormFieldData(
                "my_field_2",
                "My Field 2",
                "10",
                "Test Fieldset",
                "input",
                "text",
                "entity"
        ));

        return fields;
    }

    /**
     * Get fieldSets data.
     *
     * @return List
     */
    protected @NotNull List<UiComponentFormFieldsetData> getFieldsets() {
        final List<UiComponentFormFieldsetData> fieldsets = new ArrayList<>();

        fieldsets.add(new UiComponentFormFieldsetData(
                "general",
                "General",
                "10"
        ));
        fieldsets.add(new UiComponentFormFieldsetData(
                "test_fieldset",
                "Test Fieldset",
                "20"
        ));

        return fieldsets;
    }

    /**
     * Get buttons data.
     *
     * @return List
     */
    protected @NotNull List<UiComponentFormButtonData> getButtons() {
        final List<UiComponentFormButtonData> buttons = new ArrayList<>();
        final String namespace = "Foo/Bar/Block/Form";
        final String directory = "Block/Form";

        buttons.add(new UiComponentFormButtonData(
                directory,
                "SaveEntity",
                MODULE_NAME,
                "Save",
                namespace,
                "Save Entity",
                "10",
                FORM_NAME,
                "Foo\\Bar\\Block\\Form\\Save"
        ));
        buttons.add(new UiComponentFormButtonData(
                directory,
                "BackToEntity",
                MODULE_NAME,
                "Back",
                namespace,
                "Back To Grid",
                "20",
                FORM_NAME,
                "Foo\\Bar\\Block\\Form\\Back"
        ));
        buttons.add(new UiComponentFormButtonData(
                directory,
                "DeleteEntity",
                MODULE_NAME,
                "Save",
                namespace,
                "Delete Entity",
                "30",
                FORM_NAME,
                "Foo\\Bar\\Block\\Form\\Delete"
        ));
        buttons.add(new UiComponentFormButtonData(
                directory,
                "CustomController",
                MODULE_NAME,
                "Custom",
                namespace,
                "Custom Button",
                "40",
                FORM_NAME,
                "Foo\\Bar\\Block\\Form\\Custom"
        ));

        return buttons;
    }
}
