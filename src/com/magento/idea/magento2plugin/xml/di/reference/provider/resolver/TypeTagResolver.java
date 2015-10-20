package com.magento.idea.magento2plugin.xml.di.reference.provider.resolver;

import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 10/19/15.
 */
public class TypeTagResolver extends ClassNameResolver {
    public static final TypeTagResolver INSTANCE = new TypeTagResolver();

    private static final String TAG_NAME = "type";
    private static final String ATTRIBUTE_NAME = "name";

    private TypeTagResolver() {}

    @Nullable
    @Override
    public String resolveTypeName(XmlTag xmlTag) {
        return xmlTag.getAttributeValue(ATTRIBUTE_NAME);
    }

    @Override
    protected String getTagName() {
        return TAG_NAME;
    }
}
