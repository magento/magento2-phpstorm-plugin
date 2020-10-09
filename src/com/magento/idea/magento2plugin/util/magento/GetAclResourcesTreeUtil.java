/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.actions.generation.data.AclXmlData;
import com.magento.idea.magento2plugin.magento.files.ModuleAclXml;
import com.magento.idea.magento2plugin.stubs.indexes.xml.AclResourceIndex;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class GetAclResourcesTreeUtil {

    private GetAclResourcesTreeUtil() {}

    /**
     * Get acl resources tree list for specified acl resource id.
     *
     * @return List
     */
    public static List<AclXmlData> execute(
            final Project project,
            final String targetAclResourceId
    ) {
        final Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(
                        AclResourceIndex.KEY,
                        targetAclResourceId,
                        GlobalSearchScope.allScope(project)
                );

        if (virtualFiles.isEmpty()) {
            return null;
        }
        final VirtualFile virtualFile = virtualFiles.iterator().next();

        final XmlFile aclXml = (XmlFile) PsiManager.getInstance(project).findFile(virtualFile);
        final XmlTag rootTag = aclXml.getRootTag();
        final List<AclXmlData> aclXmlDataList = new LinkedList<>();

        if (rootTag != null) {
            final XmlTag aclTag = rootTag.findFirstSubTag(ModuleAclXml.XML_TAG_ACL);
            final XmlTag resourcesTag = aclTag.findFirstSubTag(ModuleAclXml.XML_TAG_RESOURCES);

            List<XmlTag> resourceTagsList = new LinkedList<>();
            resourceTagsList = addAclXmlDataRecursively(
                    targetAclResourceId,
                    resourcesTag,
                    resourceTagsList
            );

            for (final XmlTag resourceTag : Lists.reverse(resourceTagsList)) {
                final String parentTagId =
                        resourcesTag.getParentTag().getAttributeValue(ModuleAclXml.XML_ATTR_ID);

                final AclXmlData resourceTagData = new AclXmlData(//NOPMD
                        parentTagId,
                        resourceTag.getAttributeValue(ModuleAclXml.XML_ATTR_ID),
                        resourceTag.getAttributeValue(ModuleAclXml.XML_ATTR_TITLE)
                );
                aclXmlDataList.add(resourceTagData);
            }
        }
        return aclXmlDataList;
    }

    /**
     * Add acl xml data for resource tags to list recursively.
     *
     * @param targetAclResourceId String
     * @param parentTag XmlTag
     * @param resourceTagsList List
     *
     * @return List
     */
    private static List<XmlTag> addAclXmlDataRecursively(
            final String targetAclResourceId,
            final XmlTag parentTag,
            List<XmlTag> resourceTagsList//NOPMD
    ) {
        final List<XmlTag> resourceTags = XmlPsiTreeUtil.findSubTagsOfParent(
                parentTag,
                ModuleAclXml.XML_TAG_RESOURCE
        );
        for (final XmlTag tag : resourceTags) {
            if (tag.getAttributeValue(ModuleAclXml.XML_ATTR_ID).equals(targetAclResourceId)) {
                resourceTagsList.add(tag);
                break;
            } else {
                resourceTagsList = addAclXmlDataRecursively(
                        targetAclResourceId,
                        tag,
                        resourceTagsList
                );
                if (!resourceTagsList.isEmpty()) {
                    final XmlTag lastAdded = resourceTagsList.get(resourceTagsList.size() - 1);
                    if (lastAdded.getParentTag()
                            .getAttributeValue(ModuleAclXml.XML_ATTR_ID)
                            .equals(tag.getAttributeValue(ModuleAclXml.XML_ATTR_ID))) {
                        resourceTagsList.add(tag);
                        break;
                    }
                }
            }
        }
        return resourceTagsList;
    }
}
