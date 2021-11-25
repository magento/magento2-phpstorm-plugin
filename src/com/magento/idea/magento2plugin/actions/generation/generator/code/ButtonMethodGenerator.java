/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.code;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.FormButtonBlockPhp;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class ButtonMethodGenerator {

    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final UiComponentFormButtonData buttonData;
    private final Project project;

    /**
     * Constructor.
     *
     * @param buttonData UiComponentFormButtonData
     * @param project Project
     */
    public ButtonMethodGenerator(
            final @NotNull UiComponentFormButtonData buttonData,
            final Project project
    ) {
        this.buttonData = buttonData;
        this.project = project;
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
    }

    /**
     * Injects method to the button block class.
     *
     * @param buttonClassFile PsiFile[]
     * @param buttonClass PhpClass
     */
    public void generate(PsiFile[] buttonClassFile, final PhpClass buttonClass) {
        final String template = getMethodTemplateByButtonType(buttonData.getButtonType());
        if (template.isEmpty()) {
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            if (buttonClass == null) {
                final String errorTitle = commonBundle.message("common.error");
                final String errorMessage = validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Plugin Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        errorTitle,
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            final StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplateUtil.execute(
                        template,
                        fillCodeTemplateAttributes()
                    )
                );
            } catch (IOException e) {
                return;
            }

            buttonClassFile[0] = buttonClass.getContainingFile();
            final CodeStyleSettings codeStyleSettings = new CodeStyleSettings(
                    (PhpFile) buttonClassFile[0]
            );
            codeStyleSettings.adjustBeforeWrite();

            final int insertPos = getInsertPos(buttonClass);
            if (textBuf.length() > 0 && insertPos >= 0) {
                final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(
                        project
                );
                final Document document = psiDocumentManager.getDocument(buttonClassFile[0]);
                document.insertString(insertPos, textBuf);
                final int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(
                        buttonClassFile[0],
                        insertPos,
                        endPos
                );
                psiDocumentManager.commitDocument(document);
            }
            codeStyleSettings.restore();
        });
    }


    protected Properties fillCodeTemplateAttributes() {
        final Properties attributes = new Properties();
        attributes.setProperty("NAME", CamelCaseToHyphen.getInstance()
                .convert(buttonData.getButtonClassName()));
        attributes.setProperty("FQN", buttonData.getFqn());
        attributes.setProperty("BUTTON_SORT_ORDER", buttonData.getButtonSortOrder());
        attributes.setProperty("FORM_NAME", buttonData.getFormName());
        attributes.setProperty("BUTTON_LABEL", buttonData.getButtonLabel());
        return attributes;
    }

    private int getInsertPos(final PhpClass phpClass) {
        int insertPos = -1;
        final Method[] phpMethods =  PsiTreeUtil.getChildrenOfType(phpClass, Method.class);
        for (final Method phpMethod: phpMethods) {
            if (!phpMethod.getName().equals(FormButtonBlockPhp.DEFAULT_METHOD)) {
                continue;
            }
            final GroupStatement[] groupStatements = PsiTreeUtil.getChildrenOfType(
                phpMethod,
                GroupStatement.class
            );
            for (final GroupStatement groupStatement: groupStatements) {
                final PsiElement[] elements = PsiTreeUtil.getChildrenOfType(
                    groupStatement,
                    PsiElement.class
                );
                for (final PsiElement element: elements) {
                    if (!element.getText().equals(MagentoPhpClass.CLOSING_TAG)) {
                        continue;
                    }
                    insertPos = element.getTextOffset();
                }
            }
        }

        return insertPos;
    }

    private String getMethodTemplateByButtonType(final String buttonType) {
        String template;
        switch (buttonType) {
            case FormButtonBlockPhp.TYPE_SAVE:
                template = FormButtonBlockPhp.saveMethodTemplate;
                break;
            case FormButtonBlockPhp.TYPE_BACK:
                template = FormButtonBlockPhp.backMethodTemplate;
                break;
            case FormButtonBlockPhp.TYPE_DELETE:
                template = FormButtonBlockPhp.deleteMethodTemplate;
                break;
            default:
                template = "";
                break;
        }
        return template;
    }
}
