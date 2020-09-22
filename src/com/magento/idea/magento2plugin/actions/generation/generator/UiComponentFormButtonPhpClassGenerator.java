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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.FormButtonBlockPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.io.IOException;
import java.util.Properties;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

public class UiComponentFormButtonPhpClassGenerator extends FileGenerator {
    private final UiComponentFormButtonData buttonData;
    private final Project project;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetCodeTemplate getCodeTemplate;

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
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
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
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass buttonClass = createButton(actionName);
            if (buttonClass == null) {
                String errorTitle = commonBundle.message("common.error");
                String errorMessage = validatorBundle.message("validator.file.cantBeCreated", "Plugin Class");
                JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

                return;
            }

            StringBuffer textBuf = new StringBuffer();
            String template = getMethodTemplateByButtonType(buttonData.getButtonType());
            if (template == null) {
                return;
            }
            try {
                textBuf.append(getCodeTemplate.execute(
                        template,
                        fillCodeTemplateAttributes()
                    )
                );
            } catch (IOException e) {
                return;
            }

            buttonClassFile[0] = buttonClass.getContainingFile();
            CodeStyleSettings codeStyleSettings = new CodeStyleSettings((PhpFile) buttonClassFile[0]);
            codeStyleSettings.adjustBeforeWrite();

            int insertPos = getInsertPos(buttonClass);
            if (textBuf.length() > 0 && insertPos >= 0) {
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(buttonClassFile[0]);
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(buttonClassFile[0], insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
            codeStyleSettings.restore();
        });

        return buttonClassFile[0];
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
                FileFromTemplateGenerator.getInstance(project);

        final String moduleName = buttonData.getButtonModule();
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(moduleName);
        String[] directories = buttonData.getButtonDirectory().split(File.separator);
        for (String pluginDirectory: directories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, pluginDirectory);
        }

        @Nullable PsiFile buttonFile = fileFromTemplateGenerator.generate(
                FormButtonBlockPhp.getInstance(buttonData.getButtonClassName()),
                getAttributes(),
                parentDirectory,
                actionName
        );
        return getFirstClassOfFile.execute((PhpFile) buttonFile);
    }

    private int getInsertPos(PhpClass phpClass) {
        int insertPos = -1;
        Method[] phpMethods =  PsiTreeUtil.getChildrenOfType(phpClass, Method.class);
        for (Method phpMethod: phpMethods) {
            if (!phpMethod.getName().equals(FormButtonBlockPhp.DEFAULT_METHOD)) {
                continue;
            }
            GroupStatement[] groupStatements =  PsiTreeUtil.getChildrenOfType(phpMethod, GroupStatement.class);
            for (GroupStatement groupStatement: groupStatements) {
                PsiElement[] elements =  PsiTreeUtil.getChildrenOfType(groupStatement, PsiElement.class);
                for (PsiElement element: elements) {
                    if (!element.getText().equals(MagentoPhpClass.CLOSING_TAG)) {
                        continue;
                    }
                    insertPos = element.getTextOffset();
                }
            }
        }

        return insertPos;
    }

    protected Properties fillCodeTemplateAttributes() {
        final Properties attributes = new Properties();
        attributes.setProperty("NAME", CamelCaseToHyphen.getInstance()
                .convert(buttonData.getButtonClassName()));
        attributes.setProperty("FQN", buttonData.getFqn());
        return attributes;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", buttonData.getButtonClassName());
        attributes.setProperty("NAMESPACE", buttonData.getNamespace());
    }

    private String getMethodTemplateByButtonType(String buttonType) {
        String template;
        switch (buttonType) {
            case FormButtonBlockPhp.TYPE_SAVE:
                template = FormButtonBlockPhp.SAVE_METHOD_TEMPLATE;
                break;
            case FormButtonBlockPhp.TYPE_BACK:
                template = FormButtonBlockPhp.BACK_METHOD_TEMPLATE;
                break;
            case FormButtonBlockPhp.TYPE_DELETE:
                template = FormButtonBlockPhp.DELETE_METHOD_TEMPLATE;
                break;
            default:
                template = null;
        }
        return template;
    }
}
