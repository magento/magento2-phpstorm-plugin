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
import com.intellij.psi.xml.XmlFile;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverEventsXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateEventsXml;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplateUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import java.io.IOException;
import java.util.Properties;

public class ObserverEventsXmlGenerator extends FileGenerator {
    private final FindOrCreateEventsXml findOrCreateEventsXml;
    private final XmlFilePositionUtil positionUtil;
    private final GetCodeTemplateUtil getCodeTemplateUtil;
    private final ObserverEventsXmlData observerEventsXmlData;
    private final Project project;

    /**
     * Constructor.
     *
     * @param observerEventsXmlData ObserverEventsXmlData
     * @param project Project
     */
    public ObserverEventsXmlGenerator(
            final ObserverEventsXmlData observerEventsXmlData,
            final Project project
    ) {
        super(project);
        this.observerEventsXmlData = observerEventsXmlData;
        this.project = project;
        this.findOrCreateEventsXml = new FindOrCreateEventsXml(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
        this.getCodeTemplateUtil = new GetCodeTemplateUtil(project);
    }

    /**
     * Creates an Observer file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile eventsXmlFile =
                findOrCreateEventsXml.execute(
                        actionName,
                        observerEventsXmlData.getObserverModule(),
                        observerEventsXmlData.getArea()
                );

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplateUtil.execute(
                        ModuleEventsXml.TEMPLATE_OBSERVER,
                        getAttributes())
                );
            } catch (IOException event) {
                return;
            }

            final int insertPos = positionUtil.getRootInsertPosition((XmlFile) eventsXmlFile);
            if (textBuf.length() > 0 && insertPos >= 0) {
                final PsiDocumentManager psiDocumentManager =
                        PsiDocumentManager.getInstance(project);
                final Document document = psiDocumentManager.getDocument(eventsXmlFile);
                document.insertString(insertPos, textBuf);
                final int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project)
                        .reformatText(eventsXmlFile, insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
        });

        return eventsXmlFile;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("OBSERVER_NAME", observerEventsXmlData.getObserverName());
        attributes.setProperty("EVENT_NAME", observerEventsXmlData.getTargetEvent());
        attributes.setProperty("OBSERVER_CLASS", observerEventsXmlData.getObserverClassFqn());
    }
}
