/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;

public class ModuleControllerClassGeneratorTest extends BaseGeneratorTestCase {
    /**
     * Test generation of frontend controller file.
     */
    public void testGenerateFrontendControllerFile() {
        final Project project = myFixture.getProject();
        final ControllerFileData controllerFileData = new ControllerFileData(
                "Controller/Entity",
                "GetAction",
                "Foo_Bar",
                "frontend",
                "GET",
                "",
                false,
                "Foo\\Bar\\Controller\\Entity"
        );
        final ModuleControllerClassGenerator generator = new ModuleControllerClassGenerator(
                controllerFileData,
                project
        );
        final PsiFile controllerFile = generator.generate("test");
        final String filePath = this.getFixturePath("GetAction.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Controller/Entity",
                controllerFile
        );
    }

    /**
     * Test generation of frontend controller file with action class inheritance.
     */
    public void testGenerateFrontendInheritActionControllerFile() {
        final Project project = myFixture.getProject();
        final ControllerFileData controllerFileData = new ControllerFileData(
                "Controller/Entity",
                "DeleteAction",
                "Foo_Bar",
                "frontend",
                "DELETE",
                "",
                true,
                "Foo\\Bar\\Controller\\Entity"
        );
        final ModuleControllerClassGenerator generator = new ModuleControllerClassGenerator(
                controllerFileData,
                project
        );
        final PsiFile controllerFile = generator.generate("test");
        final String filePath = this.getFixturePath("DeleteAction.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Controller/Entity",
                controllerFile
        );
    }

    /**
     * Test generation of backend controller file.
     */
    public void testGenerateBackendControllerFile() {
        final Project project = myFixture.getProject();
        final ControllerFileData controllerFileData = new ControllerFileData(
                "Controller/Adminhtml/Entity",
                "BackendSaveAction",
                "Foo_Bar",
                "adminhtml",
                "POST",
                "Foo_Bar::entity",
                true,
                "Foo\\Bar\\Controller\\Adminhtml\\Entity"
        );
        final ModuleControllerClassGenerator generator = new ModuleControllerClassGenerator(
                controllerFileData,
                project
        );
        final PsiFile controllerFile = generator.generate("test");
        final String filePath = this.getFixturePath("BackendSaveAction.php");
        final PsiFile expectedFile = myFixture.configureByFile(filePath);

        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Controller/Adminhtml/Entity",
                controllerFile
        );
    }
}
