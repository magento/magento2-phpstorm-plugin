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
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ControllerPhp;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Collections;

public class ModuleControllerClassGenerator extends FileGenerator {
    private ControllerFileData controllerFileData;
    private Project project;
    private ValidatorBundle validatorBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    public ModuleControllerClassGenerator(@NotNull ControllerFileData controllerFileData, Project project) {
        super(project);
        this.project = project;
        this.controllerFileData = controllerFileData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
    }

    public PsiFile generate(String actionName) {
        final PsiFile[] controllerFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass controller = GetPhpClassByFQN.getInstance(project).execute(getControllerFqn());

            if (controller != null) {
                String errorMessage = this.validatorBundle.message("validator.file.alreadyExists", "Controller Class");
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            controller = createControllerClass(actionName);

            if (controller == null) {
                String errorMessage = this.validatorBundle.message("validator.file.cantBeCreated", "Controller Class");
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Properties attributes = getControllerActionContentsAttributes();
            String methodTemplate = PhpCodeUtil.getCodeTemplate(
                    ControllerPhp.CONTROLLER_ACTION_CONTENTS_TEMPLATE_NAME,
                    attributes,
                    project
            );
            controllerFiles[0] = controller.getContainingFile();

            CodeStyleSettings codeStyleSettings = new CodeStyleSettings((PhpFile) controllerFiles[0]);
            codeStyleSettings.adjustBeforeWrite();

            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            Document document = psiDocumentManager.getDocument(controllerFiles[0]);
            int insertPos = getInsertPos(controller);
            document.insertString(insertPos, methodTemplate);
            int endPos = insertPos + methodTemplate.length() + 1;
            CodeStyleManager.getInstance(project).reformatText(controllerFiles[0], insertPos, endPos);
            psiDocumentManager.commitDocument(document);
            codeStyleSettings.restore();
        });

        return controllerFiles[0];
    }

    private int getInsertPos(PhpClass controllerClass) {
        int insertPos = -1;
        LeafPsiElement[] leafElements = PsiTreeUtil.getChildrenOfType(controllerClass, LeafPsiElement.class);
        for (LeafPsiElement leafPsiElement: leafElements) {
            if (!leafPsiElement.getText().equals(MagentoPhpClass.CLOSING_TAG)) {
                continue;
            }
            insertPos = leafPsiElement.getTextOffset();
        }
        return insertPos;
    }

    @NotNull
    private String getControllerFqn() {
        return controllerFileData.getNamespace() + Package.FQN_SEPARATOR + controllerFileData.getActionClassName();
    }

    private PhpClass createControllerClass(String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(getControllerModule());
        String[] controllerDirectories = controllerFileData.getActionDirectory().split(File.separator);
        for (String controllerDirectory: controllerDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, controllerDirectory);
        }

        Properties attributes = getAttributes();
        PsiFile controllerFile = fileFromTemplateGenerator.generate(
                ControllerPhp.getInstance(controllerFileData.getActionClassName()),
                attributes,
                parentDirectory,
                actionName
        );

        if (controllerFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) controllerFile);
    }

    protected void fillAttributes(Properties attributes) {
        ArrayList<String> uses = getUses();
        String actionClassName = controllerFileData.getActionClassName();
        attributes.setProperty("NAME", actionClassName);
        attributes.setProperty("NAMESPACE", controllerFileData.getNamespace());
        String httpMethodInterface = ControllerPhp.getHttpMethodInterfaceByMethod(
                controllerFileData.getHttpMethodName()
        );
        attributes.setProperty("IMPLEMENTS", getNameFromFqn(httpMethodInterface));
        uses.add(httpMethodInterface);

        if (controllerFileData.getIsInheritClass()) {
            if (controllerFileData.getControllerArea().equals("adminhtml")) {
                uses.add(ControllerPhp.ADMINHTML_CONTROLLER_FQN);
                attributes.setProperty("EXTENDS", getNameFromFqn(ControllerPhp.ADMINHTML_CONTROLLER_FQN));
            } else {
                uses.add(ControllerPhp.FRONTEND_CONTROLLER_FQN);
                attributes.setProperty("EXTENDS", getNameFromFqn(ControllerPhp.FRONTEND_CONTROLLER_FQN));
            }
        }

        attributes.setProperty("USES", formatUses(uses));
    }

    private ArrayList<String> getUses()
    {
        return new ArrayList<>(Arrays.asList(
                "Magento\\Framework\\App\\ResponseInterface",
                "Magento\\Framework\\Controller\\ResultInterface",
                "Magento\\Framework\\Exception\\NotFoundException"
        ));
    }

    private String formatUses(ArrayList<String> uses)
    {
        Collections.sort(uses);

        return String.join(",", uses);
    }

    protected Properties getControllerActionContentsAttributes() {
        Properties attributes = new Properties();

        if (controllerFileData.getAcl().isEmpty()) {
            return attributes;
        }

        if (controllerFileData.getIsInheritClass() && controllerFileData.getControllerArea().equals("adminhtml")) {
            attributes.setProperty("ACL", getNameFromFqn(controllerFileData.getAcl()));
        }

        return attributes;
    }

    private String getNameFromFqn(String fqn) {
        String[] fqnArray = fqn.split("\\\\");
        return fqnArray[fqnArray.length - 1];
    }

    public String getControllerModule() {
        return controllerFileData.getControllerModule();
    }
}
