package com.magento.idea.magento2plugin.xml.di.reference.provider.resolver;

import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 10/20/15.
 */
public class VirtualTypeTagResolver extends ClassNameResolver {
    public static final VirtualTypeTagResolver INSTANCE = new VirtualTypeTagResolver();

    private static final String TAG_NAME = "virtualType";

    private VirtualTypeTagResolver() {}

    @Nullable
    @Override
    public String resolveTypeName(XmlTag xmlTag) {
        return xmlTag.getAttributeValue("type");
    }

    @Override
    protected String getTagName() {
        return TAG_NAME;
    }
}
