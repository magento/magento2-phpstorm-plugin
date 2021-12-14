/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleComposerJsonGeneratorTest extends BaseGeneratorTestCase {

    /**
     * Test for the module composer.json generation with dependencies.
     */
    public void testGenerateModuleFile() {
        final String filePath = this.getFixturePath(ComposerJson.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiDirectory projectDir = getProjectDirectory();

        final String expectedDirectory =
                projectDir.getVirtualFile().getPath() + "/TestWithDependencies/Test";
        final PsiFile composerJson = generateComposerJson(
                true,
                projectDir,
                true,
                "TestWithDependencies");

        assertGeneratedFileIsCorrect(
                expectedFile,
                expectedDirectory,
                composerJson
        );
    }

    /**
     * Test for generation the composer.json with dependencies in the root directory.
     */
    public void testGenerateFileInRoot() {
        final String filePath = this.getFixturePath(ComposerJson.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiDirectory projectDir = getProjectDirectory();

        final String composerJsonDirPath = projectDir.getVirtualFile().getPath();
        final PsiFile composerJson = generateComposerJson(
                false,
                projectDir,
                true,
                "TestWithDependencies");

        assertGeneratedFileIsCorrect(expectedFile, composerJsonDirPath, composerJson);
    }

    /**
     * Test case for the composer.json generation without dependencies.
     */
    public void testGenerateModuleFileWithoutDependencies() {
        final String filePath = this.getFixturePath(ComposerJson.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiDirectory projectDir = getProjectDirectory();
        final String expectedDirectory = projectDir.getVirtualFile().getPath()
                + "/TestWithoutDependencies/Test";
        final PsiFile composerJson = generateComposerJson(
                true,
                projectDir,
                false,
                "TestWithoutDependencies");

        assertGeneratedFileIsCorrect(
                expectedFile,
                expectedDirectory,
                composerJson
        );
    }

    /**
     * Generate composer.json file for tests.
     *
     * @param createModuleDirectories create module directory flag
     * @param projectDir project directory
     * @param withDependencies generate composer.json with dependencies or not
     * @param packageName the package name of the test module
     * @return PsiFile
     */
    private PsiFile generateComposerJson(
            final boolean createModuleDirectories,
            final PsiDirectory projectDir,
            final boolean withDependencies,
            final String packageName) {
        final Project project = myFixture.getProject();
        final List<String> dependencies = withDependencies
                ? new ArrayList<>(Arrays.asList("Foo_Bar", "Magento_Backend"))
                : new ArrayList<>(Arrays.asList("Foo_BarWithOutComposer"));
        final List<String> licenses = new ArrayList<>(
                Arrays.asList("Test License 1", "Test License 2")
        );
        final ModuleComposerJsonData composerJsonData = new ModuleComposerJsonData(
                packageName,
                "Test",
                projectDir,
                "test-description",
                "test/module-test",
                "1.0.0-dev",
                licenses,
                dependencies,
                createModuleDirectories
        );
        final ModuleComposerJsonGenerator composerJsonGenerator =
                new ModuleComposerJsonGenerator(composerJsonData, project);
        return composerJsonGenerator.generate("test");
    }
}
