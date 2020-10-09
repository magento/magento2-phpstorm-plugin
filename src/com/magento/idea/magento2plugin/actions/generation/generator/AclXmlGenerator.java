/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.google.common.collect.Lists;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.actions.generation.data.AclXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateAclXml;
import com.magento.idea.magento2plugin.magento.files.ModuleAclXml;
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesTreeUtil;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

public class AclXmlGenerator extends FileGenerator {
    private final Project project;
    private final AclXmlData aclXmlData;
    private final String moduleName;
    private final FindOrCreateAclXml findOrCreateAclXml;

    private final List<XmlTag> addSubTagsQueue;
    private final Map<XmlTag, XmlTag> childParentRelationMap;

    /**
     * Constructor.
     *
     * @param aclXmlData AclXmlData
     * @param moduleName String
     * @param project Project
     */
    public AclXmlGenerator(
            final @NotNull AclXmlData aclXmlData,
            final String moduleName,
            final Project project
    ) {
        super(project);
        this.project = project;
        this.moduleName = moduleName;
        this.aclXmlData = aclXmlData;
        findOrCreateAclXml = new FindOrCreateAclXml(project);

        addSubTagsQueue = new LinkedList<>();
        childParentRelationMap = new HashMap<>();
    }

    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile aclXml = (XmlFile) findOrCreateAclXml.execute(
                actionName,
                moduleName
        );
        if (aclXml == null
                || aclXmlData.getResourceId() == null
                || aclXmlData.getResourceTitle() == null) {
            return null;
        }

        final XmlTag rootTag = aclXml.getRootTag();
        XmlTag aclTag = rootTag.findFirstSubTag(ModuleAclXml.XML_TAG_ACL);

        if (aclTag == null) {
            aclTag = rootTag.createChildTag(ModuleAclXml.XML_TAG_ACL, null, "", false);
            addSubTagsQueue.add(aclTag);
            childParentRelationMap.put(aclTag, rootTag);
        }

        XmlTag resourcesTag = aclTag.findFirstSubTag(ModuleAclXml.XML_TAG_RESOURCES);

        if (resourcesTag == null) {
            resourcesTag = aclTag.createChildTag(ModuleAclXml.XML_TAG_RESOURCES, null, "", false);
            addSubTagsQueue.add(resourcesTag);
            childParentRelationMap.put(resourcesTag, aclTag);
        }

        final List<AclXmlData> tree = GetAclResourcesTreeUtil.execute(
                project,
                aclXmlData.getParentResourceId()
        );
        XmlTag parent = resourcesTag;
        for (final AclXmlData resourceTagData : tree) {
            if (resourceTagData.getParentResourceId() != null) {
                parent = resourcesTag.createChildTag(
                        ModuleAclXml.XML_TAG_RESOURCE,
                        null,
                        "",
                        false
                );
                parent.setAttribute(ModuleAclXml.XML_ATTR_ID, resourceTagData.getResourceId());
            }
            parent = createOrGetResourceTag(parent, resourceTagData.getResourceId());
        }
        final XmlTag targetTag = parent.createChildTag(
                ModuleAclXml.XML_TAG_RESOURCE,
                null,
                null,
                false
        );
        targetTag.setAttribute(ModuleAclXml.XML_ATTR_ID, aclXmlData.getResourceId());
        targetTag.setAttribute(ModuleAclXml.XML_ATTR_TITLE, aclXmlData.getResourceTitle());

        addSubTagsQueue.add(targetTag);
        childParentRelationMap.put(targetTag, parent);
        commitAclXmlFile(aclXml);
        FileBasedIndex.getInstance().requestReindex(aclXml.getVirtualFile());

        return aclXml;
    }

    /**
     * Create new tag or get existing one.
     *
     * @param parent XmlTag
     * @param targetResourceId String
     *
     * @return XmlTag
     */
    private XmlTag createOrGetResourceTag(final XmlTag parent, final String targetResourceId) {
        for (final XmlTag tag : parent.getSubTags()) {
            final XmlAttribute idAttribute = tag.getAttribute(ModuleAclXml.XML_ATTR_ID);
            if (idAttribute != null && idAttribute.getValue().equals(targetResourceId)) {
                return tag;
            }
        }
        final XmlTag newTag = parent.createChildTag(ModuleAclXml.XML_TAG_RESOURCE, null, "", false);
        newTag.setAttribute(ModuleAclXml.XML_ATTR_ID, targetResourceId);
        addSubTagsQueue.add(newTag);
        childParentRelationMap.put(newTag, parent);

        return newTag;
    }

    /**
     * Save XML file.
     *
     * @param aclXml XmlFile
     *
     * @return XmlFile
     */
    private XmlFile commitAclXmlFile(final XmlFile aclXml) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (final XmlTag tag : Lists.reverse(addSubTagsQueue)) {
                if (childParentRelationMap.containsKey(tag)) {
                    final XmlTag parent = childParentRelationMap.get(tag);
                    parent.addSubTag(tag, false);
                }
            }
        });
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(aclXml);

        if (document != null) {
            psiDocumentManager.commitDocument(document);
        }
        return aclXml;
    }

    @Override
    protected void fillAttributes(Properties attributes) {} //NOPMD
}
