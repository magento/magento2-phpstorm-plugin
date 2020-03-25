/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.magento.idea.magento2plugin.magento.files.ModuleFileInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class FileFromTemplateGenerator {
    private static FileFromTemplateGenerator INSTANCE = null;
    private Project project;

    public static FileFromTemplateGenerator getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new FileFromTemplateGenerator();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    @Nullable
    public PsiFile generate(@NotNull ModuleFileInterface moduleFile, @NotNull Properties attributes, @NotNull PsiDirectory baseDir, @NotNull String actionName) {
        Ref<PsiFile> fileRef = new Ref(null);
        Ref<String> exceptionRef = new Ref(null);
        String filePath = baseDir.getText().concat("/").concat(moduleFile.getFileName());
        CommandProcessor.getInstance().executeCommand(project, () -> {
            Runnable run = () -> {
                try {
                    PsiFile file = createFile(moduleFile, filePath, baseDir, attributes);
                    if (file != null) {
                        fileRef.set(file);
                    }
                } catch (IncorrectOperationException | IOException var9) {
                    exceptionRef.set(var9.getMessage());
                }

            };
            ApplicationManager.getApplication().runWriteAction(run);
        }, actionName, null);
        if (!exceptionRef.isNull()) {
            Messages.showErrorDialog(exceptionRef.get(), actionName);
            return null;
        } else {
            return fileRef.get();
        }
    }

    @Nullable
    private PsiFile createFile(@NotNull ModuleFileInterface moduleFile, @NotNull String filePath, @NotNull PsiDirectory baseDir, @NotNull Properties attributes) throws IOException, IncorrectOperationException {
        List<String> path = StringUtil.split(filePath.replace(File.separator, "/"), "/");
        String fileName = path.get(path.size() - 1);
        PsiFile fileTemplate = createFileFromTemplate(getTestTemplateManager(), baseDir, moduleFile.getTemplate(), attributes, fileName, moduleFile.getLanguage());
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

    public PsiFile createFileFromTemplate(@NotNull FileTemplateManager templateManager, @NotNull PsiDirectory directory, @NotNull String templateName, @NotNull Properties properties, @NotNull String fileName, @NotNull Language language) throws IOException {
        FileTemplate fileTemplate = templateManager.getInternalTemplate(templateName);
        fillDefaultProperties(templateManager, properties, directory);
        String fileTemplateText = fileTemplate.getText(properties);
        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName, language, fileTemplateText, true, false);
        if (fileTemplate.isReformatCode()) {
            CodeStyleManager.getInstance(project).reformat(file);
        }

        return file;
    }

    public void fillDefaultProperties(@NotNull FileTemplateManager templateManager, @NotNull Properties props, @NotNull PsiDirectory directory) {
        Properties hardCodedProperties = templateManager.getDefaultProperties();
        Iterator iterator = hardCodedProperties.keySet().iterator();

        while(iterator.hasNext()) {
            Object propertyKey = iterator.next();
            props.setProperty((String)propertyKey, hardCodedProperties.getProperty((String)propertyKey));
        }

        iterator = DefaultTemplatePropertiesProvider.EP_NAME.getExtensionList().iterator();

        while(iterator.hasNext()) {
            DefaultTemplatePropertiesProvider provider = (DefaultTemplatePropertiesProvider)iterator.next();
            provider.fillProperties(directory, props);
        }
    }

    public FileTemplateManager getTestTemplateManager() {
        return FileTemplateManager.getInstance(project);
    }
}
