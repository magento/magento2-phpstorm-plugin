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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.MenuXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateMenuXml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

public class MenuXmlGenerator extends FileGenerator {
    private final MenuXmlData menuXmlData;
    private final Project project;
    private final FindOrCreateMenuXml findOrCreateMenuXml;

    /**
     * Constructor.
     *
     * @param menuXmlData MenuXmlData
     * @param project Project
     */
    public MenuXmlGenerator(
            final @NotNull MenuXmlData menuXmlData,
            final Project project
    ) {
        super(project);
        this.menuXmlData = menuXmlData;
        this.project = project;
        this.findOrCreateMenuXml = new FindOrCreateMenuXml(project);
    }

    /**
     * Creates a module UI form file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile routesXml = (XmlFile) findOrCreateMenuXml.execute(
                actionName,
                menuXmlData.getModuleName()
        );
        final PsiDocumentManager psiDocumentManager =
                PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(routesXml);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = routesXml.getRootTag();
            if (rootTag == null) {
                return;
            }
            XmlTag menuTag = rootTag.findFirstSubTag("menu");
            boolean menuTagIsGenerated = false;
            if (menuTag == null) {
                menuTagIsGenerated = true;
                menuTag = rootTag.createChildTag("menu", null, "", false);
            }
            @NotNull final XmlTag[] buttonsTags = menuTag.findSubTags("add");
            boolean isDeclared = false;
            for (final XmlTag buttonsTag: buttonsTags) {
                @Nullable final XmlAttribute frontName = buttonsTag.getAttribute("id");
                if (frontName.getValue().equals(menuXmlData.getMenuIdentifier())) {
                    isDeclared = true;
                }
            }

            if (!isDeclared) {
                final XmlTag addTag = menuTag.createChildTag("add", null, "", false);
                addTag.setAttribute("id", menuXmlData.getMenuIdentifier());
                addTag.setAttribute("sortOrder", menuXmlData.getSortOrder());
                addTag.setAttribute("title", menuXmlData.getTitle());
                addTag.setAttribute("module", menuXmlData.getModuleName());
                addTag.setAttribute("parent", menuXmlData.getParentMenuItem());
                addTag.setAttribute("resource", menuXmlData.getAcl());
                addTag.setAttribute("translate", "title");
                addTag.setAttribute("action", menuXmlData.getAction());

                menuTag.addSubTag(addTag, false);

                if (menuTagIsGenerated) {
                    rootTag.addSubTag(menuTag, false);
                }
            }

            psiDocumentManager.commitDocument(document);
        });
        return routesXml;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {}//NOPMD
}
