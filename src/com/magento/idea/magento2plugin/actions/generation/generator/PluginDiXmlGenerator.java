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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDiXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class PluginDiXmlGenerator extends FileGenerator {
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final FindOrCreateDiXml findOrCreateDiXml;
    private final XmlFilePositionUtil positionUtil;
    private final PluginDiXmlData pluginFileData;
    private final Project project;
    private boolean isTypeDeclared;

    /**
     * Constructor.
     *
     * @param pluginFileData PluginDiXmlData
     * @param project Project
     */
    public PluginDiXmlGenerator(
            final @NotNull PluginDiXmlData pluginFileData,
            final Project project
    ) {
        super(project);
        this.pluginFileData = pluginFileData;
        this.project = project;
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        this.findOrCreateDiXml = new FindOrCreateDiXml(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    /**
     * Creates a module di.xml file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile diXmlFile = findOrCreateDiXml.execute(
                actionName,
                pluginFileData.getPluginModule(),
                pluginFileData.getArea()
        );
        final XmlAttributeValue typeAttributeValue = getTypeAttributeValue((XmlFile) diXmlFile);
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
            final StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplateUtil.execute(
                        ModuleDiXml.TEMPLATE_PLUGIN,
                        getAttributes())
                );
            } catch (IOException e) {
                return;
            }

            final int insertPos = isTypeDeclared
                    ? positionUtil.getEndPositionOfTag(PsiTreeUtil.getParentOfType(
                            typeAttributeValue,
                    XmlTag.class
            ))
                    : positionUtil.getRootInsertPosition((XmlFile) diXmlFile);
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

    private boolean isPluginDeclared(final XmlAttributeValue typeAttributeValue) {
        final XmlTag xmlTag = PsiTreeUtil.getParentOfType(typeAttributeValue, XmlTag.class);
        final XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(xmlTag, XmlTag.class);
        if (xmlTags == null) {
            return false;
        }
        for (final XmlTag child: xmlTags) {
            if (!child.getName().equals(ModuleDiXml.PLUGIN_TAG_NAME)) {
                continue;
            }
            final XmlAttribute[] xmlAttributes = PsiTreeUtil.getChildrenOfType(
                    child,
                    XmlAttribute.class
                );
            for (final XmlAttribute xmlAttribute: xmlAttributes) {
                if (!xmlAttribute.getName().equals(ModuleDiXml.TYPE_ATTR)) {
                    continue;
                }
                final String declaredClass = PhpLangUtil.toPresentableFQN(xmlAttribute.getValue());
                if (declaredClass.equals(pluginFileData.getPluginFqn())) {
                    return true;
                }
            }
        }

        return false;
    }

    private XmlAttributeValue getTypeAttributeValue(final XmlFile diXml) {
        final Collection<XmlAttributeValue> pluginTypes =
                XmlPsiTreeUtil.findAttributeValueElements(
                        diXml,
                        ModuleDiXml.TYPE_TAG,
                        ModuleDiXml.NAME_ATTR
                    );
        final String pluginClassFqn = pluginFileData.getTargetClass().getPresentableFQN();
        for (final XmlAttributeValue pluginType: pluginTypes) {
            if (PhpLangUtil.toPresentableFQN(pluginType.getValue()).equals(pluginClassFqn)) {
                return pluginType;
            }
        }

        return null;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        if (!isTypeDeclared) {
            attributes.setProperty("TYPE", pluginFileData.getTargetClass().getPresentableFQN());
        }
        attributes.setProperty("NAME", pluginFileData.getPluginName());
        attributes.setProperty("PLUGIN_TYPE", pluginFileData.getPluginFqn());
        attributes.setProperty("PLUGIN_NAME", pluginFileData.getPluginName());
        final String sortOrder = pluginFileData.getSortOrder();
        if (!sortOrder.isEmpty()) {
            attributes.setProperty("SORT_ORDER", sortOrder);
        }
    }
}
