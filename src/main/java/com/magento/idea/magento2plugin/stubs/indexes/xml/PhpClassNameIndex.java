/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.xml.util.XmlIncludeHandler;
import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import gnu.trove.THashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class PhpClassNameIndex extends ScalarIndexExtension<String> {
    private static final String CLASS_NAME_PATTERN =
                "\\\\?" + RegExUtil.PhpRegex.CLASS_NAME
                + "(\\\\" + RegExUtil.PhpRegex.CLASS_NAME + ")+";

    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.xml.php_class_name");

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final THashMap<String, Void> map = new THashMap<>();
            final PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }

            final XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(
                    psiFile.getFirstChild(),
                    XmlTag.class
            );
            if (xmlTags == null) {
                return map;
            }

            for (final XmlTag xmlTag: xmlTags) {
                fillMap(xmlTag, map);
            }

            return map;
        };
    }

    private void fillMap(final XmlTag parentTag, final Map<String, Void> resultMap) { //NOPMD
        for (final XmlTag childTag: parentTag.getSubTags()) {
            for (final XmlAttribute xmlAttribute: childTag.getAttributes()) {
                final String xmlAttributeValue = xmlAttribute.getValue();
                if (xmlAttributeValue != null
                        && !xmlAttributeValue.isEmpty()
                        && xmlAttributeValue.matches(CLASS_NAME_PATTERN)
                ) {
                    resultMap.put(PhpLangUtil.toPresentableFQN(xmlAttributeValue), null);
                }
            }
            if (XmlIncludeHandler.isXInclude(childTag)) {
                return;
            }

            //skipping IDEA include tag
            final List<XmlTag> ideaIncludeTags = XmlPsiTreeUtil.findSubTagsOfParent(
                    childTag,
                    "xi:include"
            );
            if (!ideaIncludeTags.isEmpty()) {
                return;
            }

            final XmlTagValue childTagValue = childTag.getValue();
            final String tagValue = childTagValue.getTrimmedText();
            if (!tagValue.isEmpty() && tagValue.matches(CLASS_NAME_PATTERN)) {
                resultMap.put(PhpLangUtil.toPresentableFQN(tagValue), null);
            }

            fillMap(childTag, resultMap);
        }
    }

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file ->
                file.getFileType() == XmlFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
