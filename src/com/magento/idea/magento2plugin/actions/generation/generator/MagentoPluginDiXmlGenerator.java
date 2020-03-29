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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.xml.util.XmlUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.actions.generation.CreateAPluginAction;
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginDiXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import com.magento.idea.magento2plugin.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.*;

public class MagentoPluginDiXmlGenerator {
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final GetCodeTemplate getCodeTemplate;
    private MagentoPluginDiXmlData pluginFileData;
    private Project project;

    public MagentoPluginDiXmlGenerator(@NotNull MagentoPluginDiXmlData pluginFileData, Project project) {
        this.pluginFileData = pluginFileData;
        this.project = project;
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
    }

    public void generate()
    {
        PsiFile diXmlFile = findOrCreateDiXml();
        XmlAttributeValue typeAttributeValue = getTypeAttributeValue((XmlFile) diXmlFile);
        boolean isPluginDeclared = false;
        boolean isTypeDeclared = false;
        if (typeAttributeValue != null) {
            isTypeDeclared = true;
            isPluginDeclared = isPluginDeclared(typeAttributeValue);
        }
        if (isPluginDeclared) {
            return;
        }
        boolean finalIsTypeDeclared = isTypeDeclared;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplate.execute(ModuleDiXml.TEMPLATE_PLUGIN, getAttributes(finalIsTypeDeclared)));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int insertPos = finalIsTypeDeclared
                    ? getEndPositionOfTag(PsiTreeUtil.getParentOfType(typeAttributeValue, XmlTag.class))
                    : getRootInsertPosition((XmlFile) diXmlFile);
            if (textBuf.length() > 0 && insertPos >= 0) {
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(diXmlFile);
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(diXmlFile, insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
        });
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

    private Properties getAttributes(boolean isTypeDeclared) {
        Properties attributes = new Properties();
        this.fillAttributes(attributes, isTypeDeclared);
        return attributes;
    }

    private void fillAttributes(Properties attributes, boolean isTypeDeclared) {
        if (!isTypeDeclared) {
            attributes.setProperty("TYPE", pluginFileData.getTargetClass().getPresentableFQN());
        }
        attributes.setProperty("NAME", pluginFileData.getPluginName());
        attributes.setProperty("PLUGIN_TYPE", pluginFileData.getPluginFqn());
        attributes.setProperty("PLUGIN_NAME", pluginFileData.getPluginName());
        attributes.setProperty("SORT_ORDER", pluginFileData.getSortOrder());
    }

    private PsiFile findOrCreateDiXml() {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project).getModuleDirectoryByModuleName(pluginFileData.getPluginModule());
        ArrayList<String> pluginDirectories = new ArrayList<>();
        pluginDirectories.add(Package.MODULE_BASE_AREA_DIR);
        if (!getArea().equals(Package.Areas.base)) {
            pluginDirectories.add(getArea().toString());
        }
        for (String pluginDirectory: pluginDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, pluginDirectory);
        }
        ModuleDiXml moduleDiXml = new ModuleDiXml();
        PsiFile diXml = FileBasedIndexUtil.findModuleConfigFile(
                moduleDiXml.getFileName(),
                getArea(),
                pluginFileData.getPluginModule(),
                project
        );
        if (diXml == null) {
            diXml = fileFromTemplateGenerator.generate(moduleDiXml, new Properties(), parentDirectory, CreateAPluginAction.ACTION_NAME);
        }
        return diXml;
    }

    public Package.Areas getArea() {
        return Package.getAreaByString(pluginFileData.getArea());
    }

    private int getRootInsertPosition(XmlFile xmlFile) {
        int insertPos = -1;
        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) {
            return insertPos;
        }
        return getEndPositionOfTag(rootTag);
    }

    private int getEndPositionOfTag(XmlTag tag) {
        PsiElement tagEnd = XmlUtil.getTokenOfType(tag, XmlTokenType.XML_END_TAG_START);
        if (tagEnd == null) {
            return -1;
        }

        return tagEnd.getTextOffset();
    }
}
