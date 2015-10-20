package com.magento.idea.magento2plugin.xml.di;

import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;

/**
 * Created by Warider on 17.08.2015.
 */
public class XmlHelper {
    /**
     * <type name="\Namespace\Class">
     */
    public static XmlAttributeValuePattern getDiTypePattern() {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute("name")
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                            .withName("type")
                    )
            ).inside(
                XmlHelper.getInsideTagPattern("type")
            ).inFile(XmlHelper.getXmlFilePattern());
    }

    /**
     * <preference type="\Namespace\Class">
     */
    public static XmlAttributeValuePattern getDiPreferenceTypePattern() {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute("type")
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                            .withName("preference")
                    )
            ).inside(
                XmlHelper.getInsideTagPattern("preference")
            ).inFile(XmlHelper.getXmlFilePattern());
    }

    /**
     * <preference for="\Namespace\Type">
     */
    public static XmlAttributeValuePattern getDiPreferenceForPattern() {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute("for")
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                            .withName("preference")
                    )
            ).inside(
                XmlHelper.getInsideTagPattern("preference")
            ).inFile(XmlHelper.getXmlFilePattern());
    }

    /**
     * <virtualType type="\Namespace\Class">
     */
    public static XmlAttributeValuePattern getDiVirtualTypePattern() {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute("type")
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                            .withName("virtualType")
                    )
            ).inside(
                XmlHelper.getInsideTagPattern("virtualType")
            ).inFile(XmlHelper.getXmlFilePattern());
    }

    /**
     * <argument name="argumentName" xsi:type="object">\Namespace\Class</argument>
     */
    public static PsiElementPattern.Capture<PsiElement> getArgumentObjectPattern() {
        return XmlPatterns
            .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
            .withParent(
                XmlPatterns
                    .xmlText()
                    .withParent(XmlPatterns
                        .xmlTag()
                        .withName("argument")
                        .withAttributeValue("xsi:type", "object")
                    )
            ).inFile(XmlHelper.getXmlFilePattern());
    }

    /**
     * <item name="argumentName" xsi:type="object">\Namespace\Class</argument>
     */
    public static PsiElementPattern.Capture<PsiElement> getItemObjectPattern() {
        return XmlPatterns
            .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
            .withParent(
                XmlPatterns
                    .xmlText()
                    .withParent(XmlPatterns
                            .xmlTag()
                            .withName("item")
                            .withAttributeValue("xsi:type", "object")
                    )
            ).inFile(XmlHelper.getXmlFilePattern());
    }

    public static PsiFilePattern.Capture<PsiFile> getXmlFilePattern() {
        return XmlPatterns.psiFile()
            .withName(XmlPatterns.string().equalTo("di.xml"));
    }

    public static PsiElementPattern.Capture<XmlTag> getInsideTagPattern(String insideTagName) {
        return XmlPatterns.psiElement(XmlTag.class).withName(insideTagName);
    }

    /**
     * <tag attributeNames="|"/>
     *
     * @param tag tagname
     * @param attributeNames attribute values listen for
     */
    public static PsiElementPattern.Capture<PsiElement> getTagAttributePattern(String tag, String... attributeNames) {
        return XmlPatterns
            .psiElement()
            .inside(XmlPatterns
                    .xmlAttributeValue()
                    .inside(XmlPatterns
                            .xmlAttribute()
                            .withName(StandardPatterns.string().oneOfIgnoreCase(attributeNames))
                            .withParent(XmlPatterns
                                    .xmlTag()
                                    .withName(tag)
                            )
                    )
            ).inFile(getXmlFilePattern());
    }

    /**
     * <argument name="argumentName" >
     */
    public static XmlAttributeValuePattern getArgumentNamePattern() {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute("name")
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                            .withName("argument")
                    )
            ).inside(
                XmlHelper.getInsideTagPattern("argument")
            ).inFile(XmlHelper.getXmlFilePattern());
    }
}
