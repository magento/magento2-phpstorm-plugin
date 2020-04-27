package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;

public class ModuleControllerClassGeneratorTest extends BaseGeneratorTestCase {

    public void testGenerateFrontendControllerFile() {
        Project project = myFixture.getProject();
        ControllerFileData controllerFileData = new ControllerFileData(
                "Controller/Entity",
                "GetAction",
                "Foo_Bar",
                "frontend",
                "GET",
                "",
                false,
                "Foo\\Bar\\Controller\\Entity"
        );
        ModuleControllerClassGenerator moduleControllerClassGenerator = new ModuleControllerClassGenerator(
                controllerFileData,
                project
        );
        PsiFile controllerFile = moduleControllerClassGenerator.generate("test");

        String filePath = this.getFixturePath("GetAction.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Controller/Entity",
                controllerFile
        );
    }

    public void testGenerateFrontendInheritActionControllerFile() {
        Project project = myFixture.getProject();
        ControllerFileData controllerFileData = new ControllerFileData(
                "Controller/Entity",
                "DeleteAction",
                "Foo_Bar",
                "frontend",
                "DELETE",
                "",
                true,
                "Foo\\Bar\\Controller\\Entity"
        );
        ModuleControllerClassGenerator moduleControllerClassGenerator = new ModuleControllerClassGenerator(
                controllerFileData,
                project
        );
        PsiFile controllerFile = moduleControllerClassGenerator.generate("test");

        String filePath = this.getFixturePath("DeleteAction.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Controller/Entity",
                controllerFile
        );
    }

    public void testGenerateBackendControllerFile() {
        Project project = myFixture.getProject();
        ControllerFileData controllerFileData = new ControllerFileData(
                "Controller/Adminhtml/Entity",
                "BackendSaveAction",
                "Foo_Bar",
                "adminhtml",
                "POST",
                "Foo_Bar::entity",
                true,
                "Foo\\Bar\\Controller\\Adminhtml\\Entity"
        );
        ModuleControllerClassGenerator moduleControllerClassGenerator = new ModuleControllerClassGenerator(
                controllerFileData,
                project
        );
        PsiFile controllerFile = moduleControllerClassGenerator.generate("test");

        String filePath = this.getFixturePath("BackendSaveAction.php");
        PsiFile expectedFile = myFixture.configureByFile(filePath);
        assertGeneratedFileIsCorrect(
                expectedFile,
                "src/app/code/Foo/Bar/Controller/Adminhtml/Entity",
                controllerFile
        );
    }
}