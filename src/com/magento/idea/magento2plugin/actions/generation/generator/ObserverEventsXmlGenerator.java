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
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.generator.util.XmlFilePositionUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;

import java.io.IOException;
import java.util.Properties;

public class ObserverEventsXmlGenerator extends FileGenerator {
    private final FindOrCreateEventsXml findOrCreateEventsXml;
    private final XmlFilePositionUtil positionUtil;
    private final GetCodeTemplate getCodeTemplate;
    private ObserverEventsXmlData observerEventsXmlData;
    private Project project;

    public ObserverEventsXmlGenerator(ObserverEventsXmlData observerEventsXmlData, Project project) {
        super(project);
        this.observerEventsXmlData = observerEventsXmlData;
        this.project = project;
        this.findOrCreateEventsXml = FindOrCreateEventsXml.getInstance(project);
        this.positionUtil = XmlFilePositionUtil.getInstance();
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
    }

    public PsiFile generate(String actionName) {
        PsiFile eventsXmlFile =
                findOrCreateEventsXml.execute(
                        actionName,
                        observerEventsXmlData.getObserverModule(),
                        observerEventsXmlData.getArea()
                );

        WriteCommandAction.runWriteCommandAction(project, () -> {
            StringBuffer textBuf = new StringBuffer();
            try {
                textBuf.append(getCodeTemplate.execute(ModuleEventsXml.TEMPLATE_OBSERVER, getAttributes()));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int insertPos = positionUtil.getRootInsertPosition((XmlFile) eventsXmlFile);
            if (textBuf.length() > 0 && insertPos >= 0) {
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                Document document = psiDocumentManager.getDocument(eventsXmlFile);
                document.insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length() + 1;
                CodeStyleManager.getInstance(project).reformatText(eventsXmlFile, insertPos, endPos);
                psiDocumentManager.commitDocument(document);
            }
        });

        return eventsXmlFile;
    }

    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("OBSERVER_NAME", observerEventsXmlData.getObserverName());
        attributes.setProperty("EVENT_NAME", observerEventsXmlData.getTargetEvent());
        attributes.setProperty("OBSERVER_CLASS", observerEventsXmlData.getObserverClassFqn());
    }
}
