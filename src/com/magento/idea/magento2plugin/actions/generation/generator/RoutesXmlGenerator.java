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
import com.magento.idea.magento2plugin.actions.generation.data.RoutesXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateRoutesXml;
import com.magento.idea.magento2plugin.magento.files.RoutesXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoutesXmlGenerator extends FileGenerator {
    private final RoutesXmlData routesXmlData;
    private final Project project;
    private final FindOrCreateRoutesXml findOrCreateRoutesXml;

    /**
     * Constructor.
     *
     * @param routesXmlData RoutesXmlData
     * @param project Project
     */
    public RoutesXmlGenerator(
            final @NotNull RoutesXmlData routesXmlData,
            final Project project
    ) {
        super(project);
        this.routesXmlData = routesXmlData;
        this.project = project;
        this.findOrCreateRoutesXml = new FindOrCreateRoutesXml(project);
    }

    /**
     * Creates a module UI form file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    @SuppressWarnings("PMD.CognitiveComplexity")
    public PsiFile generate(final String actionName) {
        final XmlFile routesXml = (XmlFile) findOrCreateRoutesXml.execute(
                actionName,
                routesXmlData.getModuleName(),
                routesXmlData.getArea()
        );
        final PsiDocumentManager psiDocumentManager =
                PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(routesXml);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            final XmlTag rootTag = routesXml.getRootTag();
            if (rootTag == null) {
                return;
            }
            XmlTag routerTag = rootTag.findFirstSubTag("router");
            boolean routerTagIsGenerated = false;
            if (routerTag == null) {
                routerTagIsGenerated = true;
                routerTag = rootTag.createChildTag("router", null, "", false);
                routerTag.setAttribute(
                        "id",
                        routesXmlData.getArea().equals(Areas.frontend.toString())
                            ? RoutesXml.ROUTER_ID_STANDARD
                            : RoutesXml.ROUTER_ID_ADMIN
                );
            }
            @NotNull final XmlTag[] buttonsTags = routerTag.findSubTags("route");
            boolean isDeclared = false;
            for (final XmlTag buttonsTag: buttonsTags) {
                @Nullable final XmlAttribute frontName = buttonsTag.getAttribute("frontName");
                if (frontName.getValue().equals(routesXmlData.getRoute())) {
                    isDeclared = true;
                }
            }

            if (!isDeclared) {
                final XmlTag routeTag = routerTag.createChildTag("route", null, "", false);
                routeTag.setAttribute("id", routesXmlData.getRoute());
                routeTag.setAttribute("frontName",routesXmlData.getRoute());
                final XmlTag moduleTag = routeTag.createChildTag("module", null, null, false);
                moduleTag.setAttribute("name", routesXmlData.getModuleName());
                routeTag.addSubTag(moduleTag, false);
                routerTag.addSubTag(routeTag, false);

                if (routerTagIsGenerated) {
                    rootTag.addSubTag(routerTag, false);
                }
            }

            psiDocumentManager.commitDocument(document);
        });
        return routesXml;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {}//NOPMD
}
