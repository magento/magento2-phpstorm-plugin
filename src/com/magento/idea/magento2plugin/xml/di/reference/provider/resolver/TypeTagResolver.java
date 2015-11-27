package com.magento.idea.magento2plugin.xml.di.reference.provider.resolver;

import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 10/19/15.
 */
public class TypeTagResolver extends ClassNameResolver {
    public static final TypeTagResolver INSTANCE = new TypeTagResolver();

    private TypeTagResolver() {}

    @Nullable
    @Override
    public String resolveTypeName(XmlTag xmlTag) {
        return xmlTag.getAttributeValue(XmlHelper.NAME_ATTRIBUTE);
    }

    @Override
    protected String getTagName() {
        return XmlHelper.TYPE_TAG;
    }
}
