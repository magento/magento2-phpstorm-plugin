/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.js;

import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.data.StringSetDataExternalizer;
import com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil;
import com.magento.idea.magento2plugin.util.magento.js.JsPsiSupportProvider;
import com.magento.idea.magento2plugin.util.magento.js.MagentoJsParser;
import com.magento.idea.magento2plugin.util.magento.js.RequireJsConfig;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class JsMixinIndex extends FileBasedIndexExtension<String, Set<String>> {
    public static final ID<String, Set<String>> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.js_mixins"
    );

    @Override
    public @NotNull ID<String, Set<String>> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Set<String>> map = new HashMap<>();

            if (!Settings.isEnabled(inputData.getProject())) {
                return map;
            }

            final JsPsiSupportProvider jsProvider = JsPsiSupportProvider.getInstance();
            final RequireJsConfig config = jsProvider != null
                    ? jsProvider.parseRequireJsConfig(inputData.getPsiFile())
                    : null;
            if (config != null) {
                map.putAll(toMixinMap(config));
            } else {
                map.putAll(getMixinMap(MagentoVfsUtil.getContentAsText(inputData)));
            }

            return map;
        };
    }

    public static @NotNull Map<String, Set<String>> getMixinMap(final @NotNull PsiFile psiFile) {
        final JsPsiSupportProvider jsProvider = JsPsiSupportProvider.getInstance();
        final RequireJsConfig config = jsProvider != null
                ? jsProvider.parseRequireJsConfig(psiFile)
                : null;
        if (config != null) {
            return toMixinMap(config);
        }
        return getMixinMap(psiFile.getText());
    }

    public static @NotNull Map<String, Set<String>> getMixinMap(final @NotNull CharSequence source) {
        return toMixinMap(MagentoJsParser.parseRequireJsConfig(source));
    }

    private static @NotNull Map<String, Set<String>> toMixinMap(final @NotNull RequireJsConfig config) {
        final Map<String, Set<String>> map = new HashMap<>();

        for (final RequireJsConfig.MixinEntry entry : config.mixinEntries()) {
            map.computeIfAbsent(entry.target(), ignored -> new LinkedHashSet<>()).add(entry.mixin());
        }

        return map;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull DataExternalizer<Set<String>> getValueExternalizer() {
        return StringSetDataExternalizer.INSTANCE;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> {
            if (!"requirejs-config.js".equals(virtualFile.getName())) {
                return false;
            }
            final JsPsiSupportProvider jsProvider = JsPsiSupportProvider.getInstance();
            return jsProvider == null || jsProvider.isJavaScriptFile(virtualFile);
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1 + JsPsiSupportProvider.versionDelta();
    }
}
