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
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.xml.util.XmlIncludeHandler;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.PhpNameUtil;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class PhpClassNameIndex extends ScalarIndexExtension<String> {
    private static final Pattern CLASS_REFERENCE_PATTERN = Pattern.compile(
            "(\\\\?" + RegExUtil.PhpRegex.CLASS_NAME
                    + "(?:\\\\" + RegExUtil.PhpRegex.CLASS_NAME + ")+)"
                    + "(?:" + RegExUtil.XmlRegex.CLASS_MEMBER_NAME + ")?"
    );

    public static final ID<String, Void> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.xml.php_class_name");

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Void> map = new HashMap<>();
            final PsiFile psiFile = inputData.getPsiFile();
            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }

            forEachClassReference(
                    (XmlFile) psiFile,
                    (xmlTag, classNames) -> classNames.forEach(name -> map.put(name, null))
            );

            return map;
        };
    }

    /**
     * Passes every tag of the file with the classes that one of its values refers to.
     *
     * @param xmlFile XmlFile
     * @param consumer receives the tag once per value that refers to a class
     */
    public static void forEachClassReference(
            final @NotNull XmlFile xmlFile,
            final @NotNull BiConsumer<XmlTag, Set<String>> consumer
    ) {
        final XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(
                xmlFile.getFirstChild(),
                XmlTag.class
        );

        if (xmlTags == null) {
            return;
        }
        for (final XmlTag xmlTag: xmlTags) {
            visitSubTags(xmlTag, consumer);
        }
    }

    /**
     * Get the classes a configuration value refers to: the written class and, for a generated
     * proxy or factory name, also the class it is generated from.
     *
     * @param value String
     *
     * @return class names without a leading backslash
     */
    public static @NotNull Set<String> getReferencedClasses(final @NotNull String value) {
        if (value.indexOf('\\') < 0) {
            return Set.of();
        }
        final Matcher matcher = CLASS_REFERENCE_PATTERN.matcher(value.trim());

        if (!matcher.matches()) {
            return Set.of();
        }

        return PhpNameUtil.getObjectTypeCandidates(
                PhpNameUtil.toPresentableFQN(matcher.group(1))
        );
    }

    private static void visitSubTags(
            final XmlTag parentTag,
            final BiConsumer<XmlTag, Set<String>> consumer
    ) {
        for (final XmlTag childTag: parentTag.getSubTags()) {
            for (final XmlAttribute xmlAttribute: childTag.getAttributes()) {
                final String xmlAttributeValue = xmlAttribute.getValue();

                if (xmlAttributeValue != null) {
                    visitValue(childTag, xmlAttributeValue, consumer);
                }
            }
            if (XmlIncludeHandler.isXInclude(childTag)
                    || !XmlPsiTreeUtil.findSubTagsOfParent(childTag, "xi:include").isEmpty()) {
                continue;
            }
            visitValue(childTag, childTag.getValue().getTrimmedText(), consumer);
            visitSubTags(childTag, consumer);
        }
    }

    private static void visitValue(
            final XmlTag xmlTag,
            final String value,
            final BiConsumer<XmlTag, Set<String>> consumer
    ) {
        final Set<String> classNames = getReferencedClasses(value);

        if (!classNames.isEmpty()) {
            consumer.accept(xmlTag, classNames);
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
        return 2;
    }
}
