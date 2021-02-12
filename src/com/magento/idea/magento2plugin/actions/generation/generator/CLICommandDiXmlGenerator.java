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
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.CLICommandXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDiXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.UselessParentheses"
})
public class CLICommandDiXmlGenerator extends FileGenerator {
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final FindOrCreateDiXml findOrCreateDiXml;
    private final XmlFilePositionUtil positionUtil;
    private final CLICommandXmlData cliCommandXmlData;
    private final Project project;
    private boolean isDeclared;

    /**
     * Initialize CLI command in the module's di.xml.
     *
     * @param project Project
     * @param cliCommandXmlData ciCommandXmlData
     */
    public CLICommandDiXmlGenerator(
            final Project project,
            final @NotNull CLICommandXmlData cliCommandXmlData) {
        super(project);
        this.cliCommandXmlData = cliCommandXmlData;
        this.project = project;
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        this.findOrCreateDiXml = new FindOrCreateDiXml(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    @Override
    public PsiFile generate(final String actionName) {
        final Areas areas = Areas.getAreaByString("base");
        final PsiFile diXmlFile = findOrCreateDiXml.execute(
                actionName,
                cliCommandXmlData.getCLICommandModule(),
                areas.toString()
        );
        final XmlAttributeValue argumentsTag = getCLICommandArgumentsTag((XmlFile) diXmlFile);
        this.isDeclared = (argumentsTag != null);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final StringBuffer textBuf = new StringBuffer();
            try {
                final String template = getCodeTemplateUtil.execute(
                        ModuleDiXml.TEMPLATE_CLI_COMMAND,
                        getAttributes()
                );
                textBuf.append(template);
            } catch (IOException exception) {

                return;
            }

            final int insertPos = this.isDeclared
                    ? positionUtil.getEndPositionOfTag(
                            PsiTreeUtil.getParentOfType(argumentsTag,
                                    XmlTag.class))
                    : positionUtil.getRootInsertPosition((XmlFile) diXmlFile);
            if (textBuf.length() > 0 && insertPos >= 0) {
                final PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
                final Document document = manager.getDocument(diXmlFile);
                assert document != null;
                document.insertString(insertPos, textBuf);
                final int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(diXmlFile, insertPos, endPos);
                manager.commitDocument(document);
            }
        });

        return diXmlFile;
    }

    @Override
    protected final void fillAttributes(final Properties attributes) {
        if (!this.isDeclared) {
            attributes.setProperty("CLI_COMMAND_INTERFACE", ModuleDiXml.CLI_COMMAND_INTERFACE);
        }
        attributes.setProperty("CLI_COMMAND_DI_NAME", cliCommandXmlData.getCliCommandDiName());
        attributes.setProperty("CLI_COMMAND_CLASS", cliCommandXmlData.getCliCommandClass());
    }

    private XmlAttributeValue getCLICommandArgumentsTag(final XmlFile diXml) {
        final Collection<XmlAttributeValue> argumentsTag =
                XmlPsiTreeUtil.findTypeArgumentsItemValueElement(
                    diXml,
                    ModuleDiXml.TYPE_TAG,
                    ModuleDiXml.NAME_ATTR,
                    ModuleDiXml.CLI_COMMAND_INTERFACE,
                    ModuleDiXml.CLI_COMMAND_ATTR_COMMANDS
                );
        return (argumentsTag.isEmpty()) ? null : argumentsTag.iterator().next();
    }
}
