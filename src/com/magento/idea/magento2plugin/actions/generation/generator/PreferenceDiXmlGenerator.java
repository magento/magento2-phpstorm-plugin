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
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.actions.generation.data.PreferenceDiXmFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDiXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class PreferenceDiXmlGenerator extends FileGenerator {
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final FindOrCreateDiXml findOrCreateDiXml;
    private final XmlFilePositionUtil positionUtil;
    private final PreferenceDiXmFileData preferenceDiXmFileData;
    private final Project project;

    /**
     * Constructor.
     *
     * @param preferenceDiXmFileData PreferenceDiXmFileData
     * @param project Project
     */
    public PreferenceDiXmlGenerator(
            final @NotNull PreferenceDiXmFileData preferenceDiXmFileData,
            final Project project
    ) {
        super(project);

        this.preferenceDiXmFileData = preferenceDiXmFileData;
        this.project = project;
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        this.findOrCreateDiXml = new FindOrCreateDiXml(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile diXmlFile = findOrCreateDiXml.execute(
                actionName,
                preferenceDiXmFileData.getPreferenceModule(),
                preferenceDiXmFileData.getArea()
        );

        final boolean isPreferenceDeclared = getTypeAttributeValue((XmlFile) diXmlFile);
        if (isPreferenceDeclared) {
            return null;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            final StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplateUtil.execute(
                        ModuleDiXml.TEMPLATE_PREFERENCE,
                        getAttributes())
                );
            } catch (IOException e) {
                return;
            }

            final int insertPos = positionUtil.getRootInsertPosition((XmlFile) diXmlFile);
            if (textBuf.length() > 0 && insertPos >= 0) {
                final PsiDocumentManager psiDocumentManager =
                        PsiDocumentManager.getInstance(project);
                final Document document = psiDocumentManager.getDocument(diXmlFile);
                document.insertString(insertPos, textBuf);
                final int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(diXmlFile, insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
        });

        return diXmlFile;
    }

    private boolean getTypeAttributeValue(final XmlFile diXml) {
        final Collection<XmlAttributeValue> preferences = XmlPsiTreeUtil
                .findAttributeValueElements(
                        diXml,
                        ModuleDiXml.PREFERENCE_TAG_NAME,
                        ModuleDiXml.PREFERENCE_ATTR_FOR
                );
        final String fqn = preferenceDiXmFileData.getTargetClass().getPresentableFQN();
        for (final XmlAttributeValue preference: preferences) {
            if (PhpLangUtil.toPresentableFQN(preference.getValue()).equals(fqn)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("FOR", preferenceDiXmFileData.getTargetClass()
                .getPresentableFQN());
        attributes.setProperty("TYPE", preferenceDiXmFileData.getPreferenceFqn());
    }
}
