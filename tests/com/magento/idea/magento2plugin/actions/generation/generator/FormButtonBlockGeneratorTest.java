/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;

public class FormButtonBlockGeneratorTest extends BaseGeneratorTestCase {
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String FORM_NAME = "my_form";
    private static final String BLOCK_DIRECTORY = "Block/Form";
    private static final String ACTION_NAME = "test";
    public static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Block/Form";

    /**
     * Test creation Block type Save.
     */
    public void testGenerateSaveButtonBlock() {
        final String filePath = this.getFixturePath("SaveBlock.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final String className = "SaveBlock";
        final NamespaceBuilder namespace = new NamespaceBuilder(
                MODULE_NAME,
                className,
                BLOCK_DIRECTORY
        );

        final UiComponentFormButtonData uiComponentFormButtonData = new UiComponentFormButtonData(
                BLOCK_DIRECTORY,
                className,
                MODULE_NAME,
                "Save",
                namespace.getNamespace(),
                "Save Entity",
                "10",
                FORM_NAME,
                namespace.getClassFqn()
        );
        final UiComponentFormButtonPhpClassGenerator formButtonPhpClassGenerator =
                new UiComponentFormButtonPhpClassGenerator(
                    uiComponentFormButtonData,
                    project
            );

        final PsiFile file = formButtonPhpClassGenerator.generate(ACTION_NAME);
        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }

    /**
     * Test creation Block type Back.
     */
    public void testGenerateBackButtonBlock() {
        final String filePath = this.getFixturePath("MyBackButton.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final String className = "MyBackButton";
        final NamespaceBuilder namespace = new NamespaceBuilder(
                MODULE_NAME,
                className,
                BLOCK_DIRECTORY
        );

        final UiComponentFormButtonData uiComponentFormButtonData = new UiComponentFormButtonData(
                BLOCK_DIRECTORY,
                className,
                MODULE_NAME,
                "Back",
                namespace.getNamespace(),
                "Back Button",
                "20",
                FORM_NAME,
                namespace.getClassFqn()
        );
        final UiComponentFormButtonPhpClassGenerator formButtonPhpClassGenerator =
                new UiComponentFormButtonPhpClassGenerator(
                        uiComponentFormButtonData,
                        project
                );

        final PsiFile file = formButtonPhpClassGenerator.generate(ACTION_NAME);
        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }

    /**
     * Test creation Block type Delete.
     */
    public void testGenerateDeleteButtonBlock() {
        final String filePath = this.getFixturePath("DeleteBlock.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final String className = "DeleteBlock";
        final NamespaceBuilder namespace = new NamespaceBuilder(
                MODULE_NAME,
                className,
                BLOCK_DIRECTORY
        );

        final UiComponentFormButtonData uiComponentFormButtonData = new UiComponentFormButtonData(
                BLOCK_DIRECTORY,
                className,
                MODULE_NAME,
                "Delete",
                namespace.getNamespace(),
                "Delete Entity",
                "30",
                FORM_NAME,
                namespace.getClassFqn()
        );
        final UiComponentFormButtonPhpClassGenerator formButtonPhpClassGenerator =
                new UiComponentFormButtonPhpClassGenerator(
                    uiComponentFormButtonData,
                    project
                );

        final PsiFile file = formButtonPhpClassGenerator.generate(ACTION_NAME);
        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }

    /**
     * Test creation Block custom type.
     */
    public void testGenerateCustomButtonBlock() {
        final String filePath = this.getFixturePath("MyCustom.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final Project project = myFixture.getProject();
        final String className = "MyCustom";
        final NamespaceBuilder namespace = new NamespaceBuilder(
                MODULE_NAME,
                className,
                BLOCK_DIRECTORY
        );

        final UiComponentFormButtonData uiComponentFormButtonData = new UiComponentFormButtonData(
                BLOCK_DIRECTORY,
                className,
                MODULE_NAME,
                "Custom",
                namespace.getNamespace(),
                "My Custom Button",
                "40",
                FORM_NAME,
                namespace.getClassFqn()
        );
        final UiComponentFormButtonPhpClassGenerator formButtonPhpClassGenerator =
                new UiComponentFormButtonPhpClassGenerator(
                    uiComponentFormButtonData,
                    project
                );

        final PsiFile file = formButtonPhpClassGenerator.generate(ACTION_NAME);
        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, file);
    }
}
