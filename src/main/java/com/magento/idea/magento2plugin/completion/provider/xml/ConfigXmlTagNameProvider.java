/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.provider.xml;

import static com.magento.idea.magento2plugin.magento.files.ModuleConfigXmlFile.FILE_NAME;
import static com.magento.idea.magento2plugin.magento.files.ModuleConfigXmlFile.ROOT_TAG_NAME;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlTagNameProvider;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigXmlTagNameProvider implements XmlTagNameProvider {

    @Override
    public void addTagNameVariants(
            final List<LookupElement> elements,
            final @NotNull XmlTag tag,
            final String prefix
    ) {
        if (!Settings.isEnabled(tag.getProject()) || !validate(tag)) {
            return;
        }
        final int tagDepth = getTagDepth(tag);

        if (tagDepth == -1) {
            return;
        }
        final ConfigTagScope tagScope = getTagScope(tag);

        if (tagScope == null) {
            return;
        }
        final ConfigTagTypeByScopedDepth configType = ConfigTagTypeByScopedDepth
                .getTagTypeByScopeAndDepth(tagDepth, tagScope);

        if (configType == null) {
            return;
        }
        final String indexKey = configType.getConfigPath(tag);
        final String phrase = indexKey.isEmpty() ? prefix : indexKey + "." + prefix;

        elements.addAll(configType.makeCompletion(phrase, tag.getProject()));
    }

    private boolean validate(final @NotNull XmlTag tag) {
        if (!(tag.getContainingFile() instanceof XmlFile)) {
            return false;
        }
        final XmlFile file = (XmlFile) tag.getContainingFile();
        final XmlTag rootTag = file.getRootTag();

        if (!FILE_NAME.equals(file.getName())
                || rootTag == null
                || !ROOT_TAG_NAME.equals(rootTag.getName())) {
            return false;
        }
        final int tagDepth = getTagDepth(tag);

        return tagDepth >= 2 && tagDepth <= 5;
    }

    private int getTagDepth(final @NotNull XmlTag tag) {
        if (!(tag.getContainingFile() instanceof XmlFile)) {
            return -1;
        }
        final XmlFile file = (XmlFile) tag.getContainingFile();
        final XmlTag rootTag = file.getRootTag();

        if (rootTag == null) {
            return -1;
        }
        int depth = 0;
        XmlTag iterable = tag;

        while (!iterable.equals(rootTag)) {
            depth++;
            iterable = iterable.getParentTag();

            if (iterable == null) {
                break;
            }
        }

        return depth;
    }

    private @Nullable ConfigTagScope getTagScope(final @NotNull XmlTag tag) {
        if (!(tag.getContainingFile() instanceof XmlFile)) {
            return null;
        }
        final XmlFile file = (XmlFile) tag.getContainingFile();
        final XmlTag rootTag = file.getRootTag();

        if (rootTag == null) {
            return null;
        }
        XmlTag scopeTag = null;
        XmlTag iterable = tag;

        while (!iterable.equals(rootTag)) {
            scopeTag = iterable;
            iterable = iterable.getParentTag();

            if (iterable == null) {
                break;
            }
        }

        if (scopeTag == null) {
            return null;
        }

        return ConfigTagScope.getScopeByTagName(scopeTag.getName());
    }

    private enum ConfigTagTypeByScopedDepth {

        SECTION(2, 3, 3, 0),
        GROUP(3, 4, 4, 1),
        FIELD(4, 5, 5, 2);

        private final int defaultScope;
        private final int websitesScope;
        private final int storesScope;
        private final int fallbackDepth;

        ConfigTagTypeByScopedDepth(
                final int defaultScope,
                final int websitesScope,
                final int storesScope,
                final int fallbackDepth
        ) {
            this.defaultScope = defaultScope;
            this.websitesScope = websitesScope;
            this.storesScope = storesScope;
            this.fallbackDepth = fallbackDepth;
        }

        public static ConfigTagTypeByScopedDepth getTagTypeByScopeAndDepth(
                final int depth,
                final ConfigTagScope scope
        ) {
            for (final ConfigTagTypeByScopedDepth tagType : ConfigTagTypeByScopedDepth.values()) {
                int depthForScope = -1;

                if (ConfigTagScope.DEFAULT.equals(scope)) {
                    depthForScope = tagType.defaultScope;
                } else if (ConfigTagScope.WEBSITES.equals(scope)) {
                    depthForScope = tagType.websitesScope;
                } else if (ConfigTagScope.STORES.equals(scope)) {
                    depthForScope = tagType.storesScope;
                }

                if (depth == depthForScope) {
                    return tagType;
                }
            }

            return null;
        }

        public @NotNull String getConfigPath(final @NotNull XmlTag tag) {
            final List<String> parts = new ArrayList<>();
            XmlTag iterable = tag.getParentTag();

            for (int i = 0; i < fallbackDepth; i++) {
                if (iterable != null) {
                    parts.add(iterable.getName());
                    iterable = iterable.getParentTag();
                }
            }

            if (parts.isEmpty()) {
                return "";
            }
            Collections.reverse(parts);

            return String.join(".", parts);
        }

        public List<LookupElement> makeCompletion(
                final @NotNull String phrase,
                final @NotNull Project project
        ) {
            if (this.equals(ConfigTagTypeByScopedDepth.SECTION)) {
                return SectionNameCompletionProvider.makeCompletion(phrase, project);
            } else if (this.equals(ConfigTagTypeByScopedDepth.GROUP)) {
                return GroupNameCompletionProvider.makeCompletion(phrase, project);
            } else if (this.equals(ConfigTagTypeByScopedDepth.FIELD)) {
                return FieldNameCompletionProvider.makeCompletion(phrase, project);
            }

            return new ArrayList<>();
        }
    }

    private enum ConfigTagScope {

        DEFAULT("default"),
        WEBSITES("websites"),
        STORES("stores");

        private final String scopeTagName;

        ConfigTagScope(final String scopeTagName) {
            this.scopeTagName = scopeTagName;
        }

        public static ConfigTagScope getScopeByTagName(final @NotNull String tagName) {
            for (final ConfigTagScope scope : ConfigTagScope.values()) {
                if (tagName.equals(scope.scopeTagName)) {
                    return scope;
                }
            }

            return null;
        }
    }
}
