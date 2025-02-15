/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.MessageQueueClassData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.MessageQueueClassPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public class MessageQueueClassGenerator extends FileGenerator {

    private final MessageQueueClassData messageQueueClassDataName;
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final String moduleName;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final List<String> errors = new ArrayList<>();

    /**
     * Message queue handler constructor.
     *
     * @param messageQueueClassData MessageQueueHandlerData
     * @param moduleName String
     * @param project Project
     */
    public MessageQueueClassGenerator(
            final MessageQueueClassData messageQueueClassData,
            final String moduleName,
            final Project project
    ) {
        super(project);

        this.messageQueueClassDataName = messageQueueClassData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.project = project;
        this.moduleName = moduleName;
    }

    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile[] handlerFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass handler = GetPhpClassByFQN.getInstance(project).execute(
                    messageQueueClassDataName.getFqn()
            );

            if (handler != null) {
                errors.add(validatorBundle.message(
                        "validator.file.alreadyExists",
                        "Handler Class"
                ));
                return;
            }
            handler = createHandlerClass(actionName);

            if (handler == null) {
                errors.add(validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Handler Class"
                ));
                return;
            }

            handlerFiles[0] = handler.getContainingFile();
        });

        for (final String errorMessage : errors) {
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    commonBundle.message("common.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }

        return handlerFiles[0];
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAMESPACE", messageQueueClassDataName.getNamespace());
        attributes.setProperty("CLASS_NAME", messageQueueClassDataName.getName());
    }

    private PhpClass createHandlerClass(final String actionName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(this.moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final PsiFile handlerFile;
        final String[] handlerDirectories = messageQueueClassDataName.getPath().split(
                File.separator
        );
        for (final String handlerDirectory: handlerDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory, handlerDirectory
            );
        }

        final Properties attributes = getAttributes();

        handlerFile = fileFromTemplateGenerator.generate(
                new MessageQueueClassPhp(
                        messageQueueClassDataName.getName(),
                        messageQueueClassDataName.getType()
                ),
                attributes,
                parentDirectory,
                actionName
        );

        if (handlerFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) handlerFile);
    }
}
