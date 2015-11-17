package com.magento.idea.magento2plugin.xml.observer;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;

/**
 * Created by dkvashnin on 11/17/15.
 */
public class XmlHelper extends XmlHelperUtility {
    public static final String FILE_TYPE = "events";

    public static final String OBSERVER_TAG = "observer";
    public static final String EVENT_TAG = "event";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String INSTANCE_ATTRIBUTE = "instance";

    /**
     * <tagName attributeName="XmlAttributeValue">
     */
    public static XmlAttributeValuePattern getTagAttributeValuePattern(String tagName, String attributeName) {
        return getTagAttributeValuePattern(tagName, attributeName, FILE_TYPE);
    }
}
