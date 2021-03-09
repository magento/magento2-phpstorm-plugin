/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.ButtonMethodGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.FormButtonBlockPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UiComponentFormButtonPhpClassGenerator extends FileGenerator {
    private final UiComponentFormButtonData buttonData;
    private final Project project;
    private final GetFirstClassOfFile getFirstClassOfFile;

    /**
     * Constructor.
     *
     * @param buttonData UiComponentFormButtonData
     * @param project Project
     */
    public UiComponentFormButtonPhpClassGenerator(
            final @NotNull UiComponentFormButtonData buttonData,
            final Project project
    ) {
        super(project);
        this.buttonData = buttonData;
        this.project = project;
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
    }

    /**
     * Creates a module UI form file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile[] buttonClassFile = {null};
        final PhpClass buttonClass = createButton(actionName);
        new ButtonMethodGenerator(buttonData, project).generate(buttonClassFile, buttonClass);

        return buttonClass.getContainingFile();
    }

    /**
     * Finds or Button PHP class.
     *
     * @param actionName String
     * @return PhpClass
     */
    protected PhpClass createButton(
            final String actionName
    ) {
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                new FileFromTemplateGenerator(project);

        final String moduleName = buttonData.getButtonModule();
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(moduleName);
        final String[] directories = buttonData.getButtonDirectory().split(File.separator);
        for (final String pluginDirectory: directories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    pluginDirectory
            );
        }

        @Nullable final PsiFile buttonFile = fileFromTemplateGenerator.generate(
                new FormButtonBlockPhp(buttonData.getButtonClassName()),
                getAttributes(),
                parentDirectory,
                actionName
        );
        return getFirstClassOfFile.execute((PhpFile) buttonFile);
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", buttonData.getButtonClassName());
        attributes.setProperty("NAMESPACE", buttonData.getNamespace());
    }
}
