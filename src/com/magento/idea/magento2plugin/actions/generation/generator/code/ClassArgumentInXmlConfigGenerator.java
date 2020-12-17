/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.code;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.SmartList;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.CommonXml;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class ClassArgumentInXmlConfigGenerator {
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final String argumentName;
    private final String argumentType;
    private final String argumentValue;
    private final XmlFilePositionUtil positionUtil;
    private final Project project;

    /**
     * Constructor.
     *
     * @param project Project
     * @param argumentName String
     * @param argumentType String
     * @param argumentValue String
     */
    public ClassArgumentInXmlConfigGenerator(
            final Project project,
            final String argumentName,
            final String argumentType,
            final String argumentValue
    ) {
        this.project = project;
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
        this.argumentName = argumentName;
        this.argumentType = argumentType;
        this.argumentValue = argumentValue;
        this.positionUtil = XmlFilePositionUtil.getInstance();
    }

    /**
     * Injects arguments to XML tag.
     *
     * @param xmlTag XmlTag
     * @return PsiFile
     */
    public PsiFile generate(final XmlTag xmlTag) {
        final PsiFile layoutFile = xmlTag.getContainingFile();
        final AtomicReference<XmlTag> argumentsTag = new AtomicReference<>(getArgumentsTag(xmlTag));

        final boolean isInitialTag = isInitialTag(argumentsTag.get());
        WriteCommandAction.runWriteCommandAction(project, () -> {
            final StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplateUtil.execute(
                        LayoutXml.ARGUMENTS_TEMPLATE,
                        fillAttributes()
                    )
                );
            } catch (IOException e) {
                return;
            }

            if (isInitialTag) {
                final XmlTag childTag = xmlTag.createChildTag(
                        CommonXml.ATTRIBUTE_ARGUMENTS, "", "", false
                );
                xmlTag.addSubTag(childTag, true);
                argumentsTag.set(getArgumentsTag(xmlTag));
            }
            final PsiDocumentManager psiDocumentManager =
                    PsiDocumentManager.getInstance(project);
            final Document document = psiDocumentManager.getDocument(layoutFile);
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            final int insertPos = positionUtil.getEndPositionOfTag(argumentsTag.get());

            if (textBuf.length() > 0 && insertPos >= 0) {
                document.insertString(insertPos, textBuf);

                final int endPos = insertPos + textBuf.length() - 1;
                final List<TextRange> ranges = getSelectedRanges(insertPos, endPos);

                CodeStyleManager.getInstance(project).reformatText(
                        layoutFile,
                        ranges
                );

                psiDocumentManager.commitDocument(document);
            }
        });

        return layoutFile;
    }

    protected List<TextRange> getSelectedRanges(final int insertPosition, final int endPosition) {
        final int argumentsTagLength = 13;
        final List<TextRange> ranges = new SmartList<>();

        final TextRange insertRange = TextRange.create(
                insertPosition - argumentsTagLength,
                insertPosition + 1
        );
        ranges.add(insertRange);

        final TextRange endRange = TextRange.create(
                endPosition,
                endPosition + argumentsTagLength
        );
        ranges.add(endRange);

        return ranges;
    }

    protected boolean isInitialTag(final XmlTag argumentsTag) {
        boolean isInitialTag = false;
        if (argumentsTag == null) {
            isInitialTag = true;
        }
        return isInitialTag;
    }

    protected XmlTag getArgumentsTag(final XmlTag xmlTag) {
        final XmlTag[] subTags = xmlTag.getSubTags();
        XmlTag argumentsTag = null;
        for (final XmlTag subTag: subTags) {
            if (subTag.getName().equals(CommonXml.ATTRIBUTE_ARGUMENTS)) {
                argumentsTag = subTag;
            }
        }

        return argumentsTag;
    }

    protected Properties fillAttributes() {
        final Properties attributes = new Properties();
        attributes.setProperty("ARGUMENT_NAME", argumentName);
        attributes.setProperty("ARGUMENT_TYPE", argumentType);
        attributes.setProperty("ARGUMENT_VALUE", argumentValue);
        return attributes;
    }
}
