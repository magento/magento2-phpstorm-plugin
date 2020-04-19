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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDiXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.*;

public class PluginDiXmlGenerator extends FileGenerator {
    private final GetCodeTemplate getCodeTemplate;
    private final FindOrCreateDiXml findOrCreateDiXml;
    private final XmlFilePositionUtil positionUtil;
    private PluginDiXmlData pluginFileData;
    private Project project;
    private boolean isTypeDeclared;

    public PluginDiXmlGenerator(@NotNull PluginDiXmlData pluginFileData, Project project) {
        super(project);
        this.pluginFileData = pluginFileData;
        this.project = project;
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
        this.findOrCreateDiXml = FindOrCreateDiXml.getInstance(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    public PsiFile generate(String actionName)
    {
        PsiFile diXmlFile = findOrCreateDiXml.execute(actionName, pluginFileData.getPluginModule(), pluginFileData.getArea());
        XmlAttributeValue typeAttributeValue = getTypeAttributeValue((XmlFile) diXmlFile);
        boolean isPluginDeclared = false;
        this.isTypeDeclared = false;
        if (typeAttributeValue != null) {
            this.isTypeDeclared = true;
            isPluginDeclared = isPluginDeclared(typeAttributeValue);
        }
        if (isPluginDeclared) {
            return null;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplate.execute(ModuleDiXml.TEMPLATE_PLUGIN, getAttributes()));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int insertPos = isTypeDeclared
                    ? positionUtil.getEndPositionOfTag(PsiTreeUtil.getParentOfType(typeAttributeValue, XmlTag.class))
                    : positionUtil.getRootInsertPosition((XmlFile) diXmlFile);
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

    private boolean isPluginDeclared(XmlAttributeValue typeAttributeValue) {
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(typeAttributeValue, XmlTag.class);
        XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(xmlTag, XmlTag.class);
        if (xmlTags == null) {
            return false;
        }
        for (XmlTag child: xmlTags) {
            if (!child.getName().equals(ModuleDiXml.PLUGIN_TAG_NAME)) {
                continue;
            }
            XmlAttribute[] xmlAttributes = PsiTreeUtil.getChildrenOfType(child, XmlAttribute.class);
            for (XmlAttribute xmlAttribute: xmlAttributes) {
                if (!xmlAttribute.getName().equals(ModuleDiXml.PLUGIN_TYPE_ATTRIBUTE)) {
                    continue;
                }
                String declaredClass = PhpLangUtil.toPresentableFQN(xmlAttribute.getValue());
                if (declaredClass.equals(pluginFileData.getPluginFqn())) {
                    return true;
                }
            }
        }

        return false;
    }

    private XmlAttributeValue getTypeAttributeValue(XmlFile diXml) {
        Collection<XmlAttributeValue> pluginTypes = XmlPsiTreeUtil.findAttributeValueElements(diXml, ModuleDiXml.PLUGIN_TYPE_TAG, ModuleDiXml.PLUGIN_TYPE_ATTR_NAME);
        String pluginClassFqn = pluginFileData.getTargetClass().getPresentableFQN();
        for (XmlAttributeValue pluginType: pluginTypes) {
            if (!PhpLangUtil.toPresentableFQN(pluginType.getValue()).equals(pluginClassFqn)) {
                continue;
            }
            return pluginType;
        }

        return null;
    }

    protected void fillAttributes(Properties attributes) {
        if (!isTypeDeclared) {
            attributes.setProperty("TYPE", pluginFileData.getTargetClass().getPresentableFQN());
        }
        attributes.setProperty("NAME", pluginFileData.getPluginName());
        attributes.setProperty("PLUGIN_TYPE", pluginFileData.getPluginFqn());
        attributes.setProperty("PLUGIN_NAME", pluginFileData.getPluginName());
        attributes.setProperty("SORT_ORDER", pluginFileData.getSortOrder());
    }
}
