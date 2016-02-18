package com.magento.idea.magento2plugin.xml.di;

import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;

/**
 * Created by Warider on 17.08.2015.
 */
public class XmlHelper extends XmlHelperUtility {
    public static final String FILE_TYPE = "di";

    public static final String TYPE_TAG = "type";
    public static final String PLUGIN_TAG = "plugin";
    public static final String VIRTUAL_TYPE_TAG = "virtualType";
    public static final String PREFERENCE_TAG = "preference";
    public static final String ARGUMENT_TAG = "argument";
    public static final String ARGUMENTS_TAG = "arguments";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String FOR_ATTRIBUTE = "for";

    public static final String OBJECT_TYPE = "object";
    public static final String STRING_TYPE = "string";
    public static final String CONST_TYPE = "const";
    public static final String INIT_TYPE = "init_parameter";


    /**
     * <type name="\Namespace\Class">
     */
    public static XmlAttributeValuePattern getDiTypePattern() {
        return getTagAttributeValuePattern(TYPE_TAG, NAME_ATTRIBUTE, FILE_TYPE);
    }

    /**
     * <plugin type="\Namespace\Class">
     */
    public static XmlAttributeValuePattern getPluginTypePattern() {
        return getTagAttributeValuePattern(PLUGIN_TAG, TYPE_ATTRIBUTE, FILE_TYPE);
    }

    /**
     * <preference type="\Namespace\Class">
     */
    public static XmlAttributeValuePattern getDiPreferenceTypePattern() {
        return getTagAttributeValuePattern(PREFERENCE_TAG, TYPE_ATTRIBUTE, FILE_TYPE);
    }

    /**
     * <preference for="\Namespace\Type">
     */
    public static XmlAttributeValuePattern getDiPreferenceForPattern() {
        return getTagAttributeValuePattern(PREFERENCE_TAG, FOR_ATTRIBUTE, FILE_TYPE);
    }

    /**
     * <virtualType type="\Namespace\Class">
     */
    public static XmlAttributeValuePattern getDiVirtualTypePattern() {
        return getTagAttributeValuePattern(VIRTUAL_TYPE_TAG, TYPE_ATTRIBUTE, FILE_TYPE);
    }


    /**
     * <argument name="argumentName" >
     */
    public static XmlAttributeValuePattern getArgumentNamePattern() {
        return getTagAttributeValuePattern(ARGUMENT_TAG, NAME_ATTRIBUTE, FILE_TYPE);
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
