/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileFromTemplateGenerator {

    private final @NotNull Project project;
    private @Nullable String exceptionMessage;

    public FileFromTemplateGenerator(final @NotNull Project project) {
        this.project = project;
    }

    /**
     * Generate file.
     *
     * @param moduleFile ModuleFileInterface
     * @param attributes Properties
     * @param baseDir PsiDirectory
     * @param actionName String
     *
     * @return PsiFile
     */
    public @Nullable PsiFile generate(
            final @NotNull ModuleFileInterface moduleFile,
            final @NotNull Properties attributes,
            final @NotNull PsiDirectory baseDir,
            final @NotNull String actionName
    ) {
        final Ref<PsiFile> fileRef = new Ref<>(null);
        final Ref<String> exceptionRef = new Ref<>(null);
        exceptionMessage = null;//NOPMD
        final String filePath = baseDir.getText().concat("/").concat(moduleFile.getFileName());

        CommandProcessor.getInstance().executeCommand(project, () -> {
            final Runnable run = () -> {
                try {
                    final PsiFile file = createFile(moduleFile, filePath, baseDir, attributes);

                    if (file != null) {
                        fileRef.set(file);
                    }
                } catch (IncorrectOperationException | IOException exception) {
                    exceptionRef.set(exception.getMessage());
                }
            };
            ApplicationManager.getApplication().runWriteAction(run);
        }, actionName, null);

        if (exceptionRef.isNull()) {
            return fileRef.get();
        }
        exceptionMessage = exceptionRef.get();

        return null;
    }

    /**
     * Get last thrown exception message if exists.
     *
     * @return String
     */
    public @Nullable String getLastExceptionMessage() {
        return exceptionMessage;
    }

    @Nullable
    private PsiFile createFile(
            final @NotNull ModuleFileInterface moduleFile,
            final @NotNull String filePath,
            final @NotNull PsiDirectory baseDir,
            final @NotNull Properties attributes
    ) throws IOException {
        final List<String> path = StringUtil.split(filePath.replace(File.separator, "/"), "/");
        final String fileName = path.get(path.size() - 1);
        final PsiFile fileTemplate = createFileFromTemplate(
                getTemplateManager(),
                baseDir,
                moduleFile.getTemplate(),
                attributes,
                fileName,
                moduleFile.getLanguage()
        );

        if (fileTemplate == null) {
            throw new IncorrectOperationException("Template not found!");
        } else {
            PsiElement file;
            file = baseDir.add(fileTemplate);

            if (file instanceof PsiFile) {
                return (PsiFile)file;
            } else {
                return null;
            }
        }
    }

    /**
     * Create file from code template.
     *
     * @param templateManager FileTemplateManager
     * @param directory PsiDirectory
     * @param templateName String
     * @param properties Properties
     * @param fileName String
     * @param language Language
     * @return PsiFile
     * @throws IOException exception
     */
    public PsiFile createFileFromTemplate(
            final @NotNull FileTemplateManager templateManager,
            final @NotNull PsiDirectory directory,
            final @NotNull String templateName,
            final @NotNull Properties properties,
            final @NotNull String fileName,
            final @NotNull Language language
    ) throws IOException {
        FileTemplate fileTemplate;

        try {
            fileTemplate = templateManager.getInternalTemplate(templateName);
        } catch (IllegalStateException e) {
            fileTemplate = templateManager.getInstance(project).getCodeTemplate(templateName);
        }

        Properties mergedProperties = new Properties();
        mergedProperties.putAll(FileTemplateManager.getInstance(project).getDefaultProperties());
        mergedProperties.putAll(properties);
        final String fileTemplateText = fileTemplate.getText(mergedProperties);
        final PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(
                fileName,
                language,
                fileTemplateText,
                true,
                false
        );

        if (fileTemplate.isReformatCode()) {
            CodeStyleManager.getInstance(project).reformat(file);
        }

        return file;
    }

    public FileTemplateManager getTemplateManager() {
        return FileTemplateManager.getInstance(project);
    }
}
