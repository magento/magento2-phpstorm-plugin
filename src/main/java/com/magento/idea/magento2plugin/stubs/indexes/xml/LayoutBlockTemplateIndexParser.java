/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileContent;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LayoutBlockTemplateIndexParser {
    private static final String BLOCK_TAG = "block";
    private static final String REFERENCE_BLOCK_TAG = "referenceBlock";
    private static final String ARGUMENT_TAG = "argument";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String TEMPLATE_ATTRIBUTE = "template";
    private static final String NAME_ATTRIBUTE = "name";
    private static LayoutBlockTemplateIndexParser INSTANCE;

    private LayoutBlockTemplateIndexParser() {
    }

    public static LayoutBlockTemplateIndexParser getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LayoutBlockTemplateIndexParser();
        }

        return INSTANCE;
    }

    public @NotNull LayoutBlockTemplateData parse(final @NotNull FileContent fileContent) {
        final Map<String, Set<String>> blockTemplates = new HashMap<>();
        final Map<String, Set<String>> templateBlocks = new HashMap<>();
        final PsiFile psiFile = fileContent.getPsiFile();

        if (!Settings.isEnabled(psiFile.getProject()) || !(psiFile instanceof XmlFile)) {
            return new LayoutBlockTemplateData(blockTemplates, templateBlocks);
        }
        final XmlDocument document = ((XmlFile) psiFile).getDocument();

        if (document == null || document.getRootTag() == null) {
            return new LayoutBlockTemplateData(blockTemplates, templateBlocks);
        }
        collect(document.getRootTag(), blockTemplates, templateBlocks);

        return new LayoutBlockTemplateData(blockTemplates, templateBlocks);
    }

    private void collect(
            final @NotNull XmlTag tag,
            final @NotNull Map<String, Set<String>> blockTemplates,
            final @NotNull Map<String, Set<String>> templateBlocks
    ) {
        if (isBlockTemplateDeclaration(tag)) {
            collectBlock(tag, blockTemplates, templateBlocks);
        }

        for (final XmlTag subTag : tag.getSubTags()) {
            collect(subTag, blockTemplates, templateBlocks);
        }
    }

    private boolean isBlockTemplateDeclaration(final @NotNull XmlTag tag) {
        return BLOCK_TAG.equals(tag.getName()) || REFERENCE_BLOCK_TAG.equals(tag.getName());
    }

    private void collectBlock(
            final @NotNull XmlTag blockTag,
            final @NotNull Map<String, Set<String>> blockTemplates,
            final @NotNull Map<String, Set<String>> templateBlocks
    ) {
        final String blockClass = normalizeClass(blockTag.getAttributeValue(CLASS_ATTRIBUTE));

        if (blockClass == null) {
            return;
        }
        addPair(blockClass, blockTag.getAttributeValue(TEMPLATE_ATTRIBUTE), blockTemplates, templateBlocks);

        for (final XmlTag argument : PsiTreeUtil.findChildrenOfType(blockTag, XmlTag.class)) {
            if (ARGUMENT_TAG.equals(argument.getName())
                    && TEMPLATE_ATTRIBUTE.equals(argument.getAttributeValue(NAME_ATTRIBUTE))) {
                addPair(blockClass, argument.getValue().getText(), blockTemplates, templateBlocks);
            }
        }
    }

    private void addPair(
            final @NotNull String blockClass,
            final @Nullable String rawTemplate,
            final @NotNull Map<String, Set<String>> blockTemplates,
            final @NotNull Map<String, Set<String>> templateBlocks
    ) {
        final String template = normalizeTemplate(rawTemplate);

        if (template == null) {
            return;
        }
        blockTemplates.computeIfAbsent(blockClass, ignored -> new LinkedHashSet<>()).add(template);
        templateBlocks.computeIfAbsent(template, ignored -> new LinkedHashSet<>()).add(blockClass);
    }

    private @Nullable String normalizeClass(final @Nullable String blockClass) {
        if (blockClass == null || blockClass.isBlank()) {
            return null;
        }

        return blockClass.startsWith("\\") ? blockClass.substring(1) : blockClass;
    }

    private @Nullable String normalizeTemplate(final @Nullable String template) {
        if (template == null || template.isBlank()) {
            return null;
        }

        return template.trim();
    }

    public static class LayoutBlockTemplateData {
        private final Map<String, Set<String>> blockTemplates;
        private final Map<String, Set<String>> templateBlocks;

        LayoutBlockTemplateData(
                final @NotNull Map<String, Set<String>> blockTemplates,
                final @NotNull Map<String, Set<String>> templateBlocks
        ) {
            this.blockTemplates = blockTemplates;
            this.templateBlocks = templateBlocks;
        }

        public @NotNull Map<String, Set<String>> getBlockTemplates() {
            return blockTemplates;
        }

        public @NotNull Map<String, Set<String>> getTemplateBlocks() {
            return templateBlocks;
        }
    }
}
