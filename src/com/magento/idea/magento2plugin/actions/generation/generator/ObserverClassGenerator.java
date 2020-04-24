/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Observer;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;

import javax.swing.*;
import java.util.Properties;

public class ObserverClassGenerator extends FileGenerator {
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private ObserverFileData observerFileData;
    private Project project;
    private ValidatorBundle validatorBundle;

    public ObserverClassGenerator(ObserverFileData observerFileData, Project project) {
        super(project);
        this.observerFileData = observerFileData;
        this.project = project;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
    }

    @Override
    public PsiFile generate(String actionName) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass observerClass = GetPhpClassByFQN.getInstance(project).execute(
                    observerFileData.getObserverClassFqn()
            );

            if (observerClass == null) {
                observerClass = createObserverClass(actionName);
            }

            if (observerClass == null) {
                String errorMessage = validatorBundle.message("validator.file.cantBeCreated", "Observer Class");
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Properties attributes = new Properties();
            attributes.setProperty("EVENT_NAME", observerFileData.getTargetEvent());

            String methodTemplate = PhpCodeUtil.getCodeTemplate(
                    Observer.OBSERVER_EXECUTE_TEMPLATE_NAME, attributes, project);


            PsiFile observerFile = observerClass.getContainingFile();
            CodeStyleSettings codeStyleSettings = new CodeStyleSettings((PhpFile) observerFile);
            codeStyleSettings.adjustBeforeWrite();

            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            Document document = psiDocumentManager.getDocument(observerFile);
            int insertPos = getInsertPos(observerClass);
            document.insertString(insertPos, methodTemplate);
            int endPos = insertPos + methodTemplate.length() + 1;
            CodeStyleManager.getInstance(project).reformatText(observerFile, insertPos, endPos);
            psiDocumentManager.commitDocument(document);
            codeStyleSettings.restore();
        });
        PhpClass observerClass = GetPhpClassByFQN.getInstance(project).execute(
                observerFileData.getObserverClassFqn()
        );
        return observerClass.getContainingFile();
    }

    private int getInsertPos(PhpClass observerClass) {
        int insertPos = -1;
        LeafPsiElement[] leafElements = PsiTreeUtil.getChildrenOfType(observerClass, LeafPsiElement.class);
        for (LeafPsiElement leafPsiElement : leafElements) {
            if (!leafPsiElement.getText().equals(MagentoPhpClass.CLOSING_TAG)) {
                continue;
            }
            insertPos = leafPsiElement.getTextOffset();
        }
        return insertPos;
    }

    private PhpClass createObserverClass(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(observerFileData.getObserverModule());
        String[] observerDirectories = observerFileData.getObserverDirectory().split("/");
        for (String observerDirectory : observerDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, observerDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile observerFile = fileFromTemplateGenerator.generate(
                Observer.getInstance(observerFileData.getObserverClassName()),
                attributes,
                parentDirectory,
                actionName
        );
        if (observerFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) observerFile);
    }

    @Override
    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("NAME", observerFileData.getObserverClassName());
        attributes.setProperty("NAMESPACE", observerFileData.getNamespace());
    }

    private boolean checkIfMethodExist(PhpClass observerClass, String methodName) {
        return observerClass.getMethods().stream().anyMatch((method) -> method.getName().equals(methodName));
    }
}
