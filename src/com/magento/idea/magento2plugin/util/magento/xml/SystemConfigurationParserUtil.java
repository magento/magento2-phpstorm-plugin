/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.xml;

import static com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile.FIELD_TAG_NAME;
import static com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile.GROUP_TAG_NAME;
import static com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile.SECTION_TAG_NAME;
import static com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile.SYSTEM_TAG_NAME;
import static com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil.findSubTagsOfParent;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.magento.files.ModuleConfigXmlFile;
import com.magento.idea.magento2plugin.magento.files.ModuleSystemXmlFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("PMD.TooManyStaticImports")
public final class SystemConfigurationParserUtil {

    private SystemConfigurationParserUtil() {}

    /**
     * Parse system configuration tags (section, group, field) with the specified depth.
     *
     * @param rootTag XmlTag
     * @param parsingDepth ParsingDepth
     * @param action RunnableOnPathElement
     */
    public static void parseConfigurationTags(
            final @NotNull XmlTag rootTag,
            final ParsingDepth parsingDepth,
            final RunnableOnPathElement action
    ) {
        final XmlTag systemTag = getSystemTag(rootTag);

        if (systemTag == null) {
            return;
        }

        for (final XmlTag sectionTag : findSubTagsOfParent(systemTag, SECTION_TAG_NAME)) {
            if (ParsingDepth.SECTION_ID.equals(parsingDepth)) {
                action.run(sectionTag.getAttributeValue("id"), null, null);
                continue;
            }
            for (final XmlTag groupTag : findSubTagsOfParent(sectionTag, GROUP_TAG_NAME)) {
                if (ParsingDepth.GROUP_ID.equals(parsingDepth)) {
                    action.run(
                            sectionTag.getAttributeValue("id"),
                            groupTag.getAttributeValue("id"),
                            null
                    );
                    continue;
                }
                for (final XmlTag fieldTag : findSubTagsOfParent(groupTag, FIELD_TAG_NAME)) {
                    action.run(
                            sectionTag.getAttributeValue("id"),
                            groupTag.getAttributeValue("id"),
                            fieldTag.getAttributeValue("id")
                    );
                }
            }
        }
    }

    /**
     * Parse system configuration path from the specified xml tag.
     *
     * @param tag XmlTag
     * @param startFrom ParsingDepth
     *
     * @return String
     */
    public static @Nullable String parseOuterConfigPath(
            final @NotNull XmlTag tag,
            final @NotNull ParsingDepth startFrom
    ) {
        final String filename = tag.getContainingFile().getName();
        ParsingDepth currentDepth = startFrom;
        XmlTag currentTag = tag;
        final List<String> parts = new ArrayList<>();

        if (ModuleSystemXmlFile.FILE_NAME.equals(filename)) {
            currentTag = PsiTreeUtil.getParentOfType(
                    currentTag,
                    XmlTag.class
            );
        }

        while (currentDepth != null) {
            if (currentTag == null) {
                break;
            }
            if (ModuleSystemXmlFile.FILE_NAME.equals(filename)) {
                final String identity = currentTag.getAttributeValue("id");

                if (identity != null) {
                    parts.add(identity);
                }
            } else if (ModuleConfigXmlFile.FILE_NAME.equals(filename)) {
                parts.add(currentTag.getName());
            }
            currentDepth = ParsingDepth.getPrevious(currentDepth);
            currentTag = PsiTreeUtil.getParentOfType(
                    currentTag,
                    XmlTag.class
            );
        }

        if (parts.isEmpty()) {
            return null;
        }
        Collections.reverse(parts);

        return String.join(".", parts);
    }

    public enum ParsingDepth {

        SECTION_ID(0),
        GROUP_ID(1),
        FIELD_ID(2);

        private final int depth;

        /**
         * Parsing depth ENUM constructor.
         *
         * @param depth int
         */
        ParsingDepth(final int depth) {
            this.depth = depth;
        }

        /**
         * Get ENUM value by its depth number.
         *
         * @param depth int
         *
         * @return ParsingDepth
         */
        public static @Nullable ParsingDepth getByDepth(final int depth) {
            for (final ParsingDepth parsingDepth : ParsingDepth.values()) {
                if (parsingDepth.depth == depth) {
                    return parsingDepth;
                }
            }

            return null;
        }

        /**
         * Get previous ENUM value by its depth number.
         *
         * @param current ParsingDepth
         *
         * @return ParsingDepth
         */
        public static @Nullable ParsingDepth getPrevious(final @NotNull ParsingDepth current) {
            return getByDepth(current.depth - 1);
        }
    }

    public interface RunnableOnPathElement {
        void run(
                final @Nullable String sectionId,
                final @Nullable String groupId,
                final @Nullable String fieldId
        );
    }

    private static @Nullable XmlTag getSystemTag(final @NotNull XmlTag rootTag) {
        if (!ModuleSystemXmlFile.FILE_NAME.equals(rootTag.getContainingFile().getName())) {
            return null;
        }

        return rootTag.findFirstSubTag(SYSTEM_TAG_NAME);
    }
}
