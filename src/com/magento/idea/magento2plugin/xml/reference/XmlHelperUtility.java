package com.magento.idea.magento2plugin.xml.reference;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.PsiFilePattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
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
}
