package com.magento.idea.magento2plugin.xml.di.reference.provider.resolver;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 10/20/15.
 */
public class VirtualTypeTagResolver extends ClassNameResolver {
    public static final VirtualTypeTagResolver INSTANCE = new VirtualTypeTagResolver();

    private VirtualTypeTagResolver() {}

    @Nullable
    @Override
    public String resolveTypeName(XmlTag xmlTag) {
        String parentTypeName = xmlTag.getAttributeValue(XmlHelper.TYPE_ATTRIBUTE);

        if (parentTypeName == null) {
            return null;
        }

        String superParentName = VirtualTypesNamesFileBasedIndex.getSuperParentTypeName(xmlTag.getProject(), parentTypeName);

        return superParentName == null ? parentTypeName : superParentName;

    }

    @Override
    protected String getTagName() {
        return XmlHelper.VIRTUAL_TYPE_TAG;
    }
}
