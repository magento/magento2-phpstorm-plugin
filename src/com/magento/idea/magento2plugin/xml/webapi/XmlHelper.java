package com.magento.idea.magento2plugin.xml.webapi;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;

/**
 * Created by isentiabov on 20.12.2015.
 */
public class XmlHelper extends XmlHelperUtility {
    public static final String FILE_TYPE = "webapi";
    public static final String SERVICE_TAG = "service";
    public static final String METHOD_ATTRIBUTE = "method";
    public static final String CLASS_ATTRIBUTE = "class";

    /**
     * <service method="MethodName">
     */
    public static XmlAttributeValuePattern getMethodAttributePattern() {
        return getTagAttributeValuePattern(SERVICE_TAG, METHOD_ATTRIBUTE, FILE_TYPE);
    }

    /**
     * <argument name="argumentName" xsi:type="typeName">\Namespace\Class</argument>
     * @param typeName Type name
     */
    public static PsiElementPattern.Capture<PsiElement> getArgumentValuePatternForType(String typeName) {
        return XmlPatterns
                .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .withParent(
                        XmlPatterns
                                .xmlText()
                                .withParent(XmlPatterns
                                        .xmlTag()
                                        .withName("argument")
                                        .withAttributeValue("xsi:type", typeName)
                                )
                ).inFile(XmlHelper.getXmlFilePattern(FILE_TYPE));
    }

    /**
     * <item name="argumentName" xsi:type="typeName">\Namespace\Class</argument>
     * @param typeName Type name
     */
    public static PsiElementPattern.Capture<PsiElement> getItemValuePatternForType(String typeName) {
        return XmlPatterns
                .psiElement(XmlTokenType.XML_DATA_CHARACTERS)
                .withParent(
                        XmlPatterns
                                .xmlText()
                                .withParent(XmlPatterns
                                        .xmlTag()
                                        .withName("item")
                                        .withAttributeValue("xsi:type", typeName)
                                )
                ).inFile(XmlHelper.getXmlFilePattern(FILE_TYPE));
    }

    /**
     * <tag attributeNames="|"/>
     */
    public static PsiElementPattern.Capture<PsiElement> getTagAttributePattern(String tag, String attributeName) {
        return getTagAttributePattern(tag, attributeName, FILE_TYPE);
    }
}
