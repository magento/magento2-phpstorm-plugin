/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.xml;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import gnu.trove.THashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.LocalVariableCouldBeFinal",
        "PMD.CommentSize",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.UseUtilityClass",
        "PMD.OnlyOneReturn",
        "PMD.UseObjectForClearerAPI"
})
public class XmlPsiTreeUtil {
    public static final String EVENT_TAG_NAME = "event";
    public static final String OBSERVER_TAG_NAME = "observer";
    public static final String NAME_ATTRIBUTE = "name";

    /**
     * Get type tag of an argument.
     *
     * @param psiArgumentValueElement value element
     * @return xml tag
     */
    @Nullable
    public static XmlTag getTypeTagOfArgument(final XmlElement psiArgumentValueElement) {

        final XmlTag argumentTag = PsiTreeUtil.getParentOfType(
                psiArgumentValueElement,
                XmlTag.class
        );
        final XmlTag argumentsTag = PsiTreeUtil.getParentOfType(argumentTag, XmlTag.class);

        return PsiTreeUtil.getParentOfType(argumentsTag, XmlTag.class);
    }

    /**
     * Finds observer tags by event-observer name combination.
     */
    @SuppressWarnings({
            "PMD.CyclomaticComplexity"
    })
    public static Collection<XmlAttributeValue> findObserverTags(
            final XmlFile xmlFile,
            final String eventName,
            final String observerName
    ) {
        Collection<XmlAttributeValue> psiElements = new THashSet<>();
        final XmlTag configTag = xmlFile.getRootTag();

        if (configTag == null) {
            return psiElements;
        }

        /* Loop through event tags */
        for (XmlTag eventTag: configTag.getSubTags()) {
            XmlAttribute eventNameAttribute = eventTag.getAttribute(NAME_ATTRIBUTE);

            /* Check if event tag and name matches */
            if (EVENT_TAG_NAME.equals(eventTag.getName())
                    && eventNameAttribute != null
                    && eventName.equals(eventNameAttribute.getValue())
            ) {
                /* Loop through observer tags under matched event tag */
                for (XmlTag observerTag: eventTag.getSubTags()) {
                    XmlAttribute observerNameAttribute = observerTag.getAttribute(NAME_ATTRIBUTE);

                    /* Check if observer tag and name matches */
                    if (OBSERVER_TAG_NAME.equals(observerTag.getName())
                            && observerNameAttribute != null
                            && observerNameAttribute.getValueElement() != null
                            && observerName.equals(observerNameAttribute.getValue())
                    ) {
                        psiElements.add(observerNameAttribute.getValueElement());
                    }
                }
            }
        }

        return psiElements;
    }

    /**
     * Find attribute value elements based on the tag name.
     *
     * @param xmlFile xml file
     * @param tagName xml tag name
     * @param attributeName xml attribute name
     * @return Collection of xml attributes.
     */
    public static Collection<XmlAttributeValue> findAttributeValueElements(
            final XmlFile xmlFile,
            final String tagName,
            final String attributeName) {
        final Collection<XmlAttributeValue> psiElements = new THashSet<>();
        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) {
            return psiElements;
        }

        for (XmlTag tag: rootTag.findSubTags(tagName)) {
            if (tag != null) {
                XmlAttribute attribute = tag.getAttribute(attributeName);
                if (attribute != null && attribute.getValueElement() != null) {
                    psiElements.add(attribute.getValueElement());
                }
            }
        }

        return psiElements;
    }

    /**
     * Search for the attribute value elements.
     *
     * @param xmlFile xml file
     * @param tagName tag name
     * @param attributeName attribute name
     * @param value value
     * @return collection of xml elements
     */
    public static Collection<XmlAttributeValue> findAttributeValueElements(
            final XmlFile xmlFile,
            final String tagName,
            final String attributeName,
            final String value) {

        Collection<XmlAttributeValue> psiElements = findAttributeValueElements(
                xmlFile,
                tagName,
                attributeName
        );

        psiElements.removeIf(e -> e.getValue() == null || !e.getValue().equals(value));
        return psiElements;
    }

    /**
     * Return collection of arguments items.
     *
     * @param xmlFile xml file
     * @param parentTagName parent node name
     * @param parentTagAttributeName parent attribute name
     * @param parentTagAttributeNameValue parent attribute name value
     * @param argumentTagAttributeName argument attribute name
     * @return collection of xml elements
     */
    public static Collection<XmlAttributeValue> findTypeArgumentsItemValueElement(
            final XmlFile xmlFile,
            final String parentTagName,
            final String parentTagAttributeName,
            final String parentTagAttributeNameValue,
            final String argumentTagAttributeName) {
        Collection<XmlAttributeValue> psiElements = new THashSet<>();

        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) {
            return psiElements;
        }

        for (XmlTag parentTag: rootTag.findSubTags(parentTagName)) {
            if (parentTag != null && parentTag.findSubTags(ModuleDiXml.ARGUMENTS_TAG).length > 0) {
                XmlAttribute parentAttribute = parentTag.getAttribute(parentTagAttributeName);
                if (parentAttribute != null
                        && parentAttribute.getValueElement() != null
                        && Objects.equals(parentAttribute.getValue(), parentTagAttributeNameValue)
                ) {
                    iterateArgumentsNode(
                            parentTag,
                            argumentTagAttributeName,
                            psiElements
                    );
                }
            }
        }

        return psiElements;
    }

    private static Collection<XmlAttributeValue> iterateArgumentsNode(
            final XmlTag parentTag,
            final String argumentTagAttributeName,
            Collection<XmlAttributeValue> psiElements
    ) {
        for (XmlTag argumentsTag: parentTag.findSubTags(ModuleDiXml.ARGUMENTS_TAG)) {
            if (argumentsTag != null
                    && argumentsTag.findSubTags(ModuleDiXml.ARGUMENT_TAG).length > 0) {
                for (XmlTag argumentTag: argumentsTag.findSubTags(ModuleDiXml.ARGUMENT_TAG)) {
                    XmlAttribute argumentAttribute = argumentTag.getAttribute(ModuleDiXml.NAME_TAG);
                    if (argumentAttribute != null
                            && argumentAttribute.getValueElement() != null
                            && Objects.equals(
                                    argumentAttribute.getValue(),
                            argumentTagAttributeName)
                    ) {
                        psiElements.add(argumentAttribute.getValueElement());
                    }
                }
            }
        }

        return psiElements;
    }

    /**
     * Get child tags of parent.
     *
     * @param parentTag XmlTag
     * @param subTagsName String
     *
     * @return List
     */
    public static List<XmlTag> findSubTagsOfParent(
            final XmlTag parentTag,
            final String subTagsName
    ) {
        return new LinkedList<>(Arrays.asList(parentTag.findSubTags(subTagsName)));
    }
}
