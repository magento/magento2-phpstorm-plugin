package com.magento.idea.magento2plugin.xml;

import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;

/**
 * Created by dkvashnin on 11/2/15.
 */
public class XmlHelperUtility {
    public static PsiFilePattern.Capture<PsiFile> getXmlFilePattern(String fileName) {
        return XmlPatterns.psiFile()
            .withName(XmlPatterns.string().equalTo(fileName + ".xml"));
    }

    public static PsiElementPattern.Capture<XmlTag> getInsideTagPattern(String insideTagName) {
        return XmlPatterns.psiElement(XmlTag.class).withName(insideTagName);
    }

    /**
     * <tagName attributeName="XmlAttributeValue">
     */
    public static XmlAttributeValuePattern getTagAttributeValuePattern(String tagName, String attributeName, String fileType) {
        return XmlPatterns
            .xmlAttributeValue()
            .withParent(
                XmlPatterns
                    .xmlAttribute(attributeName)
                    .withParent(
                        XmlPatterns
                            .xmlTag()
                            .withName(tagName)
                    )
            ).inside(
                getInsideTagPattern(tagName)
            ).inFile(getXmlFilePattern(fileType));
    }

    /**
     * <tag attributeNames="|"/>
     */
    public static PsiElementPattern.Capture<PsiElement> getTagAttributePattern(String tag, String attributeName, String fileName) {
        return XmlPatterns
            .psiElement()
            .inside(XmlPatterns
                    .xmlAttributeValue()
                    .inside(XmlPatterns
                            .xmlAttribute()
                            .withName(attributeName)
                            .withParent(XmlPatterns
                                    .xmlTag()
                                    .withName(tag)
                            )
                    )
            ).inFile(getXmlFilePattern(fileName));
    }
}
