/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.*;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDiXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

public class PreferenceDiXmlGenerator extends FileGenerator {
    private final GetCodeTemplate getCodeTemplate;
    private final FindOrCreateDiXml findOrCreateDiXml;
    private final XmlFilePositionUtil positionUtil;
    private PreferenceDiXmFileData preferenceDiXmFileData;
    private Project project;

    public PreferenceDiXmlGenerator(@NotNull PreferenceDiXmFileData preferenceDiXmFileData, Project project) {
        super(project);
        this.preferenceDiXmFileData = preferenceDiXmFileData;
        this.project = project;
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
        this.findOrCreateDiXml = FindOrCreateDiXml.getInstance(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    public PsiFile generate(String actionName)
    {
        PsiFile diXmlFile = findOrCreateDiXml.execute(actionName, preferenceDiXmFileData.getPreferenceModule(), preferenceDiXmFileData.getArea());
        boolean isPreferenceDeclared = getTypeAttributeValue((XmlFile) diXmlFile);
        if (isPreferenceDeclared) {
            return null;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplate.execute(ModuleDiXml.TEMPLATE_PREFERENCE, getAttributes()));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int insertPos = positionUtil.getRootInsertPosition((XmlFile) diXmlFile);
            if (textBuf.length() > 0 && insertPos >= 0) {
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(diXmlFile);
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(diXmlFile, insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
        });

        return diXmlFile;
    }

    private boolean getTypeAttributeValue(XmlFile diXml) {
        Collection<XmlAttributeValue> preferences = XmlPsiTreeUtil.findAttributeValueElements(diXml, ModuleDiXml.PREFERENCE_TAG_NAME, ModuleDiXml.PREFERENCE_ATTR_FOR);
        String pluginClassFqn = preferenceDiXmFileData.getTargetClass().getPresentableFQN();
        for (XmlAttributeValue preference: preferences) {
            if (!PhpLangUtil.toPresentableFQN(preference.getValue()).equals(pluginClassFqn)) {
                continue;
            }
            return true;
        }

        return false;
    }

    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("FOR", preferenceDiXmFileData.getTargetClass().getPresentableFQN());
        attributes.setProperty("TYPE", preferenceDiXmFileData.getPreferenceFqn());
    }
}
