package com.magento.idea.magento2plugin.xml.webapi;

import com.intellij.patterns.XmlAttributeValuePattern;
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
}
