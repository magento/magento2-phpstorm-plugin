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
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDiXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

public class CLICommandDiXmlGenerator extends FileGenerator {
    private final GetCodeTemplate getCodeTemplate;
    private final FindOrCreateDiXml findOrCreateDiXml;
    private final XmlFilePositionUtil positionUtil;
    private final CLICommandXmlData cliCommandXmlData;
    private final Project project;
    private boolean isCLICommandDeclared;

    public CLICommandDiXmlGenerator(Project project, @NotNull CLICommandXmlData cliCommandXmlData) {
        super(project);
        this.cliCommandXmlData = cliCommandXmlData;
        this.project = project;
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
        this.findOrCreateDiXml = FindOrCreateDiXml.getInstance(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    public PsiFile generate(String actionName)
    {
        Package.Areas areas = Package.getAreaByString("base");
        PsiFile diXmlFile = findOrCreateDiXml.execute(actionName, cliCommandXmlData.getCLICommandModule(), areas.toString());
        XmlAttributeValue cliCommandsAttribute = getCLICommandArgumentsTag((XmlFile) diXmlFile);
        this.isCLICommandDeclared = (cliCommandsAttribute != null);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplate.execute(ModuleDiXml.TEMPLATE_CLI_COMMAND, getAttributes()));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int insertPos = this.isCLICommandDeclared
                    ? positionUtil.getEndPositionOfTag(PsiTreeUtil.getParentOfType(cliCommandsAttribute, XmlTag.class))
                    : positionUtil.getRootInsertPosition((XmlFile) diXmlFile);
            if (textBuf.length() > 0 && insertPos >= 0) {
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(diXmlFile);
                assert document != null;
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(diXmlFile, insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
        });

        return diXmlFile;
    }

    private XmlAttributeValue getCLICommandArgumentsTag(XmlFile diXml) {
        Collection<XmlAttributeValue> cliCommandArguments = XmlPsiTreeUtil.findTypeArgumentsItemValueElement(
                diXml,
                ModuleDiXml.CLI_COMMAND_TAG,
                ModuleDiXml.CLI_COMMAND_ATTR_NAME,
                ModuleDiXml.CLI_COMMAND_INTERFACE,
                ModuleDiXml.CLI_COMMAND_ATTR_COMMANDS
        );
        return (cliCommandArguments.size() > 0) ?
                cliCommandArguments.iterator().next()
                : null;
    }

    protected void fillAttributes(Properties attributes) {
        if (!this.isCLICommandDeclared) {
            attributes.setProperty("CLI_COMMAND_INTERFACE", ModuleDiXml.CLI_COMMAND_INTERFACE);
        }
        attributes.setProperty("CLI_COMMAND_DI_NAME", cliCommandXmlData.getCliCommandDiName());
        attributes.setProperty("CLI_COMMAND_CLASS", cliCommandXmlData.getCliCommandClass());
    }
}
