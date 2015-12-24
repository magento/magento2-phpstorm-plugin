package com.magento.idea.magento2plugin.xml.webapi;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;
import org.jetbrains.annotations.Nullable;

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
     * <tag attributeNames="|"/>
     */
    public static PsiElementPattern.Capture<PsiElement> getTagAttributePattern(String tag, String attributeName) {
        return getTagAttributePattern(tag, attributeName, FILE_TYPE);
    }

    @Nullable
    public static XmlAttribute getInterfaceAttributeByMethod(PsiElement psiElement)
    {
        // get service xml tag `method` attribute
        XmlAttribute methodAttribute = PsiTreeUtil.getParentOfType(psiElement, XmlAttribute.class);
        if (methodAttribute == null) {
            return null;
        }

        // get service xml tag
        XmlTag serviceTag = PsiTreeUtil.getParentOfType(methodAttribute, XmlTag.class);
        if (serviceTag == null) {
            return null;
        }

        // get service xml tag `class` attribute
        return serviceTag.getAttribute(CLASS_ATTRIBUTE);
    }
}
