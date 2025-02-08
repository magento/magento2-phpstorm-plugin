/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.code.util;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.generator.util.CommitXmlFileUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.DiArgumentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class DiXmlTagManipulatorUtil {

    private DiXmlTagManipulatorUtil() {}

    /**
     * Insert new tag.
     *
     * @param parentTag XmlTag
     * @param tagName String
     * @param attributes List[Pair[String, String]]
     *
     * @return XmlTag
     */
    public static XmlTag insertTag(
            final @NotNull XmlTag parentTag,
            final @NotNull String tagName,
            final List<Pair<String, String>> attributes
    ) {
        XmlTag tag = parentTag.createChildTag(
                tagName,
                null,
                "",
                false
        );

        for (final Pair<String, String> attributeData : attributes) {
            tag.setAttribute(attributeData.getFirst(), attributeData.getSecond());
        }
        final Map<XmlTag, XmlTag> childParentRelationMap = new HashMap<>();
        childParentRelationMap.put(tag, parentTag);

        CommitXmlFileUtil.execute(
                (XmlFile) parentTag.getContainingFile(),
                List.of(tag),
                childParentRelationMap
        );

        for (final XmlTag childTag : PsiTreeUtil.findChildrenOfType(parentTag, XmlTag.class)) {
            if (childTag.getText().equals(tag.getText())) {
                tag = childTag;
            }
        }

        return tag;
    }

    /**
     * Wrap Type/VirtualType argument value into corresponding XML.
     *
     * @param typeTag XmlTag
     * @param name String
     * @param type DiArgumentType
     * @param value String
     */
    @SuppressWarnings("PMD.CognitiveComplexity")
    public static void insertArgumentInTypeTag(
            final @NotNull XmlTag typeTag,
            final @NotNull String name,
            final @NotNull DiArgumentType type,
            final @NotNull String value
    ) {
        final List<XmlTag> addSubTagsQueue = new ArrayList<>();
        final Map<XmlTag, XmlTag> childParentRelationMap = new HashMap<>();
        final XmlTag argumentsTag = getOrCreateArgumentsTag(
                typeTag,
                addSubTagsQueue,
                childParentRelationMap
        );
        boolean isExists = false;

        for (final XmlTag argTag : PsiTreeUtil.findChildrenOfType(argumentsTag, XmlTag.class)) {
            final String argName = argTag.getAttributeValue(ModuleDiXml.NAME_ATTR);

            if (name.equals(argName)) {
                CommitXmlFileUtil.execute(
                        (XmlFile) typeTag.getContainingFile(),
                        () -> {
                            argTag.setAttribute(ModuleDiXml.XSI_TYPE_ATTR, type.getArgumentType());

                            if (type.equals(DiArgumentType.ARRAY)) {
                                argTag.getValue().setEscapedText("");
                                String arrayValue = value;
                                boolean isNotChanged = false;

                                while (!arrayValue.isEmpty() && !isNotChanged) {
                                    final XmlTag xmlValueTag = XmlElementFactory.getInstance(
                                            argTag.getProject()
                                    ).createTagFromText(arrayValue);
                                    argTag.addSubTag(xmlValueTag, false);

                                    final String newArrayValue = arrayValue.replace(
                                            xmlValueTag.getText(),
                                            ""
                                    );

                                    if (newArrayValue.equals(arrayValue)) {
                                        isNotChanged = true;
                                    }
                                    arrayValue = newArrayValue;
                                }
                            } else {
                                argTag.getValue().setText(value);
                            }

                            if (value.isEmpty()) {
                                argTag.collapseIfEmpty();
                            }
                        }
                );
                isExists = true;
                break;
            }
        }

        if (!isExists) {
            final XmlTag argTag = argumentsTag.createChildTag(
                    ModuleDiXml.ARGUMENT_TAG,
                    null,
                    value.isEmpty() ? null : value,
                    false
            );
            addSubTagsQueue.add(argTag);
            childParentRelationMap.put(argTag, argumentsTag);
            argTag.setAttribute(ModuleDiXml.NAME_ATTR, name);
            argTag.setAttribute(ModuleDiXml.XSI_TYPE_ATTR, type.getArgumentType());
        }

        if (!addSubTagsQueue.isEmpty()) {
            CommitXmlFileUtil.execute(
                    (XmlFile) typeTag.getContainingFile(),
                    addSubTagsQueue,
                    childParentRelationMap
            );
        }
    }

    private static XmlTag getOrCreateArgumentsTag(
            final @NotNull XmlTag typeTag,
            final List<XmlTag> addSubTagsQueue,
            final Map<XmlTag, XmlTag> childParentRelationMap
    ) {
        for (final XmlTag tag : PsiTreeUtil.findChildrenOfType(typeTag, XmlTag.class)) {
            if (ModuleDiXml.ARGUMENTS_TAG.equals(tag.getName())) {
                return tag;
            }
        }
        final XmlTag argumentsTag = typeTag.createChildTag(
                ModuleDiXml.ARGUMENTS_TAG,
                null,
                "",
                false
        );
        addSubTagsQueue.add(argumentsTag);
        childParentRelationMap.put(argumentsTag, typeTag);

        return argumentsTag;
    }
}
